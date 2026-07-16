package com.example.forum.feature.user;

import com.example.forum.feature.user.dto.ChangePasswordRequest;
import com.example.forum.feature.user.dto.UserUpdateRequest;
import com.example.forum.common.dto.ApiResponse;
import com.example.forum.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                "Current user",
                userService.getCurrentUser(userEntity)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                "get user by id",
                        userService.getUserInfor(id)
        ));
    }

    @GetMapping
    public ResponseEntity<?> getUsers(
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


    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(
                "get all users",
                userService.getAllUsers()
        ));
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword (
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request
            ) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password is successfully changed"
        ));
    }

    @PatchMapping("/{id}/update")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Update user successfully!",
                userService.updateUser(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hardDeleteUser(@PathVariable Long id) {
        userService.hardDeleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User is permanently deleted"
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(
                "User is temporary deleted"
        ));
    }
}
