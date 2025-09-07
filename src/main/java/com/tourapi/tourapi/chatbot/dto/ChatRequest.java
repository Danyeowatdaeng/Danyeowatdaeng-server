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
    @Size(max = 1000, message = "메시지는 1000자 이하여야 합니다")
    @Schema(description = "사용자 메시지", example = "안녕하세요! 반려동물 관련 질문이 있어요.")
    private String message;

    @Schema(description = "세션 ID (새 대화면 생략 가능)", example = "session_123456")
    private String sessionId;

    @Schema(description = "대화 컨텍스트 포함 여부", example = "true")
    private Boolean includeContext = true;
}