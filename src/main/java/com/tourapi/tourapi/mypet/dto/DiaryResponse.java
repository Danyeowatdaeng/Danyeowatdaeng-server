package com.tourapi.tourapi.mypet.dto;

import com.tourapi.tourapi.mypet.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DiaryResponse {

    @Schema(description = "다이어리 ID")
    private Long id;

    @Schema(description = "다이어리 제목")
    private String title;

    @Schema(description = "다이어리 내용")
    private String content;

    @Schema(description = "첨부 이미지 URL")
    private String imageUrl;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "작성자 ID")
    private Long memberId;

    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .memberId(diary.getMember().getId())
                .build();
    }
}
