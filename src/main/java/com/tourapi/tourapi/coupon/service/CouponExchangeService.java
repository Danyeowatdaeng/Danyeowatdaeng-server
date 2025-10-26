package com.tourapi.tourapi.coupon.service;

import com.tourapi.tourapi.coupon.domain.CouponExchangeHistory;
import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponExchangeService {

    /**
     * 포인트로 쿠폰 교환
     */
    UserCoupon exchangeWithPoints(Long memberId, Long couponId);

    /**
     * 스탬프로 쿠폰 교환 (향후 구현)
     */
    UserCoupon exchangeWithStamps(Long memberId, Long couponId);

    /**
     * 교환 가능 여부 확인
     */
    boolean canExchange(Long memberId, Long couponId);

    /**
     * 교환 내역 조회
     */
    Page<CouponExchangeHistory> getExchangeHistory(Long memberId, Pageable pageable);

    /**
     * 교환 타입별 내역 조회
     */
    Page<CouponExchangeHistory> getExchangeHistoryByType(Long memberId, ExchangeType exchangeType, Pageable pageable);
}
