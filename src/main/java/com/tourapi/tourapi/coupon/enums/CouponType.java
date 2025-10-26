package com.tourapi.tourapi.coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponType {
    DRINK("음료"),
    GROOMING("미용"),
    HOTEL("펫호텔"),
    FOOD("사료/간식"),
    TOY("장난감"),
    MEDICAL("의료"),
    ETC("기타");

    private final String description;
}
