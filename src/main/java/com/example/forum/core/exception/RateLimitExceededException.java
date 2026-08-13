package com.example.forum.core.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long waitForRefillSeconds;

    public RateLimitExceededException(long waitForRefillSeconds) {
        super("Too many requests. Please try again later." + waitForRefillSeconds);
        this.waitForRefillSeconds = waitForRefillSeconds;
    }
}
