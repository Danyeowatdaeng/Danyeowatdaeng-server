package com.tourapi.tourapi.walk.dto;

import com.tourapi.tourapi.walk.domain.Walk;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalkDetailResponse {

    @Schema(description = "산책 ID")
    private Long id;

    @Schema(description = "산책 이미지 URL")
    private String imageUrl;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;

    public static WalkDetailResponse from(Walk walk) {
        return WalkDetailResponse.builder()
                .id(walk.getId())
                .imageUrl(walk.getImageUrl())
                .createdAt(walk.getCreatedAt())
                .updatedAt(walk.getUpdatedAt())
                .build();
    }
}