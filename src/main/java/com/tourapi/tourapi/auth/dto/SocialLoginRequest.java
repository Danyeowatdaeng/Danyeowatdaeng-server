package com.tourapi.tourapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SocialLoginRequest {
    @Schema(description = "소셜 제공자", example = "GOOGLE")
    public String provider;
    
    @Schema(description = "소셜 액세스/ID 토큰")
    public String token;
}


