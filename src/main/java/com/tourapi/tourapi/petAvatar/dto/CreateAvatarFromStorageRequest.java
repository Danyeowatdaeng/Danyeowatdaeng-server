package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateAvatarFromStorageRequest", example = "{\n  \"s3Key\": \"result/2025/09/15/xxxx.png\",\n  \"cdnUrl\": \"https://cdn.example.com/result/2025/09/15/xxxx.png\",\n  \"mime\": \"image/png\",\n  \"memberId\": 123\n}")
public class CreateAvatarFromStorageRequest {

    @JsonProperty("petType")
    private PetType petType;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("style")
    private PetAvatarStyle style;

    @JsonProperty("s3Key")
    private String s3Key;

    @JsonProperty("cdnUrl")
    private String cdnUrl;

    @JsonProperty("mime")
    private String mime;

    @JsonProperty("memberId")
    private Long memberId;
}


