package com.tourapi.tourapi.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    @NotBlank(message = "메시지는 필수입니다")
    @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다")
    @Schema(description = "사용자 메시지", example = "제주도 여행 계획을 세워줘")
    private String message;

    @Schema(description = "사용할 모델", example = "gemini-1.5-flash", allowableValues = {"gemini-1.5-pro", "gemini-1.5-flash"})
    private String model = "gemini-1.5-flash";
}