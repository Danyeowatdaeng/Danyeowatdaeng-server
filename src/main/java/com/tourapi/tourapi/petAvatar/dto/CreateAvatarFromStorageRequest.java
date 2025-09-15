package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonProperty("setPrimary")
    private Boolean setPrimary;
}


