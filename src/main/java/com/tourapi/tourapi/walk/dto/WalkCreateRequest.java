package com.tourapi.tourapi.walk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalkCreateRequest {

    @Schema(description = "산책 이미지 URL", example = "https://example.com/walk-image.jpg")
    private String imageUrl;
}