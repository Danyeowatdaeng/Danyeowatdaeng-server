package com.tourapi.tourapi.coupon.dto;

import com.tourapi.tourapi.coupon.enums.ExchangeType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CouponExchangeRequest {
    
    @NotNull(message = "쿠폰 ID는 필수입니다")
    private Long couponId;
    
    @NotNull(message = "교환 타입은 필수입니다")
    private ExchangeType exchangeType;
}
