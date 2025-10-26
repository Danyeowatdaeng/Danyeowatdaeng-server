package com.tourapi.tourapi.coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserCouponStatus {
    ACTIVE("사용 가능"),
    USED("사용됨"),
    EXPIRED("만료됨");

    private final String description;
}
