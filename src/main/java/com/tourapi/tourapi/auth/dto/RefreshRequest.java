package com.tourapi.tourapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RefreshRequest {
    @Schema(description = "기존 리프레시 토큰", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    public String oldRefresh;
}


