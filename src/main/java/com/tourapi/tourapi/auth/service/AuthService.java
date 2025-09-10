package com.tourapi.tourapi.auth.service;

import com.tourapi.tourapi.auth.dto.TokenResponse;
import com.tourapi.tourapi.member.enums.Role;

public interface AuthService {

    record TokenPair(String accessToken, String refreshToken, String sessionId, String familyId) {}

    TokenPair issueTokensOnLogin(Long id, String email, String name, Role role, boolean isSignUpCompleted);
    
    TokenResponse socialLogin(String provider, String token);
    
    TokenResponse oauthCallbackLogin(String provider, String providerUserId, String email, String name);
    
    TokenResponse refreshToken(String oldRefreshToken);
}
