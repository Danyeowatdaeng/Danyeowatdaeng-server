package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사전서명 업로드 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignUploadResponse {
    
    /**
     * 사전서명 업로드 URL
     */
    @JsonProperty("uploadUrl")
    private String uploadUrl;
    
    /**
     * S3 객체 키
     */
    @JsonProperty("objectKey")
    private String objectKey;
    
    /**
     * 만료 시간 (초)
     */
    @JsonProperty("expiresIn")
    private long expiresIn;
    
    /**
     * 업로드 완료 후 CDN URL
     */
    @JsonProperty("cdnUrl")
    private String cdnUrl;
}
