package com.tourapi.tourapi.auth.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.tourapi.tourapi.auth.dto.TokenResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.auth.success-redirect-url:/app}")
    private String successRedirectUrl;

    @Value("${app.auth.onboarding-redirect-url:/app/onboarding}")
    private String onboardingRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        TokenResponse tokenResponse = oauth2User.getTokenResponse();

        log.info("OAuth2 로그인 성공: provider={}, email={}", 
            oauth2User.getRegistrationId(), oauth2User.getEmail());

        // 쿠키로 토큰 전달
        setTokenCookies(response, tokenResponse);

        // 가입완료 여부에 따라 리다이렉트 분기
        String targetUrl = tokenResponse.isSignUpCompleted ? successRedirectUrl : onboardingRedirectUrl;
        response.sendRedirect(targetUrl);
    }

    private void setTokenCookies(HttpServletResponse response, TokenResponse tokenResponse) {
        // Access Token 쿠키 설정
        Cookie accessTokenCookie = new Cookie("access_token", tokenResponse.accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // HTTPS에서만 전송
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(30 * 60); // 30분
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 설정 (있는 경우)
        if (tokenResponse.refreshToken != null) {
            Cookie refreshTokenCookie = new Cookie("refresh_token", tokenResponse.refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            response.addCookie(refreshTokenCookie);
        }

        // 사용자 정보 쿠키 설정 (선택사항)
        Cookie emailCookie = new Cookie("user_email", 
            URLEncoder.encode(tokenResponse.email, StandardCharsets.UTF_8));
        emailCookie.setHttpOnly(false); // 프론트엔드에서 접근 가능
        emailCookie.setSecure(true);
        emailCookie.setPath("/");
        emailCookie.setMaxAge(30 * 60); // 30분
        response.addCookie(emailCookie);
    }
}
