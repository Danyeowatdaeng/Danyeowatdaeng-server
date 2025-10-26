package com.tourapi.tourapi.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CouponUseRequest {
    
    @NotBlank(message = "쿠폰 코드는 필수입니다")
    private String couponCode;
    
    @NotBlank(message = "사용 장소는 필수입니다")
    private String usedAtPlace;
}
