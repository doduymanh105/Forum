package com.example.forum.feature.user;

import com.example.forum.common.dto.PagedResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.user.dto.ChangePasswordRequest;
import com.example.forum.feature.user.dto.UserResponseDto;
import com.example.forum.feature.user.dto.UserUpdateRequest;
import com.example.forum.common.dto.ApiResponse;
import com.example.forum.domain.UserEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User API")
@RestController
@RequestMapping("/forum/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(Authentication authentication) {
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                "Current user",
                userService.getCurrentUser(userEntity)
        ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                "get user by id",
                        userService.getUserInfor(id)
        ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponseDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirect,
            @RequestParam(defaultValue = "") String keyword
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Get all users success",
                userService.getUsers(page, size, sortBy,sortDirect, keyword)
        ));
    }

    @RateLimit(capacity = 100, time = 1)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(
                "get all users",
                userService.getAllUsers()
        ));
    }

    @RateLimit(capacity = 3, time = 1)
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword (
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
            ) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password is successfully changed"
        ));
    }

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Update user successfully!",
                userService.updateUser(id, request)
        ));
    }

    @RateLimit(capacity = 20, time = 1)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> hardDeleteUser(@PathVariable Long id) {
        userService.hardDeleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User is permanently deleted"
                )
        );
    }

    @RateLimit(capacity = 20, time = 1)
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(
                "User is temporary deleted"
        ));
    }
}
