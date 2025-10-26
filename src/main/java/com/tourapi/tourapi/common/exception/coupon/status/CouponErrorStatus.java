package com.tourapi.tourapi.common.exception.coupon.status;

import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorStatus implements ErrorResponse {
    
    // 쿠폰 관련 에러 (COUPON4001 ~ COUPON4999)
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON4001", "쿠폰을 찾을 수 없습니다."),
    COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "COUPON4002", "교환 가능한 쿠폰이 아닙니다."),
    COUPON_EXCHANGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COUPON4003", "쿠폰 교환 한도를 초과했습니다."),
    COUPON_ALREADY_EXCHANGED(HttpStatus.BAD_REQUEST, "COUPON4004", "이미 교환한 쿠폰입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "COUPON4005", "만료된 쿠폰입니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "COUPON4006", "이미 사용된 쿠폰입니다."),
    COUPON_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON4007", "쿠폰 코드를 찾을 수 없습니다."),
    COUPON_CODE_INVALID(HttpStatus.BAD_REQUEST, "COUPON4008", "유효하지 않은 쿠폰 코드입니다."),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "COUPON4009", "포인트가 부족합니다."),
    INSUFFICIENT_STAMPS(HttpStatus.BAD_REQUEST, "COUPON4010", "스탬프가 부족합니다."),
    EXCHANGE_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "COUPON4011", "지원하지 않는 교환 타입입니다."),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON4012", "사용자 쿠폰을 찾을 수 없습니다."),
    COUPON_EXCHANGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COUPON4013", "쿠폰 교환에 실패했습니다."),
    COUPON_USE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COUPON4014", "쿠폰 사용에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getErrorStatus() {
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
