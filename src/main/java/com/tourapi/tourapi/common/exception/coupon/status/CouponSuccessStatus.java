package com.tourapi.tourapi.common.exception.coupon.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponSuccessStatus implements SuccessResponse {
    
    // 쿠폰 관련 성공 (COUPON2001 ~ COUPON2999)
    COUPON_LIST_FOUND(HttpStatus.OK, "COUPON2001", "쿠폰 목록을 성공적으로 조회했습니다."),
    COUPON_DETAIL_FOUND(HttpStatus.OK, "COUPON2002", "쿠폰 상세 정보를 성공적으로 조회했습니다."),
    COUPON_EXCHANGED(HttpStatus.CREATED, "COUPON2003", "쿠폰을 성공적으로 교환했습니다."),
    COUPON_USED(HttpStatus.OK, "COUPON2004", "쿠폰을 성공적으로 사용했습니다."),
    USER_COUPON_LIST_FOUND(HttpStatus.OK, "COUPON2005", "내 쿠폰 목록을 성공적으로 조회했습니다."),
    COUPON_EXCHANGE_HISTORY_FOUND(HttpStatus.OK, "COUPON2006", "쿠폰 교환 내역을 성공적으로 조회했습니다."),
    COUPON_VALIDATED(HttpStatus.OK, "COUPON2007", "쿠폰이 유효합니다."),
    COUPON_EXCHANGE_AVAILABLE(HttpStatus.OK, "COUPON2008", "쿠폰 교환이 가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getSuccessStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
