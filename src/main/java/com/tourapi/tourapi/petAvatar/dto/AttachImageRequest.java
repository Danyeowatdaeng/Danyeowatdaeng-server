package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachImageRequest {

    @JsonProperty("s3Key")
    private String s3Key;

    @JsonProperty("cdnUrl")
    private String cdnUrl;

    @JsonProperty("mime")
    private String mime;

    @JsonProperty("originalFilename")
    private String originalFilename;

    @JsonProperty("size")
    private Long size;
}


