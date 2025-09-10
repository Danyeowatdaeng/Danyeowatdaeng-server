package com.tourapi.tourapi.web.controller.auth;

import com.tourapi.tourapi.auth.oauth.CustomOAuth2User;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.member.status.MemberSuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security OAuth2 기반 인증 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "OAuth2", description = "OAuth2 인증 관련 API")
public class OAuth2Controller {

    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보 조회", description = "OAuth2 로그인된 현재 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(@AuthenticationPrincipal CustomOAuth2User oauth2User) {
        log.info("OAuth2 사용자 정보 조회 요청: oauth2User={}", oauth2User != null ? "인증됨" : "인증되지 않음");
        
        if (oauth2User == null) {
            log.debug("OAuth2 사용자 정보 조회: 인증되지 않은 사용자");
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ApiResponse.onSuccess(MemberSuccessStatus.USER_INFO_RETRIEVED, response);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", oauth2User.getEmail());
        userInfo.put("name", oauth2User.getName());
        userInfo.put("provider", oauth2User.getRegistrationId());
        userInfo.put("isSignUpCompleted", oauth2User.isSignUpCompleted());

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("user", userInfo);

        return ApiResponse.onSuccess(MemberSuccessStatus.USER_INFO_RETRIEVED, response);
    }

    @GetMapping("/tokens")
    @Operation(summary = "JWT 토큰 조회", description = "OAuth2 로그인 후 발급된 JWT 토큰들을 조회합니다. (Swagger에서 테스트 시 OAuth2 로그인이 필요합니다)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTokens(@AuthenticationPrincipal CustomOAuth2User oauth2User) {
        log.info("OAuth2 토큰 조회 요청: oauth2User={}", oauth2User != null ? "인증됨" : "인증되지 않음");
        
        if (oauth2User == null) {
            log.debug("OAuth2 토큰 조회: 인증되지 않은 사용자");
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ApiResponse.onSuccess(MemberSuccessStatus.USER_INFO_RETRIEVED, response);
        }

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", oauth2User.getAccessToken());
        if (oauth2User.getRefreshToken() != null) {
            tokens.put("refreshToken", oauth2User.getRefreshToken());
        }
        tokens.put("email", oauth2User.getEmail());
        tokens.put("name", oauth2User.getName());
        tokens.put("isSignUpCompleted", oauth2User.isSignUpCompleted());

        return ApiResponse.onSuccess(MemberSuccessStatus.USER_INFO_RETRIEVED, tokens);
    }
}
