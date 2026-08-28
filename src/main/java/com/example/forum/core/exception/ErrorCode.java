package com.example.forum.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Authentication & Authorization(401, 403)
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Not logged in or Invalid token."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized access. Please login specifically."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "Action can be done when logged in."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "Invalid username or password."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to access this resource."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password."),
    OLD_PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "The old password provided is incorrect."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "Password is required."),


    // ACCOUNT STATUS (403 Locked/Disabled)
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Your account is locked, please contact supports."),
    ACCOUNT_LOCKED_DUE_TO_OVER_ATTEMPTS(HttpStatus.FORBIDDEN, "Your account has been locked due to multiple failed login attempts."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Your account has been disabled. Please contact support."),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Your account is not verified."),


    // TOKEN & OTP & 2FA (400, 401, 403)
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid or expired token."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired. Please refresh your token."),
    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh Token is missing!"),
    REFRESH_TOKEN_INVALID(HttpStatus.FORBIDDEN, "Invalid Refresh Token."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "Refresh Token Expired."),
    REFRESH_TOKEN_INVALID_REVOKED(HttpStatus.FORBIDDEN, "Refresh Token has been revoked (Logout)."),
    INVALID_RESET_PASSWORD_TOKEN(HttpStatus.BAD_REQUEST, "Invalid or expired reset password token."),

    REQUIRE_TWO_FACTOR(HttpStatus.UNAUTHORIZED, "Two-factor authentication is required."),
    TWO_FACTOR_NOT_ENABLED(HttpStatus.BAD_REQUEST, "2FA is not enabled for this user."),
    CODE_2FA_EXPIRED(HttpStatus.BAD_REQUEST, "Your code is expired or unavailable. Please retake enable/setup step."),

    OTP_INVALID(HttpStatus.BAD_REQUEST, "Invalid or expired OTP."),
    WRONG_OTP_CODE(HttpStatus.BAD_REQUEST, "Wrong code, please re-enter."),
    OTP_LIMIT_REACHED(HttpStatus.TOO_MANY_REQUESTS, "Maximum OTP attempts reached. Please request a new one."),


    // USER & ROLE (400, 404, 409)
    USER_EXISTED(HttpStatus.CONFLICT, "Username is existed"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username is already taken."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "Username or password is incorrect!"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email address is already in use."),


    // POST & COLLECTION (400, 403, 404, 409)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "Post not found."),
    NO_PERMISSION_EDIT_POST(HttpStatus.FORBIDDEN, "You do not have permission to edit this post."),

    SAVE_COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Collection not found."),
    COLLECTION_LIMIT_REACH(HttpStatus.CONFLICT, "Maximum number of collections reached."),
    NOT_OWN_COLLECTION(HttpStatus.FORBIDDEN, "Do not own this collection."),
    POST_ALREADY_IN_COLLECTION(HttpStatus.CONFLICT, "Post already saved."),
    POST_NOT_IN_COLLECTION(HttpStatus.BAD_REQUEST, "Post not in collection."),


    // COMMENT & FOLLOW (400, 403, 404)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment not found."),
    EDIT_OWN_COMMENT(HttpStatus.FORBIDDEN, "You can only edit your own comment!"),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Parent comment not found."),

    CANT_FOLLOW_YOURSELF(HttpStatus.BAD_REQUEST, "You cannot follow yourself!"),
    ALREADY_FOLLOWED(HttpStatus.CONFLICT, "You already followed this user."),
    HAVE_NOT_FOLLOW(HttpStatus.BAD_REQUEST, "You have not followed this user."),
    USER_HAVE_NOT_FOLLOW(HttpStatus.BAD_REQUEST, "This user is not following you."),


    // FILE & MEDIA (400, 403, 404, 413, 500)
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "File cannot be empty."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the maximum limit of 5MB."),
    FILE_EXTENSION_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "Unsupported file format. Only jpg, jpeg, png, webp, and gif are allowed."),
    FILE_NOT_VALID_IMAGE(HttpStatus.BAD_REQUEST, "Invalid file content or unsupported format. Please select a valid image."),
    MAX_BATCH_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "You can only upload up to the allowed maximum images at a time."),
    UPLOAD_LIMIT_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "Upload failed. Total request exceeds limits."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed. Please try again later."),

    MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "Media not found."),
    MEDIA_NOT_BELONG_TO_POST(HttpStatus.BAD_REQUEST, "Media does not belong to the selected post."),
    NO_PERMISSION_TO_DELETE_MEDIA(HttpStatus.FORBIDDEN, "You do not have permission to delete this image."),


    // CHAT & MESSAGE (400, 403, 404, 409)
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "Chat not found."),
    CHAT_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Not participated in this chat."),
    SELF_CHAT_CREATE(HttpStatus.BAD_REQUEST, "You cannot create a chat with yourself!"),
    CHAT_ACTION_FORBIDDEN(HttpStatus.FORBIDDEN, "You have no permission to take this action, be ADMIN to access."),
    CHAT_MUST_HAVE_ADMIN(HttpStatus.CONFLICT, "Chat must have at least one admin."),
    LAST_ADMIN_CANNOT_LEAVE(HttpStatus.CONFLICT, "Cannot leave because you are the last admin."),
    GROUP_CHAT_FEATURE(HttpStatus.BAD_REQUEST, "This feature belongs to group chat."),

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found!"),
    NOT_OWNED_MESSAGE(HttpStatus.FORBIDDEN, "You are not the creator of this message!"),


    // SYSTEM, & AI & OTHERS
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Database integrity violation."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email. Please try again."),
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Device not found."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification not found."),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Role not found"),

    POST_CONTENT_MIN_LENGTH(HttpStatus.BAD_REQUEST, "Post content is too short."),
    POST_CONTENT_MAX_LENGTH(HttpStatus.BAD_REQUEST, "Post content is too long."),
    POST_CONTENT_TOXIC(HttpStatus.BAD_REQUEST, "Post content is toxic.")






    ;


    private final HttpStatus status;
    private final String message;

}
