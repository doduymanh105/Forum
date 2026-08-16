package com.example.forum.feature.auth.controller;


import com.example.forum.common.dto.ApiResponse;
import com.example.forum.core.annotation.RateLimit;
import com.example.forum.feature.auth.dto.request.*;
import com.example.forum.feature.auth.dto.response.AuthenticationResponse;
import com.example.forum.feature.auth.dto.response.VerifyOtpResponse;
import com.example.forum.feature.auth.service.VerificationService;
import com.example.forum.feature.auth.service.impl.AuthenticationServiceImpl;
import com.example.forum.feature.user.dto.UserSummaryDto;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.admin.AdminServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API")
@RestController
@RequestMapping("/forum/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationService;
    private final VerificationService verificationService;
    private final AdminServiceImpl adminService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Not authenticated", null));
        }

        UserEntity user = (UserEntity) authentication.getPrincipal();

        UserSummaryDto userDto = new UserSummaryDto(
                user.getUserId(),
                user.displayUsername(),
                user.getEmail(),
                user.getAvatarUrl()
        );

        return ResponseEntity.ok(ApiResponse.success("User fetched", userDto));
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping(value = "/register")
    public ResponseEntity<ApiResponse<UserSummaryDto>> register (
            @Valid @RequestBody RegisterRequest request
            ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Successfully created, you need to verify your email",
                        authenticationService.register(request)
        ));
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyEmail (@Valid
            @RequestBody VerifyEmailRequest request
    ) {
        return ResponseEntity.ok( ApiResponse.success(
               "successfully, you can login now",
                authenticationService.verifyCode(request.getEmail(), request.getCode())
        ));
    }

    @RateLimit(capacity = 5, time = 1)
    @PatchMapping("/resend-verification-code")
    public ResponseEntity<ApiResponse<?>> resendVerificationCode(
            @Valid @RequestBody ResendEmailRequest request
            ){
        verificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "Resent verification code!"
        ));
    }


    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticateUser (
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Logging in successfully",
                authenticationService.authenticate(request)
        ));
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/createAdmin")
    public ResponseEntity<ApiResponse<?>> createAdmin (
            @Valid @RequestBody RegisterRequest request
    ) {
        adminService.createAdmin(request);
        return ResponseEntity.ok(
                ApiResponse.created(
                        "Successfully create ADMIN"
                )
        );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Refreshed token",
                        authenticationService.refreshToken(request.getRefreshToken())
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestBody LogoutRequest request,
            HttpServletRequest httpServletRequest
    ){
        String authHeader = httpServletRequest.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")){
            accessToken = authHeader.substring(7);
        }
        authenticationService.logout(request, accessToken);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successfully"
                )
        );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword( @RequestBody ForgotPasswordRequest request){
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Your verification code has been send to your email"
                )
        );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request){
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reset Password Successfully",
                        null
                )
        );
    }

    @RateLimit(capacity = 5, time = 1)
    @PostMapping("/2fa-login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> verify2faLogin (
            @Valid @RequestBody Verify2faLoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Logging in successfully",
                authenticationService.verifyTwoFactorLogin(request.getEmail(),request.getOtpCode(), request.getDeviceId())
        ));
    }
}
