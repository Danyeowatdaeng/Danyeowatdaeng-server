package com.tourapi.tourapi.coupon.dto;

import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.enums.UserCouponStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserCouponResponse {
    private Long id;
    private CouponResponse coupon;
    private UserCouponStatus status;
    private LocalDateTime expireDate;
    private LocalDateTime usedAt;
    private String usedAtPlace;
    private String couponCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserCouponResponse from(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .id(userCoupon.getId())
                .coupon(CouponResponse.from(userCoupon.getCoupon()))
                .status(userCoupon.getStatus())
                .expireDate(userCoupon.getExpireDate())
                .usedAt(userCoupon.getUsedAt())
                .usedAtPlace(userCoupon.getUsedAtPlace())
                .couponCode(userCoupon.getCouponCode())
                .createdAt(userCoupon.getCreatedAt())
                .updatedAt(userCoupon.getUpdatedAt())
                .build();
    }
}
