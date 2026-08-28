package com.example.forum.core.exception;

import com.example.forum.common.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.common.OpenAiApiClientErrorException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // validation error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException (
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        "validation failed",
                        errors
                )
        );
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtExpirationException(
            ExpiredJwtException ex
    ){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse
                        .error(HttpStatus.UNAUTHORIZED.value(), "Token expiration"));
    }

    @ExceptionHandler({io.jsonwebtoken.MalformedJwtException.class, io.jsonwebtoken.security.SignatureException.class})
    public ResponseEntity<ApiResponse<?>> handleInvalidJwtException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid token"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage())
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(ResponseStatusException ex){
        return ResponseEntity.status(ex.getStatusCode()).body(
                ApiResponse.error(ex.getStatusCode().value(), ex.getReason())
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage())
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailAlreadyUsed(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error(
                        HttpStatus.CONTINUE.value(),
                        "Email was used!",
                        ex.getMessage()
                )
        );
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access denied")
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex){
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body( ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(NotLoggedInException.class)
    public ResponseEntity<?> handleNotLoggedInException(NotLoggedInException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        e.getMessage()
                ));

    }

    @ExceptionHandler(OtpVerificationException.class)
    public ResponseEntity<?> handleOtpVerification(OtpVerificationException e){
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeException(MaxUploadSizeExceededException maxUploadSizeExceededException){

        ErrorCode errorCode = ErrorCode.UPLOAD_LIMIT_EXCEEDED;

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getStatus().value());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException e){
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getStatus().value()
                                , e.getMessage()
                        )
                );
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Database integrity violation occurred.";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("users_user_name_key")) {
                message = "Username already exists. Please choose another one.";
            } else if (ex.getMessage().contains("users_pkey")) {
                message = "System ID conflict. Please try again.";
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        HttpStatus.CONTINUE.value(),
                        message
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleEnumError(
            HttpMessageNotReadableException ex
    ){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        "Input must be in allowed range"
                        ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleRateLimitExceeded(
            RateLimitExceededException ex
    ){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(ex.getWaitForRefillSeconds()))
                .body(
                        ApiResponse.error(
                                HttpStatus.TOO_MANY_REQUESTS.value(),
                                ex.getMessage())
                );
    }

    private static final String LLM_COMMUNICATION_ERROR = "Unable to communicate with the configured LLM. Please try again later.";

    @ExceptionHandler(OpenAiApiClientErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleOpenAiException(OpenAiApiClientErrorException exception) {

        log.error("OpenAI returned an error: ", exception);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        LLM_COMMUNICATION_ERROR));
    }

}
