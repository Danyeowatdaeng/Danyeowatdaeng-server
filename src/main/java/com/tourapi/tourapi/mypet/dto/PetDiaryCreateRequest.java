package com.tourapi.tourapi.mypet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PetDiaryCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    @Schema(description = "다이어리 제목", example = "오늘 우리 강아지와의 산책")
    private String title;

    @Schema(description = "다이어리 내용", example = "오늘은 날씨가 좋아서 공원에서 산책을 했다...")
    private String content;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
}