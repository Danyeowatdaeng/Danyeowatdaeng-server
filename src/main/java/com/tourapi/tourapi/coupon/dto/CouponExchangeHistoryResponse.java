package com.tourapi.tourapi.coupon.dto;

import com.tourapi.tourapi.coupon.domain.CouponExchangeHistory;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponExchangeHistoryResponse {
    private Long id;
    private CouponResponse coupon;
    private ExchangeType exchangeType;
    private Integer exchangeCost;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CouponExchangeHistoryResponse from(CouponExchangeHistory history) {
        return CouponExchangeHistoryResponse.builder()
                .id(history.getId())
                .coupon(CouponResponse.from(history.getCoupon()))
                .exchangeType(history.getExchangeType())
                .exchangeCost(history.getExchangeCost())
                .description(history.getDescription())
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}
