package com.backend.application.service;

import java.util.Map;

public interface RecaptchaService {

    boolean verifyToken(String token, String action);

    Map<String, String> getPublicClientConfig();
}
