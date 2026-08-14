package com.example.forum.feature.auth.controller;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.auth.dto.request.OtpInputRequest;
import com.example.forum.feature.auth.dto.request.PasswordConfirmRequest;
import com.example.forum.feature.auth.dto.response.TwoFactorResponse;
import com.example.forum.feature.auth.service.impl.TwoFactorServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Two Factor Auth API")
@RestController
@RequestMapping("/forum/user/2af")
@RequiredArgsConstructor
public class TwoFactorController {
    private final TwoFactorServiceImpl twoFactorService;

    @RateLimit(capacity = 5, time = 1)
    @GetMapping("/setup")
    public ResponseEntity<ApiResponse<TwoFactorResponse>> setup2af(
            @AuthenticationPrincipal UserEntity user
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get secret key successfully",
                        twoFactorService.enableTwoFactor(user.getEmail())
                )
        );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<List<String>>> Verify2af(
            @RequestBody OtpInputRequest request,
            @AuthenticationPrincipal UserEntity user
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                                "Your account is verified, Your backup code here!",
                                twoFactorService.verifyOtp(user.getEmail(), request.getOtpCode())
                        )
                );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/disable-2fa")
    public ResponseEntity<ApiResponse<?>> disable2fa(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody PasswordConfirmRequest request
    ) {

        twoFactorService.disable2fa(user,request.getPassword());

        return ResponseEntity.ok(
                        ApiResponse.success(
                                "2FA disabled successfully"
                        ));
    }

    @GetMapping("/isEnable")
    public ResponseEntity<ApiResponse<Boolean>> is2faEnable(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get 2fa status",
                        twoFactorService.is2faEnable()
                )
        );
    }
}
