package com.tourapi.tourapi.mypet.dto;

import com.tourapi.tourapi.mypet.domain.PetDiary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PetDiaryDetailResponse {

    @Schema(description = "다이어리 ID")
    private Long id;

    @Schema(description = "다이어리 제목")
    private String title;

    @Schema(description = "다이어리 내용")
    private String content;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시")
    private LocalDateTime updatedAt;

    public static PetDiaryDetailResponse from(PetDiary diary) {
        return PetDiaryDetailResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}