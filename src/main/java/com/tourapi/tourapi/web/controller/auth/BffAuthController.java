package com.tourapi.tourapi.web.controller.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.service.OAuthService;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberSuccessStatus;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 최소 BFF 엔드포인트 뼈대.
 * - 공급자별 로직은 전략 패턴으로 위임
 */
@RestController
@RequestMapping("/api/auth")
public class BffAuthController {


    private final OAuthService oauthService;

    public BffAuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/login/{provider}")
    public ResponseEntity<Void> startLogin(@PathVariable("provider") String provider, HttpServletRequest request) {
        return oauthService.startLogin(provider, request);
    }

    @GetMapping("/callback/{provider}")
    public ResponseEntity<Void> oauthCallback(@PathVariable("provider") String provider,
                                              @RequestParam(name = "code", required = false) String code,
                                              @RequestParam(name = "state", required = false) String state,
                                              HttpServletRequest request) {
        return oauthService.handleCallback(provider, code, state, request);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(HttpServletRequest request) {
        try {
            Map<String, Object> userInfo = oauthService.getCurrentUser(request);
            return ApiResponse.onSuccess(MemberSuccessStatus.USER_INFO_RETRIEVED, userInfo);
        } catch (IllegalStateException ex) {
            if (ex.getMessage().contains("세션이 만료")) {
                return ApiResponse.onFailure(MemberErrorStatus.OAUTH_SESSION_EXPIRED, null);
            } else {
                return ApiResponse.onFailure(MemberErrorStatus.UNAUTHENTICATED, null);
            }
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        oauthService.logout(request);
        return ApiResponse.onSuccess(MemberSuccessStatus.OAUTH_LOGOUT_SUCCESS, null);
    }

}



