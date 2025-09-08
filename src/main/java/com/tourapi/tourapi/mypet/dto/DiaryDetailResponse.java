package com.tourapi.tourapi.mypet.dto;

import com.tourapi.tourapi.mypet.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "다이어리 상세 정보 응답")
public class DiaryDetailResponse {

    @Schema(description = "다이어리 ID", example = "1")
    private Long id;

    @Schema(description = "다이어리 제목", example = "오늘의 산책 일기")
    private String title;

    @Schema(description = "다이어리 내용", example = "오늘 강아지와 함께 공원에서 산책을 했다...")
    private String content;

    @Schema(description = "이미지 URL", example = "https://example.com/images/diary123.jpg")
    private String imageUrl;

    @Schema(description = "작성 시간", example = "2025-01-15T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-01-15T15:30:00")
    private LocalDateTime updatedAt;

    public static DiaryDetailResponse from(Diary diary) {
        return DiaryDetailResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}