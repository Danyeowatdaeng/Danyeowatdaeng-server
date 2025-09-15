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
public class CreateAvatarFromStorageResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("storageKey")
    private String storageKey;

    @JsonProperty("imageVersion")
    private Integer imageVersion;
}


