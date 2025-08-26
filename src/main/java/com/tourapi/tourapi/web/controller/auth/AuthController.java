package com.tourapi.tourapi.web.controller.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.token.RefreshTokenStore;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.token.status.TokenErrorStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenStore store;

    public AuthController(RefreshTokenStore store) {
        this.store = store;
    }

    public static class RefreshRequest {
        @Schema(description = "기존 리프레시 토큰", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        public String oldRefresh;
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
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest body) {
        String uid = "demoUser";
        String sid = "demoSession";
        String familyId = "demoFamily";
        try {
            RefreshTokenStore.RotateResult r = store.rotate(body.oldRefresh, uid, sid, familyId);
            Map<String, String> resp = new HashMap<>();
            resp.put("accessToken", "dummy-access-token");
            resp.put("refreshToken", r.newRefreshRaw());
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException ex) {
            Map<String, String> err = new HashMap<>();
            err.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }
}
