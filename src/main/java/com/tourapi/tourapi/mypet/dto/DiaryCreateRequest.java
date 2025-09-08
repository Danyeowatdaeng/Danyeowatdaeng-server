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
    @Size(max = 100, message = "제목은 100자 이하로 입력해주세요")
    @Schema(description = "다이어리 제목", example = "오늘의 산책 일기", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "다이어리 내용", example = "오늘 강아지와 함께 공원에서 산책을 했다. 날씨가 정말 좋았고...")
    private String content;

    @Schema(description = "이미지 URL", example = "https://example.com/images/diary123.jpg")
    private String imageUrl;
}