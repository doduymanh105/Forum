package com.example.forum.feature.auth.service;

public interface LoginAttemptService {
    boolean isLocked(String email);
    void loginFail(String email);
    void loginSucceeded(String email);
}
