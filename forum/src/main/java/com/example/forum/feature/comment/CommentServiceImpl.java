package com.example.forum.feature.comment;

import com.example.forum.common.constant.MessageConstants;
import com.example.forum.common.dto.CursorResponse;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.domain.Enum.VoteType;
import com.example.forum.feature.comment.dto.*;
import com.example.forum.domain.CommentEntity;
import com.example.forum.domain.Enum.EventType;
import com.example.forum.domain.NotificationEvent;
import com.example.forum.domain.PostEntity;
import com.example.forum.domain.UserEntity;
import com.example.forum.core.exception.ResourceNotFoundException;
import com.example.forum.feature.post.PostRepository;
import com.example.forum.feature.user.UserRepository;
import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.feature.user.dto.UserSummaryDto;
import com.example.forum.feature.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final SecurityUtils securityService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponseDto createComment(Long postId, CreateCommentRequest request) {

        PostEntity post= postRepository.findByPostId(postId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        UserEntity user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND));

        CommentEntity comment= CommentEntity.builder()
                .postEntity(post)
                .userEntity(user)
                .commentContent(request.getContent())
                .isDeleted(false)
                .build();

        String newPath;
        if(request.getParentPath() == null
                || request.getParentPath().trim().isEmpty()
                || request.getParentId()==0
        ){
            newPath= "/";
            comment.setParentId(null);
        } else {
            CommentEntity parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));
            comment.setParentId(request.getParentId());
            newPath = request.getParentPath() + request.getParentId() +"/";
        }
        comment.setCommentPath(newPath);
        CommentEntity savedComment = commentRepository.save(comment);

        String content = request.getContent();
        String preview = content.length() <= 20
                ? content
                : content.substring(0, 20);

        // Notification
        UserEntity postOwner = savedComment.getPostEntity().getCreator();

        if(request.getParentPath() == null
                || request.getParentPath().trim().isEmpty()
                || request.getParentId()==0) {
            NotificationEvent notificationEvent = notificationService.createEvent(
                    EventType.NEW_COMMENT,
                    currentUser,
                    preview,
                    comment.getCommentId(),
                    "COMMENT"
            );
            if (!postOwner.getUserId().equals(currentUser.getUserId())) {
                notificationService.notifySpecificUser(postOwner, notificationEvent);
            }
        } else {
            NotificationEvent notificationEvent = notificationService.createEvent(
                    EventType.NEW_REPLY,
                    currentUser,
                    preview,
                    comment.getCommentId(),
                    "COMMENT"
            );
            CommentEntity parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));
            if(!postOwner.getUserId().equals(currentUser.getUserId())){
                notificationService.notifySpecificUser(post.getCreator(), notificationEvent);
            }
            if(!notificationEvent.getCreatedBy().getUserId().equals(parentComment.getUserEntity().getUserId())){
                notificationService.notifySpecificUser(parentComment.getUserEntity(), notificationEvent);
            }
        }

        postRepository.incrementCommentCount(postId);

        return mapToCommentResponseDto(comment);
    }

    @Override
    public List<CommentDto> getListOfCommentByPath(Long postId, String path) {
        List<CommentEntity> comments = commentRepository.findByPostIdAndPathLike(postId, path);

        return comments.stream()
                .map(this::mapToCommentDto)
                .toList();
    }

    @Override
    public CursorResponse<CommentDto> getListOfRootCommentAndCountReplyComment(Long postId, String cursor, String sortBy , int size) {

        Long currentUserId = securityService.getCurrentUser().getUserId();

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND);
        }

        Pageable pageable = PageRequest.ofSize(size+1);
        List<CommentProjection> rows;

        if("top".equalsIgnoreCase(sortBy)){
            Long cursorId = null;
            Long cursorScore = null;

            if(cursor!= null && cursor.contains("_")){
                String[] parts = cursor.split("_");
                cursorScore = Long.parseLong(parts[0]);
                cursorId = Long.parseLong(parts[1]);
            }

            rows = commentRepository.findTopRootComments(postId, currentUserId, cursorScore, cursorId, pageable);
        } else {
            Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;
            rows = commentRepository.findNewestRootComments(postId, currentUserId, cursorId, pageable);
        }

        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows.remove(rows.size() - 1);
        }

        String nextCursor = null;
        if(!rows.isEmpty()){
            CommentProjection lastItem = rows.get(rows.size() - 1);
            if("top".equalsIgnoreCase(sortBy)){
                nextCursor = lastItem.getScore() + "_" + lastItem.getCommentId();
            } else {
                nextCursor = String.valueOf(lastItem.getCommentId());
            }
        }

        List<CommentDto> data = rows.stream()
                .map(this::mapProjectionToDto)
                .toList();

        return new CursorResponse<>(data, nextCursor, hasNext);
    }

    private CommentDto mapProjectionToDto(CommentProjection row) {

        return CommentDto.builder()
                .commentId(row.getCommentId())
                .commentContent(row.getCommentContent())
                .commentPath(row.getCommentPath())
                .parentId(row.getParentId())
                .isDeleted(row.getIsDeleted())
                .createdAt(row.getCreatedAt())
                .updatedAt(row.getUpdatedAt())
                .userInfor(new UserSummaryDto(
                        row.getUserId(),
                        row.getUsername(),
                        row.getEmail(),
                        row.getAvatarUrl()
                ))
                .postId(row.getPostId())
                .upvotes(row.getUpvotes())
                .downvotes(row.getDownvotes())
                .score(row.getScore())
                .userVote(row.getUserVote())
                .replyCount(row.getReplyCount())
                .build();
    }

    @Override
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException(MessageConstants.EDIT_OWN_COMMENT);
        }

        comment.setCommentContent(request.getUpdatedContent());
        commentRepository.save(comment);

        return mapToCommentDto(comment);
    }

    @Override
    @Transactional
    public void softDeletedComment(Long commentId) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));

        PostEntity post = comment.getPostEntity();

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException(MessageConstants.EDIT_OWN_COMMENT);
        }
        comment.setIsDeleted(true);
        postRepository.decrementCommentCount(post.getPostId());
        commentRepository.save(comment);
    }

    @Override
    public void hardDeletedComment(Long commentId) {
        CommentEntity comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));

        UserEntity currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        if(currentUserId != comment.getUserEntity().getUserId()) {
            throw new AccessDeniedException(MessageConstants.EDIT_OWN_COMMENT);
        }
        commentRepository.delete(comment);
    }

    @Override
    public PagedResponse<CommentDto> getListOfChildCommentAndCountReplyComment(Long postId, Long parentId, int page, int size) {

        Long currentUserId = securityService.getCurrentUser().getUserId();

        int pageIndex = (page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size);

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND);
        }

        Page<CommentProjection> pageCommentProjection = commentRepository.findChildCommentsWithReplyCountByPostId(postId, parentId, currentUserId, pageable);

        List<CommentDto> data= pageCommentProjection.getContent().stream()
                .map(this::mapProjectionToDto)
                .toList();
        return new PagedResponse<>(
                data,
                pageCommentProjection.getNumber()+1,
                pageCommentProjection.getSize(),
                pageCommentProjection.getTotalElements(),
                pageCommentProjection.getTotalPages(),
                pageCommentProjection.isLast()
        );

    }

    @Override
    public PagedResponse<CommentResponseDto> getTopLevelComments(Long postId, Pageable pageable) {
        PostEntity post = postRepository.findByPostId(postId).orElseThrow(()-> new ResourceNotFoundException(MessageConstants.POST_NOT_FOUND));
        Page<CommentEntity> commentPage = commentRepository
                .findByPostEntity_PostIdAndParentIdIsNull(postId, pageable);


        Page<CommentResponseDto> dtoPage = commentPage.map(this::mapToCommentResponseDto);

        return new PagedResponse<>(
                dtoPage.getContent(),      // List<CommentResponseDto>
                dtoPage.getNumber(),       // Số trang hiện tại
                dtoPage.getSize(),         // Kích thước trang
                dtoPage.getTotalElements(),// Tổng số comment (cấp 1)
                dtoPage.getTotalPages(),   // Tổng số trang
                dtoPage.isLast()           // Trang cuối?
        );
    }


    @Override
    public List<CommentResponseDto> getReplies(Long parentId) {
        if (!commentRepository.existsById(parentId)) {
            throw new ResourceNotFoundException(MessageConstants.PARENT_COMMENT_NOT_FOUND);
        }

        List<CommentEntity> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(parentId);

        return replies.stream()
                .map(this::mapToCommentResponseDto)
                .toList();
    }

    @Override
    public CommentContextResponse getCommentContext(Long commentId) {
        UserEntity user = securityService.getCurrentUser();

        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(()-> new ResourceNotFoundException(MessageConstants.COMMENT_NOT_FOUND));

        List<Long> relatedCommentIds= new ArrayList<>();
        String commentPath= commentEntity.getCommentPath();
        if(commentEntity.getParentId()!= null && !commentPath.equals("/")){
            String[] path = commentEntity.getCommentPath().substring(1).split("/");
            for(String s: path){
                if (!s.isEmpty()) {
                    relatedCommentIds.add(Long.parseLong(s));
                }
            }
        }
        relatedCommentIds.add(commentId);
        List<CommentProjection> commentProjections = commentRepository.findCommentContextByIds(relatedCommentIds, user.getUserId());
        List<CommentDto> commentDtos = commentProjections.stream()
                .map(this::mapProjectionToDto)
                .sorted(Comparator.comparingInt( c -> c.getCommentPath().length()))
                .toList();

        return CommentContextResponse.builder()
                .postId(commentEntity.getPostEntity().getPostId())
                .highlightCommentId(commentId)
                .threadContext(commentDtos)
                .build();
    }

    private CommentResponseDto mapToCommentResponseDto(CommentEntity comment){
        if (comment == null) return null;
        String path = comment.getCommentPath();
        String parentPath = null;
        int depth = 0;

        if (path != null && !path.isEmpty()) {
            // depth = số lượng dấu '/'
            depth = StringUtils.countMatches(path, "/");

            // Nếu depth > 1 (ví dụ: "/123/"), parentPath là null
            // Nếu depth > 2 (ví dụ: "/123/456/"), parentPath là "/123/"
            if (depth > 1) {
                // Tìm vị trí dấu '/' thứ 2 từ cuối lên
                int lastSlash = path.lastIndexOf('/', path.length() - 2);
                if (lastSlash >= 0) {
                    parentPath = path.substring(0, lastSlash + 1);
                }
            }
        }
        UserEntity user = comment.getUserEntity();

        return CommentResponseDto.builder()
                .id(comment.getCommentId())
                .postId(comment.getPostEntity().getPostId())
                .ownerId(user != null ? user.getUserId() : null)

                .content(comment.getCommentContent())
                .isArchived(comment.getIsDeleted())

                .createdAt(comment.getCreatedAt() != null ? comment.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)

                .path(path)
                .depth(depth)
                .parentPath(parentPath)
                .childCommentCount(commentRepository.countByParentId(comment.getCommentId())) // Tạm thời set 0
                .children(Collections.emptyList()) // Luôn trả về mảng rỗng

                .upvote(0L)
                .downvote(0L)
                .userVoteType(null)

                .owner(mapToOwnerCommentDto(user))
                .build();
    }

    private CommentOwnerDto mapToOwnerCommentDto(UserEntity owner){
        if (owner == null) return null;

        return CommentOwnerDto.builder()
                .id(owner.getUserId())
                .name(owner.displayUsername())
                .photo(owner.getAvatarUrl())

                .createdAt(owner.getCreatedAt() != null ? owner.getCreatedAt().atOffset(ZoneOffset.UTC) : null)

                .slug(owner.getSlug())
                .point(null)
                .bio(owner.getBio())
                .build();
    }




    private CommentDto mapToCommentDto(CommentEntity comment){
        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getCommentContent())
                .commentPath(comment.getCommentPath())
                .parentId(comment.getParentId())
                .isDeleted(comment.getIsDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replyCount(0L)
                .userInfor(mapToUserSummary(comment.getUserEntity()))
                .postId(comment.getPostEntity().getPostId())
                .build();
    }
    private UserSummaryDto mapToUserSummary(UserEntity user){
        return UserSummaryDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.displayUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

}

