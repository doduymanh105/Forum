package com.example.forum.feature.post;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.dto.CursorResponse;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.feature.ai.ContentModerationService;
import com.example.forum.feature.ai.GenerativeAiService;
import com.example.forum.feature.collection.PostCollectionRepository;
import com.example.forum.feature.follow.FollowRepository;
import com.example.forum.feature.media.CloudinaryService;
import com.example.forum.feature.media.dto.UploadResponseDto;
import com.example.forum.feature.post.dto.*;
import com.example.forum.domain.*;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.domain.Enum.MediaType;
import com.example.forum.feature.tag.TagRepository;
import com.example.forum.feature.tag.dto.TagDto;
import com.example.forum.feature.user.UserRepository;
import com.example.forum.feature.vote.VoteRepository;
import com.example.forum.feature.media.MediaRepository;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.feature.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final TagRepository tagRepo;
    private final VoteRepository voteRepository;
    private final MediaRepository mediaRepository;
    private final FollowRepository followRepository;
    private final PostCollectionRepository postCollectionRepository;

    private final SecurityUtils securityService;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;
    private final GenerativeAiService generativeAiService;
    private final ContentModerationService moderationService;

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequest request) {

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long userId = currentUser.getUserId();

        moderationService.validateContentStrictly(request.getPostTitle(), request.getPostContent());

        UserEntity creator = userRepo.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

//        Set<Category> categories = new HashSet<>(categoryRepo.findAllById(request.getCategoryIds()));
        Set<Tag> tags= new HashSet<>(tagRepo.findAllById(request.getTagIds()));

        PostEntity post = PostEntity.builder()
                .creator(creator)
//                .categories(categories)
                .postTitle(request.getPostTitle())
                .tags(tags)
                .postContent(request.getPostContent())
                .upvotes(0L)
                .downvotes(0L)
                .countedViews(0L)
                .isArchived(false)
                .build();


        postRepo.save(post);

        if(request.getMediaFiles() !=null && !request.getMediaFiles().isEmpty()){

            saveMediaEntity(request.getMediaFiles(), post);
        }

        NotificationEvent newNotificationEvent = notificationService.createEvent(
                EventType.NEW_POST,
                creator,
                request.getPostTitle(),
                post.getPostId(),
                "POST");

        notificationService.notifyFollowers(newNotificationEvent);

        return mapToPostResponseDto(post, currentUser, true);
    }

    @Override
    public void removeMediaFromPost(Long postId, Long mediaId) {
        PostEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        if (!post.getCreator().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_TO_DELETE_MEDIA);
        }

        MediaEntity mediaEntity = mediaRepository.findById(mediaId)
                        .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.MEDIA_NOT_FOUND));

        if (!mediaEntity.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException(MessageConstants.MEDIA_NOT_BELONG_TO_POST);
        }

        String publicIdToDelete = mediaEntity.getPublicId();

        mediaRepository.delete(mediaEntity);

        if (publicIdToDelete != null) {
            cloudinaryService.deleteImage(publicIdToDelete);
        }
    }

    public void saveMediaEntity(List<UploadResponseDto> mediaList, PostEntity post){
        if (mediaList == null || mediaList.isEmpty()) return;
        Set<MediaEntity> mediaEntitySet = mediaList.stream().map(media ->{
            MediaEntity mediaEntity = new MediaEntity();
            mediaEntity.setPost(post);
            mediaEntity.setPublicId(media.getId());
            mediaEntity.setUrl(media.getUrl());
            mediaEntity.setFormat(media.getFormat());
            mediaEntity.setSize(media.getBytes());

            if ("video".equalsIgnoreCase(media.getResourceType())) {
                mediaEntity.setMediaType(MediaType.VIDEO);
            } else {
                mediaEntity.setMediaType(MediaType.IMAGE);
            }
            return  mediaEntity;
        }).collect(Collectors.toSet());

        mediaRepository.saveAll(mediaEntitySet);
    }

    @Override
    @Transactional
    public PostResponseDto addMediaToPost(Long postId, List<MultipartFile> files) {
        PostEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        if (!post.getCreator().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_EDIT_POST);
        }

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException(MessageConstants.FILE_EMPTY);
        }

        List<UploadResponseDto> mediaInfo = cloudinaryService.uploadImages(files);

        saveMediaEntity(mediaInfo, post);

        return mapToPostResponseDto(post, currentUser, false);
    }

    @Override
    @Transactional
    public String getSummaryForPost(Long postId) {

        PostEntity post = postRepo.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        if (post.getSummary() != null && !post.getSummary().trim().isEmpty()) {
            return post.getSummary();
        }

        String content = post.getPostContent();
        // Strip HTML tags and replace non-breaking spaces (&nbsp;) with standard spaces before counting
        String cleanText = content != null ? content.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim() : "";
        int wordCount = cleanText.isEmpty() ? 0 : cleanText.split("\\s+").length;

        if (wordCount < 300) {
            throw new AppException(ErrorCode.POST_CONTENT_MIN_LENGTH);
        } else if (wordCount > 2000) {
            throw new AppException(ErrorCode.POST_CONTENT_MAX_LENGTH);
        }

        String aiSummary = generativeAiService.summarizeText(content);

        post.setSummary(aiSummary);
        postRepo.save(post);

        return aiSummary;
    }

    @Override
    public List<String> recommendTagsForContent(PostRecommendTagRequest request) {

        List<Tag> tagList = tagRepo.findAll();
        List<String> stringList = tagList.stream()
                .map(Tag::getTagName)
                .toList();

        List<String> aiRecommendedTags = generativeAiService.recommendTags(
                request.getTitle(),
                request.getContent(),
                stringList
        );
        return aiRecommendedTags.stream()
                .filter(stringList::contains)
                .toList();
    }

    public PostResponseDto mapToPostResponseDto(PostEntity post, UserEntity currentUser, boolean singlePost) {

        List<MediaEntity> mediaEntityList = mediaRepository.findByPostPostId(post.getPostId());

        String postContentPreview = post.getPostContent();

        String isVoted = null;
        boolean isSaved= false;
        if (currentUser != null) {
            Optional<Vote> voteOpt = voteRepository.findByUserEntityUserIdAndPostEntityPostId(currentUser.getUserId(), post.getPostId());
            if (voteOpt.isPresent()) {
                isVoted = voteOpt.get().getVoteType().toString();
            }

             isSaved = postCollectionRepository.existsByUserIdAndPostId(currentUser.getUserId(), post.getPostId());
        }

        return buildPostResponseDto(post, currentUser, mediaEntityList, isVoted, isSaved, singlePost);
    }

    private TagDto mapToTagDto(Tag tag) {
        return TagDto.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build();
    }


    @Override
    @Cacheable(value = "postDetail", key = "#postId")
    public PostResponseDto getPost(Long postId) {
        UserEntity currentUser = securityService.getCurrentUser();
        PostEntity post= postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));
        return mapToPostResponseDto(post, currentUser, true);
    }

    @Override
    public PagedResponse<PostResponseDto> getPostByUser(Long userId, String keyword, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        UserEntity owner = userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUserOrNull();

        Page<PostEntity> postEntitiesPage = postRepo.findByCreatorUserIdAndIsArchivedFalseAndPostTitleContainingIgnoreCase(userId,keyword, pageable);
        List<PostResponseDto> postListContent = postEntitiesPage.getContent().stream().map(postEntity -> mapToPostResponseDto(postEntity, currentUser, false)).toList();

        return new PagedResponse<>(
                postListContent,
                postEntitiesPage.getNumber(),
                postEntitiesPage.getSize(),
                postEntitiesPage.getTotalElements(),
                postEntitiesPage.getTotalPages(),
                postEntitiesPage.isLast()
        );
    }

    @Override
    public PagedResponse<PostResponseDto> getPosts(int page, int size, String sortBy, String sortDirect, String keyword) {

        Sort sort = sortDirect.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(Sort.Direction.DESC, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null) {
            keyword = "";
        }
        UserEntity currentUser = securityService.getCurrentUserOrNull();

        Page<PostEntity> postEntitiesPage = postRepo.findByPostTitleContainingIgnoreCaseAndIsArchivedFalse(keyword, pageable);
        List<PostResponseDto> postListContent = mapToPostResponseDtoList(postEntitiesPage.getContent(), currentUser, false);

        return new PagedResponse<>(
                postListContent,
                postEntitiesPage.getNumber(),
                postEntitiesPage.getSize(),
                postEntitiesPage.getTotalElements(),
                postEntitiesPage.getTotalPages(),
                postEntitiesPage.isLast()
        );
    }

    @Override
    public PagedResponse<PostResponseDto> searchPost(PostFilterRequest request, int page, int size) {

        int pageIndex = (page > 0) ? page - 1 : 0;

        Sort sort = Sort.by("createdAt").descending();

        if(request.getSortBy() != null){
            sort = switch (request.getSortBy().toString().toLowerCase()) {
                case "lowest" -> Sort.by("createdAt").ascending();
                case "top_vote" -> Sort.by("upvotes").descending();
                default -> sort;
            };
        }

        UserEntity user = securityService.getCurrentUser();

        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        Specification<PostEntity> specification = PostSpecification.getFilterSpec(request);

        Page<PostEntity> postPage = postRepo.findAll(specification, pageable);

        List<PostResponseDto> data = mapToPostResponseDtoList(postPage.getContent(), user, false);

        return new PagedResponse<>(
                data,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    @Override
    @Cacheable(value = "newsfeed", key = "#currentUser != null ? #currentUser.userId : 0L", condition = "#cursor == null")
    public CursorResponse<PostResponseDto> getNewsfeed (String cursor,UserEntity currentUser, int size){
        Long currentUserId = (currentUser != null) ? currentUser.getUserId() : 0L;

        return getNewsfeedFromDb(currentUserId, cursor, size, currentUser);
    }

    @Override
    public CursorResponse<PostResponseDto> getNewsfeedFromDb(Long userId,String cursor, int size, UserEntity currentUser) {

        Double cursorScore = null;
        Long cursorId = null;

        if (cursor != null && cursor.contains("_")) {
            String[] parts = cursor.split("_");
            cursorScore = Double.parseDouble(parts[0]);
            cursorId = Long.parseLong(parts[1]);
        }

        Pageable pageable = PageRequest.ofSize(size + 1);

        List<PostEntity> posts = postRepo.getNewsfeedRanking(currentUser.getUserId(),cursorScore, cursorId, pageable);

        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts.remove(posts.size() - 1);
        }

        List<PostResponseDto> postResponseDtoList = mapToPostResponseDtoList(posts, currentUser, false);

        String nextCursor = null;
        if (!posts.isEmpty()) {
            PostEntity lastPost = posts.get(posts.size() - 1);

            double hoursDiff = java.time.Duration.between(lastPost.getCreatedAt(), java.time.LocalDateTime.now()).toHours();
            long up = lastPost.getUpvotes() != null ? lastPost.getUpvotes() : 0;
            long down = lastPost.getDownvotes() != null ? lastPost.getDownvotes() : 0;

            boolean isFollowing = followRepository.existsById(new FollowId(currentUser.getUserId(), lastPost.getCreator().getUserId()));
            int followBonus = isFollowing ? 50 : 0;

            double lastScore = ((up - down) * 5) + (lastPost.getCommentCount() * 10) + followBonus - (hoursDiff * 2);
            nextCursor = lastScore + "_" + lastPost.getPostId();
        }
        return new CursorResponse<>(postResponseDtoList, nextCursor, hasNext);
    }

    @Override
    @CacheEvict(value = "postDetail", key = "#postId")
    public PostResponseDto updatePost(Long postId, UpdatePostRequest request) {

        PostEntity post = postRepo.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        if(post.getIsArchived()){
            throw new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND);
        }

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long currentUserId = currentUser.getUserId();

        if(!post.getCreator().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_EDIT_POST);
        }

        if(request.getTitle() !=null && !request.getTitle().isBlank()) {
            post.setPostTitle(request.getTitle());
        }
        if(request.getContent() !=null && !request.getContent().isBlank()) {
            post.setPostContent(request.getContent());
        }

        if(request.getTagSet()!= null) {
            Set<Tag> tags = new HashSet<>(tagRepo.findAllById(request.getTagSet()));
            post.setTags(tags);
        }

        postRepo.save(post);

        return mapToPostResponseDto(post, currentUser, true);
    }



    @Override
    @CacheEvict(value = "postDetail", key = "#id")
    public void softDeletePost(Long id) {
        PostEntity post= postRepo.findByPostId(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        if(post.getIsArchived()){
            throw new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND);
        }

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(!currentUserId.equals(post.getCreator().getUserId())) {
            throw new AccessDeniedException(MessageConstants.NO_PERMISSION_EDIT_POST);
        }
        post.setIsArchived(true);
        postRepo.save(post);
    }

    @Override
    public void hardDeletePost(Long id) {
        PostEntity post= postRepo.findByPostId(id)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));
        postRepo.delete(post);
    }


    private PostResponseDto buildPostResponseDto(
            PostEntity post,
            UserEntity user,
            List<MediaEntity> mediaEntityList,
            String isVoted,
            Boolean isSaved,
            boolean singlePost
    ){
        String postContentPreview = post.getPostContent();
        Integer timeRead = 0;
        if (post.getPostContent() != null && !post.getPostContent().isEmpty()) {
            String plainText = post.getPostContent().replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
            int words = plainText.isEmpty() ? 0 : plainText.split("\\s+").length;
            timeRead = (int) Math.ceil((double) words / 150);
            if (!singlePost) {
                if(post.getPostContent().length() <= 150){
                    postContentPreview = post.getPostContent();
                } else {
                    postContentPreview = post.getPostContent().substring(0, 150);
                }
            }
        }
        return PostResponseDto.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(postContentPreview)
                .thumbnailUrl(post.getThumbnailUrl())
                .upvotes(post.getUpvotes())
                .downvotes(post.getDownvotes())
                .countedViews(post.getCountedViews())
                .mediaEntityList(mediaEntityList != null ? mediaEntityList : new ArrayList<>())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .creatorName(post.getCreator().displayUsername())
                .creatorId(post.getCreator().getUserId())
                .creatorAvatarUrl(post.getCreator().getAvatarUrl())
                .tags(post.getTags().stream().map(this::mapToTagDto).collect(Collectors.toSet()))
                .commentCount((long) post.getCommentCount())
                .timeRead(timeRead)
                .isVoted(isVoted)
                .isSaved(isSaved)
                .build();
    }

    public List<PostResponseDto> mapToPostResponseDtoList(
            List<PostEntity> posts,
            UserEntity currentUser,
            boolean singlePost
    ){
        List<Long> postIds = posts.stream().map(PostEntity::getPostId).toList();
        List<MediaEntity> allMedia = mediaRepository.findByPostPostIdIn(postIds);

        Map<Long, List<MediaEntity>> mediaMap = allMedia.stream()
                .collect(Collectors.groupingBy(media -> media.getPost().getPostId()));

        Map<Long, String> voteMap = new HashMap<>();
        if (currentUser != null) {
            List<Vote> userVotes = voteRepository.findByUserEntityUserIdAndPostEntityPostIdIn(currentUser.getUserId(), postIds);
            userVotes.forEach(vote -> voteMap.put(vote.getPostEntity().getPostId(), vote.getVoteType().toString()));
        }

        Map<Long, Boolean> savedMap = new HashMap<>();
        if(currentUser!= null){
            List<Long> savedList = postCollectionRepository.findByUserIdAndPostIdIn(currentUser.getUserId(),postIds);
            savedList.forEach(id -> savedMap.put(id, true));
        }

        return posts.stream()
                .map(post -> {
                    List<MediaEntity> mediaList = mediaMap.getOrDefault(post.getPostId(), new ArrayList<>());
                    String isVoted = voteMap.get(post.getPostId());
                    Boolean isSaved = savedMap.getOrDefault(post.getPostId(), false);

                    return buildPostResponseDto(post, currentUser, mediaList, isVoted, isSaved, singlePost);
                }).toList();
    }

}
