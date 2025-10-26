package com.tourapi.tourapi.coupon.service;

import com.tourapi.tourapi.coupon.domain.Coupon;
import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.enums.CouponType;
import com.tourapi.tourapi.coupon.enums.UserCouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {

    /**
     * 교환 가능한 쿠폰 목록 조회
     */
    Page<Coupon> getAvailableCoupons(Pageable pageable);

    /**
     * 특정 타입의 교환 가능한 쿠폰 목록 조회
     */
    Page<Coupon> getAvailableCouponsByType(CouponType type, Pageable pageable);

    /**
     * 쿠폰 상세 조회
     */
    Coupon getCouponById(Long couponId);

    /**
     * 쿠폰 교환
     */
    UserCoupon exchangeCoupon(Long memberId, Long couponId);

    /**
     * 쿠폰 사용
     */
    void useCoupon(Long userCouponId, String usedAtPlace);

    /**
     * 쿠폰 코드로 쿠폰 사용
     */
    void useCouponByCode(String couponCode, String usedAtPlace);

    /**
     * 사용자 쿠폰 목록 조회
     */
    Page<UserCoupon> getUserCoupons(Long memberId, UserCouponStatus status, Pageable pageable);

    /**
     * 쿠폰 교환 가능 여부 확인
     */
    boolean canExchangeCoupon(Long memberId, Long couponId);

    /**
     * 쿠폰 코드 유효성 검증
     */
    boolean validateCouponCode(String couponCode);

    /**
     * 만료된 쿠폰 처리 (스케줄러용)
     */
    void processExpiredCoupons();
}
