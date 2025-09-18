package com.tourapi.tourapi.point.dto;

import com.tourapi.tourapi.point.domain.Point;
import com.tourapi.tourapi.point.enums.PointType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointResponse {

    @Schema(description = "포인트 ID")
    private Long id;

    @Schema(description = "포인트 타입")
    private PointType pointType;

    @Schema(description = "포인트 타입 설명")
    private String pointTypeDescription;

    @Schema(description = "포인트 금액 (양수: 적립, 음수: 사용)")
    private Integer amount;

    @Schema(description = "포인트 설명")
    private String description;

    @Schema(description = "관련 엔티티 ID")
    private Long relatedId;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "적립/사용 구분")
    private String transactionType; // "EARN" or "SPEND"

    public static PointResponse from(Point point) {
        return PointResponse.builder()
                .id(point.getId())
                .pointType(point.getPointType())
                .pointTypeDescription(point.getPointType().getDescription())
                .amount(point.getAmount())
                .description(point.getDescription())
                .relatedId(point.getRelatedId())
                .createdAt(point.getCreatedAt())
                .transactionType(point.getAmount() > 0 ? "EARN" : "SPEND")
                .build();
    }
}