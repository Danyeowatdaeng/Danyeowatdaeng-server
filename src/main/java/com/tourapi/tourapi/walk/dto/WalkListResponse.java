package com.tourapi.tourapi.walk.dto;

import com.tourapi.tourapi.walk.domain.Walk;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalkListResponse {

    @Schema(description = "산책 ID")
    private Long id;

    @Schema(description = "산책 이미지 URL")
    private String imageUrl;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static WalkListResponse from(Walk walk) {
        return WalkListResponse.builder()
                .id(walk.getId())
                .imageUrl(walk.getImageUrl())
                .createdAt(walk.getCreatedAt())
                .build();
    }
}