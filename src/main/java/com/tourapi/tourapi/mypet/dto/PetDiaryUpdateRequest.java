package com.tourapi.tourapi.mypet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PetDiaryUpdateRequest {

    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    @Schema(description = "다이어리 제목", example = "수정된 제목")
    private String title;

    @Schema(description = "다이어리 내용", example = "수정된 내용...")
    private String content;

    @Schema(description = "이미지 URL", example = "https://example.com/updated-image.jpg")
    private String imageUrl;
}