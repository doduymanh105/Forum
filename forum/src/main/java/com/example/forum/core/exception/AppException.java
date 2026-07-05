package com.example.forum.core.exception;


import com.example.forum.entity.Enum.ErrorCode;

public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode=errorCode;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
