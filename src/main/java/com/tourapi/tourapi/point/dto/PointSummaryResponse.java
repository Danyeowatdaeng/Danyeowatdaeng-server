package com.tourapi.tourapi.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointSummaryResponse {

    @Schema(description = "현재 보유 포인트")
    private Integer currentBalance;

    @Schema(description = "총 적립 포인트")
    private Integer totalEarned;

    @Schema(description = "총 사용 포인트")
    private Integer totalSpent;

    @Schema(description = "총 거래 횟수")
    private Long totalTransactions;

    public static PointSummaryResponse of(Integer currentBalance, Integer totalEarned, Integer totalSpent, Long totalTransactions) {
        return PointSummaryResponse.builder()
                .currentBalance(currentBalance)
                .totalEarned(totalEarned)
                .totalSpent(totalSpent)
                .totalTransactions(totalTransactions)
                .build();
    }
}