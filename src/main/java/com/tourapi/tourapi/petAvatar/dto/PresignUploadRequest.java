package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사전서명 업로드 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignUploadRequest {
    
    /**
     * 파일 확장자 (예: .png, .jpg)
     */
    @JsonProperty("ext")
    private String ext;
    
    /**
     * MIME 타입 (예: image/png, image/jpeg)
     */
    @JsonProperty("mime")
    private String mime;
    
    /**
     * 최대 파일 크기 (bytes, 선택사항)
     */
    @JsonProperty("maxFileSize")
    private Long maxFileSize;
    
    /**
     * S3 키 접두사 (input, result, thumb, 선택사항)
     */
    @JsonProperty("prefix")
    private String prefix;
}
