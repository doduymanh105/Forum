package com.example.forum.feature.post;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.dto.CursorResponse;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.common.service.cache.CacheService;
import com.example.forum.feature.follow.FollowRepository;
import com.example.forum.feature.media.CloudinaryService;
import com.example.forum.feature.comment.CommentRepository;
import com.example.forum.feature.media.dto.UploadResponseDto;
import com.example.forum.feature.post.dto.CreatePostRequest;
import com.example.forum.feature.post.dto.PostFilterRequest;
import com.example.forum.feature.post.dto.PostResponseDto;
import com.example.forum.feature.post.dto.UpdatePostRequest;
import com.example.forum.domain.*;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.domain.Enum.MediaType;
import com.example.forum.feature.tag.TagRepository;
import com.example.forum.feature.tag.dto.TagDto;
import com.example.forum.feature.user.UserRepository;
import com.example.forum.feature.user.dto.UserSummaryDto;
import com.example.forum.feature.vote.VoteRepository;
import com.example.forum.feature.media.MediaRepository;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.feature.notification.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final TagRepository tagRepo;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final MediaRepository mediaRepository;
    private final FollowRepository followRepository;

    private final SecurityUtils securityService;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequest request) {

        UserEntity currentUser = securityService.getCurrentUser();  // dùng service
        Long userId = currentUser.getUserId();

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

        return mapToPostResponseDto(post, currentUser);
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

        return mapToPostResponseDto(post, currentUser);
    }

    private PostResponseDto mapToPostResponseDto(PostEntity post, UserEntity currentUser) {

        Long commentCount = commentRepository.countByPostEntity(post);

        List<MediaEntity> mediaEntityList = mediaRepository.findByPostPostId(post.getPostId());

        String postContentPreview = "";

        Integer timeRead =0;
        if (post.getPostContent() != null && !post.getPostContent().isEmpty()) {
            int words = post.getPostContent().split("\\s+").length;
            timeRead = (int) Math.ceil((double) words / 150);
            if(post.getPostContent().length()<=150){
                postContentPreview=post.getPostContent();
            } else {
                postContentPreview = post.getPostContent().substring(0, 150);
            }
        }

        String isVoted = null;
        Boolean isSaved = false;

        if (currentUser != null) {
            Optional<Vote> voteOpt = voteRepository.findByUserEntityUserIdAndPostEntityPostId(currentUser.getUserId(), post.getPostId());
            if (voteOpt.isPresent()) {
                isVoted = voteOpt.get().getVoteType().toString();
            }

            // 4. Logic kiểm tra isSaved (TODO: Bạn cần tạo SavePostRepository)
            // isSaved = savePostRepo.existsByUserEntityAndPostEntity(currentUser, post);
        }

        return PostResponseDto.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(postContentPreview)
                .thumbnailUrl(post.getThumbnailUrl())
                .upvotes(post.getUpvotes())
                .downvotes(post.getDownvotes())
                .countedViews(post.getCountedViews())
                .mediaEntityList( mediaEntityList)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .creatorName(post.getCreator().displayUsername())
                .creatorId(post.getCreator().getUserId())
                .creatorAvatarUrl(post.getCreator().getAvatarUrl())
//                .categories(post
//                        .getCategories().stream()
//                        .map(this::mapToCategoryDto)
//                        .collect(Collectors.toSet())
//                )
                .tags(post.getTags().stream()
                        .map(this::mapToTagDto)
                        .collect(Collectors.toSet())
                )
                .commentCount(commentCount)
                .timeRead(timeRead)
                .isVoted(isVoted)
                .isSaved(isSaved)
                .build();
    }
    private UserSummaryDto mapToUserSummaryDto(UserEntity user) {
        return UserSummaryDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private TagDto mapToTagDto(Tag tag) {
        return TagDto.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build();
    }


    @Override
    public PostResponseDto getPost(Long postId) {
        UserEntity currentUser = securityService.getCurrentUser();
        PostEntity post= postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));
        return mapToPostResponseDto(post, currentUser);
    }

    @Override
    public PagedResponse<PostResponseDto> getPostByUser(Long userId, String keyword, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        UserEntity owner = userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUserOrNull();

        Page<PostEntity> postEntitiesPage = postRepo.findByCreatorUserIdAndIsArchivedFalseAndPostTitleContainingIgnoreCase(userId,keyword, pageable);
        List<PostResponseDto> postListContent = postEntitiesPage.getContent().stream().map(postEntity -> mapToPostResponseDto(postEntity, currentUser)).toList();

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
        List<PostResponseDto> postListContent = postEntitiesPage.getContent().stream().map(postEntity -> mapToPostResponseDto(postEntity, currentUser)).toList();

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

        UserEntity user = securityService.getCurrentUser();

        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("createdAt").descending());

        Specification<PostEntity> specification = PostSpecification.getFilterSpec(request);

        Page<PostEntity> postPage = postRepo.findAll(specification, pageable);

        List<PostResponseDto> data = postPage.getContent().stream()
                .map(post-> mapToPostResponseDto( post, user)).toList();

        return new PagedResponse<>(
                data,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    private static final String CACHE_PREFIX = "newsfeed:user:";
    private static final long TTL_MINUTES = 3;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @Override
    public CursorResponse<PostResponseDto> getNewsfeed (String cursor, int size){
        UserEntity currentUser = securityService.getCurrentUser();

        Long currentUserId = (currentUser != null) ? currentUser.getUserId() : 0L;

        if(cursor== null){
            String cacheKey = CACHE_PREFIX + currentUserId;

            String cachedJson =(String) cacheService.get(cacheKey);
            if(cachedJson != null){
                try{
                    return objectMapper.readValue(cachedJson, new TypeReference<CursorResponse<PostResponseDto>>() {});
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse cache JSON for key: {}", cacheKey, e);
                }
            }
            CursorResponse<PostResponseDto> dbResult = getNewsfeedFromDb(currentUserId, null, size, currentUser);

            try{
                String jsonToCache = objectMapper.writeValueAsString(dbResult);
                cacheService.set(cacheKey, jsonToCache, TTL_MINUTES, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize newsfeed to JSON", e);
            }
            return dbResult;
        }
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

        List<PostResponseDto> postResponseDtoList = posts.stream()
                .map(post -> mapToPostResponseDto(post, currentUser))
                .toList();

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

        return mapToPostResponseDto(post, currentUser);
    }



    @Override
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
}
