package com.example.forum.feature.auth.controller;

import com.example.forum.common.dto.ApiResponse;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.auth.dto.request.OtpInputRequest;
import com.example.forum.feature.auth.dto.request.PasswordConfirmRequest;
import com.example.forum.feature.auth.service.impl.TwoFactorServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum/user/2af")
@RequiredArgsConstructor
public class TwoFactorController {
    private final TwoFactorServiceImpl twoFactorService;

    @GetMapping("/setup")
    public ResponseEntity<?> setup2af(
            @AuthenticationPrincipal UserEntity user
            ){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "get secret key successfully",
                        twoFactorService.enableTwoFactor(user.getEmail())
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<?> Verify2af(
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

    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(
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
    public ResponseEntity<?> is2faEnable(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get 2fa status",
                        twoFactorService.is2faEnable()
                )
        );
    }
}
