package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PetAvatar 업로드 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetAvatarUploadResponse {
    
    /**
     * S3 키
     */
    @JsonProperty("key")
    private String key;
    
    /**
     * 업로드된 파일의 URL
     */
    @JsonProperty("url")
    private String url;
}
