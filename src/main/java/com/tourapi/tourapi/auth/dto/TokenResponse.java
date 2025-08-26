package com.tourapi.tourapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class TokenResponse {
    @Schema(description = "액세스 토큰")
    public String accessToken;
    
    @Schema(description = "리프레시 토큰")
    public String refreshToken;
    
    @Schema(description = "세션 ID")
    public String sid;
    
    @Schema(description = "패밀리 ID")
    public String familyId;
    
    @Schema(description = "회원 이메일")
    public String email;
    
    @Schema(description = "회원 이름")
    public String name;
    
    @Schema(description = "회원가입 완료 여부")
    public boolean isSignUpCompleted;
    
    public TokenResponse(String accessToken, String refreshToken, String sid, String familyId, String email, String name, boolean isSignUpCompleted) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sid = sid;
        this.familyId = familyId;
        this.email = email;
        this.name = name;
        this.isSignUpCompleted = isSignUpCompleted;
    }
    
    // 리프레시 토큰이 없는 경우를 위한 생성자
    public TokenResponse(String accessToken, String email, String name, boolean isSignUpCompleted) {
        this.accessToken = accessToken;
        this.refreshToken = null;
        this.sid = null;
        this.familyId = null;
        this.email = email;
        this.name = name;
        this.isSignUpCompleted = isSignUpCompleted;
    }
}

