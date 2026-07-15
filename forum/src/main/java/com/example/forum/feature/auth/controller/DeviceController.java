package com.example.forum.feature.auth.controller;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.domain.UserEntity;
import com.example.forum.core.security.jwt.JWTService;
import com.example.forum.feature.auth.service.impl.AuthenticationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/user/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final JWTService jwtService;
    private final AuthenticationServiceImpl authenticationService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/device-list")
    public ResponseEntity<?> getAllDevices(
            @AuthenticationPrincipal UserEntity currentUser,
            HttpServletRequest request
    ) {
        String currentToken = request.getHeader("Authorization").substring(7);
        String currentDeviceId = jwtService.extractDeviceId(currentToken);
        System.out.println(currentUser.toString());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Get All Devices Successfully",
                        authenticationService.getAllDevice(currentUser, currentDeviceId)
                )
        );
    }

//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{deviceId}/revoke")
    public ResponseEntity<?> revokeADevice(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable String deviceId
    ){
        authenticationService.revokeDevice(user, deviceId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Logout successfully",
                        null
                )
        );
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> revokeAllDevices(
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        authenticationService.revokeAllDevice(currentUser);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Logout successfully",
                        null
                )
        );
    }
}
