package com.example.forum.feature.user;

import com.example.forum.common.utils.SecurityUtils;
import com.example.forum.core.exception.AppException;
import com.example.forum.core.exception.ErrorCode;
import com.example.forum.feature.user.dto.ChangePasswordRequest;
import com.example.forum.feature.user.dto.UserUpdateRequest;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.feature.user.dto.UserResponseDto;
import com.example.forum.domain.FollowId;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.follow.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto getCurrentUser(UserEntity userEntity) {
        return mapToUserResponseDto(userEntity);
    }

    private UserResponseDto mapToUserResponseDto(UserEntity user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.displayUsername())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles())
                .bio(user.getBio())
                .socialPlatforms(user.getSocialLinks())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponseDto getUserInfo(Long targetUserid) {
        UserEntity user = userRepository.findById(targetUserid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(user.getIsDeleted()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        int followerCount = followRepository.countFollowers(targetUserid);
        int followingCount = followRepository.countFollowings(targetUserid);
        boolean isFollowing = false;
        UserEntity currentUser = securityUtils.getCurrentUserOrNull();

        if(currentUser!= null && currentUser.getUserId() !=targetUserid ) {
            FollowId checkId = new FollowId(currentUser.getUserId(),targetUserid);

            isFollowing = followRepository.existsById(checkId);
        }
        UserResponseDto responseDto = mapToUserResponseDto(user);
        responseDto.setFollowerCount((long)followerCount);
        responseDto.setFollowingCount((long)followingCount);
        responseDto.setIsFollowing(isFollowing);

        return responseDto;
    }




    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<UserResponseDto> getUsers(int page, int size, String sortBy, String sortDirect, String keyword) {

        Sort sort = sortDirect.equalsIgnoreCase("asc")
                ? Sort.by(ASC, sortBy)
                : Sort.by(DESC, sortBy);

        Pageable pageable= PageRequest.of(page,size,sort);


        if (keyword == null) keyword = "";

        Page<UserEntity> usersPage = userRepository.findByIsDeletedFalseAndUserNameContainingIgnoreCase(keyword, pageable);
        List<UserResponseDto> UserPageContent= usersPage.getContent().stream().map(this::mapToUserResponseDto).toList();
        return new PagedResponse<>(
                UserPageContent,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(request.getAvatarUrl()!= null && !request.getAvatarUrl().isBlank()){
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getUsername()!=null && !request.getUsername().isBlank()) {
            user.setUserName(request.getUsername());
        }
        if (request.getBio()!=null && !request.getBio().isBlank()) {
            user.setBio(request.getBio());
        }
        if(request.getSocialLinks()!= null && !request.getSocialLinks().isEmpty()){
            request.getSocialLinks().forEach((socialPlatform, url) ->{
                if(!socialPlatform.isValidUrl(url)){
                    throw new AppException(ErrorCode.INVALID_PLATFORM_URL);
                }
            } );
            user.setSocialLinks(request.getSocialLinks());
        }
        return mapToUserResponseDto(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsDeleted(true);
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long id) {
        UserEntity user= userRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getUserPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
    }

}
