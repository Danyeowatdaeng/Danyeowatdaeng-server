package com.tourapi.tourapi.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestLoginResponse {
    
    private String accessToken;
    private String refreshToken; // isSignUpCompleted가 true일 때만 포함
    private String sessionId;
    private String familyId;
    private String email;
    private String name;
    private boolean isSignUpCompleted;
}
