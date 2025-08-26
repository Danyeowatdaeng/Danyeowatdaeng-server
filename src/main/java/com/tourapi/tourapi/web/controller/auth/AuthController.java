package com.tourapi.tourapi.web.controller.auth;

import com.tourapi.tourapi.common.exception.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.dto.RefreshRequest;
import com.tourapi.tourapi.auth.dto.SocialLoginRequest;
import com.tourapi.tourapi.auth.dto.TokenResponse;
import com.tourapi.tourapi.auth.service.AuthService;
import com.tourapi.tourapi.auth.token.RefreshTokenStore;

import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.token.status.TokenErrorStatus;
import com.tourapi.tourapi.common.exception.token.status.TokenSuccessStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberSuccessStatus;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenStore store;
    private final AuthService authService;

    public AuthController(RefreshTokenStore store, AuthService authService) {
        this.store = store;
        this.authService = authService;
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "리프레시 토큰 회전",
            description = "기존 리프레시 토큰을 검증 후 새 액세스 토큰과 리프레시 토큰을 발급합니다."
    )
    @ApiErrorCodeExample(
            value = TokenErrorStatus.class,
            codes = {"INVALID_REFRESH_TOKEN", "REFRESH_TOKEN_EXPIRED"}
    )
    @ApiErrorCodeExample(
            value = MemberErrorStatus.class,
            codes = {"MEMBER_NOT_FOUND"}
    )
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody RefreshRequest body) {
        try {
            TokenResponse tokens = authService.refreshToken(body.oldRefresh);
            return ApiResponse.onSuccess(TokenSuccessStatus.REFRESH_SUCCESS, tokens);
        } catch (Exception ex) {
            return ApiResponse.onFailure(TokenErrorStatus.INVALID_REFRESH_TOKEN, null);
        }
    }

    @PostMapping("/login/social")
    @Operation(
            summary = "소셜 로그인",
            description = "소셜 토큰을 검증하고 액세스/리프레시 토큰을 발급합니다."
    )
    public ResponseEntity<ApiResponse<TokenResponse>> socialLogin(@RequestBody SocialLoginRequest body) {
        try {
            TokenResponse tokens = authService.socialLogin(body.provider, body.token);
            return ApiResponse.onSuccess(MemberSuccessStatus.SIGN_IN_SUCCESS, tokens);
        } catch (Exception ex) {
            return ApiResponse.onFailure(TokenErrorStatus.INVALID_REFRESH_TOKEN, null);
        }
    }
}
