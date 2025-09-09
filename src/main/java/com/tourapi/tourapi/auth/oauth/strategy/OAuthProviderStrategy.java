package com.tourapi.tourapi.auth.oauth.strategy;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

public interface OAuthProviderStrategy {

    String providerName();

    ResponseEntity<Void> startLogin(HttpServletRequest request);

    Map<String, Object> handleCallback(HttpServletRequest request, String code, String state);
}


