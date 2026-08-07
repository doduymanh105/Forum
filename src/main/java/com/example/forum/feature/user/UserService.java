package com.example.forum.feature.user;

import com.example.forum.feature.user.dto.ChangePasswordRequest;
import com.example.forum.feature.user.dto.UserUpdateRequest;
import com.example.forum.common.dto.PagedResponse;
import com.example.forum.feature.user.dto.UserResponseDto;
import com.example.forum.domain.UserEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {

    @PreAuthorize("isAuthenticated()")
    UserResponseDto getCurrentUser(UserEntity userEntity);

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    UserResponseDto getUserInfor(Long id);


    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    void softDeleteUser(Long id);

    @PreAuthorize("hasRole('ADMIN')")
    List<UserResponseDto> getAllUsers();

    @PreAuthorize("hasRole('ADMIN')")
    PagedResponse<UserResponseDto> getUsers(
            int page,
            int size,
            String sortBy,
            String sortDirect,
            String keyword
    );

    @PreAuthorize("hasRole('USER') and #id==authentication.principal.userId")
    UserResponseDto updateUser(Long id, UserUpdateRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    void hardDeleteUser(Long id);

    @PreAuthorize("hasRole('USER') and #id==authentication.principal.userId")
    void changePassword(Long id, ChangePasswordRequest request);

    UserResponseDto updateProfilePicture();
    UserResponseDto updateUserInfo();
}
