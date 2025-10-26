package com.tourapi.tourapi.coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExchangeType {
    POINT("포인트"),
    STAMP("스탬프");

    private final String description;
}
