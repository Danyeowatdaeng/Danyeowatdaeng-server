package com.tourapi.tourapi.coupon.dto;

import com.tourapi.tourapi.coupon.domain.Coupon;
import com.tourapi.tourapi.coupon.enums.CouponDiscountType;
import com.tourapi.tourapi.coupon.enums.CouponType;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {
    private Long id;
    private String name;
    private String description;
    private CouponType type;
    private CouponDiscountType discountType;
    private Integer discountValue;
    private Integer exchangeCost;
    private ExchangeType exchangeType;
    private Integer validityDays;
    private Integer maxExchangeCount;
    private Integer currentExchangeCount;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .exchangeCost(coupon.getExchangeCost())
                .exchangeType(coupon.getExchangeType())
                .validityDays(coupon.getValidityDays())
                .maxExchangeCount(coupon.getMaxExchangeCount())
                .currentExchangeCount(coupon.getCurrentExchangeCount())
                .isActive(coupon.getIsActive())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}
