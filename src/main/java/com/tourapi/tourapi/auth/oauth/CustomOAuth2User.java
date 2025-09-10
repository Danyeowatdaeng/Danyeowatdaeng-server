package com.tourapi.tourapi.auth.oauth;

import com.tourapi.tourapi.auth.dto.TokenResponse;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final TokenResponse tokenResponse;
    private final String registrationId;

    public CustomOAuth2User(Map<String, Object> attributes, TokenResponse tokenResponse, String registrationId) {
        this.attributes = attributes;
        this.tokenResponse = tokenResponse;
        this.registrationId = registrationId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return tokenResponse.name;
    }

    public String getEmail() {
        return tokenResponse.email;
    }

    public String getAccessToken() {
        return tokenResponse.accessToken;
    }

    public String getRefreshToken() {
        return tokenResponse.refreshToken;
    }

    public boolean isSignUpCompleted() {
        return tokenResponse.isSignUpCompleted;
    }
}
