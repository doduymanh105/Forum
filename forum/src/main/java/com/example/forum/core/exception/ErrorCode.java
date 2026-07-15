package com.example.forum.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_EXISTED(HttpStatus.CONFLICT, "Username is existed"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Not logged in or Invalid token"),
    DATABASE_ERROR(HttpStatus.CONFLICT, "Database integrity violation"),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "Username or password is incorrect!"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"You need to login to take this action!"),

    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "Chat not found"),
    CHAT_PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Not participated in this chat"),
    SELF_CHAT_CREATE(HttpStatus.BAD_REQUEST, "You can not create chat with yourself!"),
    CHAT_ACTION_FORBIDDEN(HttpStatus.FORBIDDEN, "You have no permission to take this action, be ADMIN to access"),


    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found!"),
    NOT_OWNED_MESSAGE(HttpStatus.FORBIDDEN, "You are not creator of this message!"),
    CHAT_MUST_HAVE_ADMIN(HttpStatus.CONFLICT, "Chat must have at least one admin"),
    LAST_ADMIN_CANNOT_LEAVE(HttpStatus.CONFLICT, "Cannot leave because you are the last admin"),
    GROUP_CHAT_FEATURE(HttpStatus.UNAUTHORIZED, "This feature is belong to group chat")
    ;

    private final HttpStatus status;
    private final String message;
}
