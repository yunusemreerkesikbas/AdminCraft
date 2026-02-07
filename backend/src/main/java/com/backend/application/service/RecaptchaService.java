package com.backend.application.service;

public interface RecaptchaService {

    boolean verifyToken(String token, String action);

    boolean isEnabled();
}
