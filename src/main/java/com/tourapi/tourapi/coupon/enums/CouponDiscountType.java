package com.tourapi.tourapi.coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponDiscountType {
    FREE("무료"),
    PERCENTAGE("퍼센트 할인"),
    FIXED_AMOUNT("고정 금액 할인");

    private final String description;
}
