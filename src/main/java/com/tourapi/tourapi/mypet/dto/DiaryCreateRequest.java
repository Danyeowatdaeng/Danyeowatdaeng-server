package com.tourapi.tourapi.mypet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    @Schema(description = "다이어리 제목", example = "오늘의 산책")
    private String title;

    @Schema(description = "다이어리 내용", example = "오늘은 날씨가 좋아서 반려동물과 함께 공원에서 산책했다...")
    private String content;

    @Schema(description = "첨부 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
}
