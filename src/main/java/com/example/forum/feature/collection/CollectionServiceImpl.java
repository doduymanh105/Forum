package com.example.forum.feature.collection;

import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.domain.*;
import com.example.forum.feature.collection.dto.*;
import com.example.forum.feature.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService{

    @Value("${collection-limit}")
    private int collectionLimit;

    private final SecurityUtils securityUtils;

    private final CollectionRepository collectionRepository;
    private final PostCollectionRepository postCollectionRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public CollectionResponseDto createCollection(CreateCollectionRequest request) {

        UserEntity user = securityUtils.getCurrentUser();

        int numberOfCreatedCollection = collectionRepository.countByUserEntityUserId(user.getUserId());

        if(numberOfCreatedCollection>collectionLimit){
            throw new AppException(ErrorCode.COLLECTION_LIMIT_REACH);
        }

        SaveCollection saveCollection = new SaveCollection();
        saveCollection.setName(request.getName());
        saveCollection.setUserEntity(user);
        saveCollection.setThumbnailUrl(request.getThumbnailUrl());
        saveCollection = collectionRepository.save(saveCollection);
        return mapToCollectionResponseDto(saveCollection);
    }


    @Override
    @Transactional
    public CollectionResponseDto updateCollection(UpdateCollectionRequest request) {

        SaveCollection existingCollection = collectionRepository.findByIdAndIsDeletedFalse(request.getCollectionId())
                .orElseThrow(()-> new AppException(ErrorCode.SAVE_COLLECTION_NOT_FOUND));

        if(request.getNewName()!= null){
            existingCollection.setName(request.getNewName());
        }
        if(request.getNewThumbnailUrl() != null){
            existingCollection.setThumbnailUrl(request.getNewThumbnailUrl());
        }
        collectionRepository.save(existingCollection);
        return mapToCollectionResponseDto(existingCollection);
    }

    @Override
    public List<CollectionResponseDto> getAllCollections() {
        UserEntity currentUser = securityUtils.getCurrentUser();

        List<SaveCollection> collectionList = collectionRepository.findAllByUserEntityUserId(currentUser.getUserId());
        return collectionList.stream().map(this::mapToCollectionResponseDto)
                .toList();
    }

    @Override
    public CollectionContentResponse getCollectionById(Long collectionId, String keyword) {

        UserEntity user = securityUtils.getCurrentUser();

        boolean collectionBelongToUser = collectionRepository.existsByIdAndUserEntityUserId(collectionId, user.getUserId());

        if(!collectionBelongToUser){
            throw new AppException(ErrorCode.NOT_OWN_COLLECTION);
        }
        if(keyword == null){
            keyword="";
        }

        List<PostEntity> postEntityList = postCollectionRepository.getPostsInCollection(collectionId, keyword );
        List<PostPreviewDto> data = postEntityList.stream().map(PostPreviewDto::mapToPostPreviewDto)
                .toList();

        return new CollectionContentResponse(
                data.size(),
                data
        );
    }

    @Override
    @Transactional
    public void deleteCollection(Long collectionId) {

        UserEntity currentUser = securityUtils.getCurrentUser();

        SaveCollection saveCollection = collectionRepository.findByIdAndIsDeletedFalse(collectionId)
                .orElseThrow(()-> new AppException(ErrorCode.SAVE_COLLECTION_NOT_FOUND));

        if(!saveCollection.getUserEntity().getUserId().equals(currentUser.getUserId())){
            throw new AppException(ErrorCode.NOT_OWN_COLLECTION);
        }

        saveCollection.setDeleted(true);

    }

    @Override
    @Transactional
    public CollectionResponseDto addPostToCollection(Long collectionId, Long postId) {
        UserEntity currentUser = securityUtils.getCurrentUser();

        SaveCollection saveCollection = collectionRepository.findByIdAndIsDeletedFalse(collectionId)
                .orElseThrow(()-> new AppException(ErrorCode.SAVE_COLLECTION_NOT_FOUND));

        if(!saveCollection.getUserEntity().getUserId().equals(currentUser.getUserId())){
            throw new AppException(ErrorCode.NOT_OWN_COLLECTION);
        }


        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        PostCollectionId postCollectionId = new PostCollectionId(collectionId,postId);

        if (postCollectionRepository.existsById(postCollectionId)) {
            throw new AppException(ErrorCode.POST_ALREADY_IN_COLLECTION);
        }
        PostCollection newPostCollection = new PostCollection();
        newPostCollection.setId(postCollectionId);
        newPostCollection.setPostEntity(post);
        newPostCollection.setSaveCollection(saveCollection);
        postCollectionRepository.save(newPostCollection);

        saveCollection.setTotalElements(saveCollection.getTotalElements()+1);

        return mapToCollectionResponseDto(saveCollection);
    }

    @Override
    @Transactional
    public CollectionResponseDto removePostFromCollection(Long collectionId, Long postId) {
        UserEntity currentUser = securityUtils.getCurrentUser();

        SaveCollection saveCollection = collectionRepository
                .findByIdAndIsDeletedFalse(collectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SAVE_COLLECTION_NOT_FOUND));

        if (!Objects.equals(
                saveCollection.getUserEntity().getUserId(),
                currentUser.getUserId()
        )) {
            throw new AppException(ErrorCode.NOT_OWN_COLLECTION);
        }

        PostCollectionId postCollectionId = new PostCollectionId(collectionId, postId);

        PostCollection postCollection = postCollectionRepository.findById(postCollectionId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_IN_COLLECTION));

        postCollectionRepository.delete(postCollection);

        saveCollection.setTotalElements(saveCollection.getTotalElements() - 1);

        return mapToCollectionResponseDto(saveCollection);
    }

    @Override
    public List<PostPreviewDto> searchSaved(String title) {
        UserEntity currentUser = securityUtils.getCurrentUser();

        if(title== null || title.isBlank()){
            title ="";
        }

        List<PostEntity> postList = postCollectionRepository.findSavedPostsByUserAndTitle(title, currentUser.getUserId());


        return postList.stream().map(PostPreviewDto::mapToPostPreviewDto).toList();
    }

    public CollectionResponseDto mapToCollectionResponseDto(SaveCollection saveCollection){
        return CollectionResponseDto.builder()
                .id(saveCollection.getId())
                .name(saveCollection.getName())
                .thumbnailUrl(saveCollection.getThumbnailUrl())
                .totalElements(saveCollection.getTotalElements())
                .createdAt(saveCollection.getCreatedAt())
                .build();
    }
}
