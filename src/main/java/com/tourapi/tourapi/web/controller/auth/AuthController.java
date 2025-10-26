package com.tourapi.tourapi.web.controller.auth;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.auth.enums.OauthProvider;
import com.tourapi.tourapi.member.enums.Role;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.auth.service.AuthService.TokenPair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.dto.RefreshRequest;
import com.tourapi.tourapi.auth.dto.SocialLoginRequest;
import com.tourapi.tourapi.auth.dto.TokenResponse;
import com.tourapi.tourapi.auth.service.AuthService;

import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberSuccessStatus;
import com.tourapi.tourapi.common.exception.token.status.TokenErrorStatus;
import com.tourapi.tourapi.common.exception.token.status.TokenSuccessStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberRepository memberRepository;

    @PostMapping("/local/login")
    @Operation(
            summary = "로컬 테스트 로그인 (개발용)",
            description = "이메일로 간단하게 로그인합니다. 회원이 없으면 자동으로 생성됩니다."
    )
    public ResponseEntity<ApiResponse<TokenResponse>> localLogin(@RequestBody LocalLoginRequest request) {
        try {
            log.info("로컬 테스트 로그인 요청: email={}, name={}", request.email, request.name);
            
            // 이메일로 회원 조회 또는 생성
            Member member = memberRepository.findByEmail(request.email).orElse(null);
            if (member == null) {
                log.info("회원이 없어서 생성: email={}, name={}", request.email, request.name);
                member = new Member();
                member.setProvider(OauthProvider.LOCAL);
                member.setProviderUserId("local_" + request.email); // LOCAL용 임시 providerUserId
                member.setEmail(request.email);
                member.setNickname(request.name != null ? request.name : "테스트유저");
                member.setProfileImageUrl(null);
                member.setSignUpCompleted(true); // 테스트용으로 가입 완료 처리
                member.setRole(Role.USER);
                member = memberRepository.save(member);
                log.info("회원 생성 완료: memberId={}", member.getId());
            }

            // 토큰 발급
            TokenPair tokens = authService.issueTokensOnLogin(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.isSignUpCompleted()
            );

            TokenResponse response = new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.sessionId(),
                tokens.familyId(),
                member.getEmail(),
                member.getNickname(),
                member.isSignUpCompleted()
            );

            log.info("로컬 테스트 로그인 성공: memberId={}, email={}", member.getId(), member.getEmail());
            return ApiResponse.onSuccess(TokenSuccessStatus.REFRESH_SUCCESS, response);
        } catch (Exception ex) {
            log.error("로컬 테스트 로그인 실패", ex);
            return ApiResponse.onFailure(TokenErrorStatus.INVALID_REFRESH_TOKEN, null);
        }
    }

    @PostMapping("/login/social")
    @Operation(
            summary = "01. 소셜 로그인 및 회원가입",
            description = "가입되어 있는 경우는 액세스/리프레시 주고, 안되어있으면 액세스만 줍니다. 판단은 isSignUpCompleted로",
            tags = {"회원가입 플로우"},
            operationId = "01-social-login"
    )
    public ResponseEntity<ApiResponse<TokenResponse>> socialLogin(@RequestBody SocialLoginRequest body) {
        try {
            TokenResponse tokens = authService.socialLogin(body.provider, body.token);
            
            // 리프레시 토큰이 없으면 약관 동의가 필요한 상태로 간주
            if (tokens.refreshToken == null) {
                return ApiResponse.onSuccess(MemberSuccessStatus.TERMS_AGREEMENT_REQUIRED, tokens);
            }
            
            // 리프레시 토큰이 있으면 정상 로그인
            return ApiResponse.onSuccess(MemberSuccessStatus.SIGN_IN_SUCCESS, tokens);
        } catch (Exception ex) {
            return ApiResponse.onFailure(TokenErrorStatus.INVALID_REFRESH_TOKEN, null);
        }
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

    // 로컬 로그인 요청 DTO
    public static class LocalLoginRequest {
        public String email;
        public String name;
    }
}