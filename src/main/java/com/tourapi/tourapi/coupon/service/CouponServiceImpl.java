package com.tourapi.tourapi.coupon.service;

import com.tourapi.tourapi.common.exception.coupon.CouponException;
import com.tourapi.tourapi.common.exception.coupon.status.CouponErrorStatus;
import com.tourapi.tourapi.coupon.domain.Coupon;
import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.enums.CouponType;
import com.tourapi.tourapi.coupon.enums.UserCouponStatus;
import com.tourapi.tourapi.coupon.repository.CouponRepository;
import com.tourapi.tourapi.coupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponExchangeService couponExchangeService;

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> getAvailableCoupons(Pageable pageable) {
        return couponRepository.findAvailableCoupons(LocalDateTime.now(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Coupon> getAvailableCouponsByType(CouponType type, Pageable pageable) {
        return couponRepository.findAvailableCouponsByType(type, LocalDateTime.now(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(CouponErrorStatus.COUPON_NOT_FOUND));
    }

    @Override
    public UserCoupon exchangeCoupon(Long memberId, Long couponId) {
        // 쿠폰 존재 여부 확인
        Coupon coupon = getCouponById(couponId);
        
        // 교환 가능 여부 확인
        if (!coupon.isExchangeable()) {
            throw new CouponException(CouponErrorStatus.COUPON_NOT_AVAILABLE);
        }
        
        // 교환 가능 여부 확인 (포인트/스탬프 등)
        if (!couponExchangeService.canExchange(memberId, couponId)) {
            throw new CouponException(CouponErrorStatus.COUPON_NOT_AVAILABLE);
        }
        
        // 교환 타입에 따른 처리
        UserCoupon userCoupon;
        if (coupon.getExchangeType().name().equals("POINT")) {
            userCoupon = couponExchangeService.exchangeWithPoints(memberId, couponId);
        } else if (coupon.getExchangeType().name().equals("STAMP")) {
            userCoupon = couponExchangeService.exchangeWithStamps(memberId, couponId);
        } else {
            throw new CouponException(CouponErrorStatus.EXCHANGE_TYPE_NOT_SUPPORTED);
        }
        
        // 쿠폰 교환 수량 증가
        coupon.incrementExchangeCount();
        couponRepository.save(coupon);
        
        log.info("Coupon exchanged successfully: memberId={}, couponId={}, userCouponId={}", 
                memberId, couponId, userCoupon.getId());
        
        return userCoupon;
    }

    @Override
    public void useCoupon(Long userCouponId, String usedAtPlace) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CouponException(CouponErrorStatus.USER_COUPON_NOT_FOUND));
        
        if (userCoupon.getStatus() != UserCouponStatus.ACTIVE) {
            throw new CouponException(CouponErrorStatus.COUPON_ALREADY_USED);
        }
        
        if (!userCoupon.isUsable()) {
            throw new CouponException(CouponErrorStatus.COUPON_EXPIRED);
        }
        
        userCoupon.use(usedAtPlace);
        userCouponRepository.save(userCoupon);
        
        log.info("Coupon used successfully: userCouponId={}, usedAtPlace={}", userCouponId, usedAtPlace);
    }

    @Override
    public void useCouponByCode(String couponCode, String usedAtPlace) {
        UserCoupon userCoupon = userCouponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new CouponException(CouponErrorStatus.COUPON_CODE_NOT_FOUND));
        
        useCoupon(userCoupon.getId(), usedAtPlace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserCoupon> getUserCoupons(Long memberId, UserCouponStatus status, Pageable pageable) {
        if (status == null) {
            return userCouponRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        }
        return userCouponRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canExchangeCoupon(Long memberId, Long couponId) {
        try {
            return couponExchangeService.canExchange(memberId, couponId);
        } catch (Exception e) {
            log.warn("Failed to check coupon exchange availability: memberId={}, couponId={}", memberId, couponId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCouponCode(String couponCode) {
        try {
            UserCoupon userCoupon = userCouponRepository.findByCouponCode(couponCode)
                    .orElseThrow(() -> new CouponException(CouponErrorStatus.COUPON_CODE_NOT_FOUND));
            
            return userCoupon.isUsable();
        } catch (CouponException e) {
            return false;
        }
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    public void processExpiredCoupons() {
        List<UserCoupon> expiredCoupons = userCouponRepository.findExpiredUserCoupons(LocalDateTime.now());
        
        for (UserCoupon userCoupon : expiredCoupons) {
            userCoupon.expire();
            userCouponRepository.save(userCoupon);
        }
        
        log.info("Processed {} expired coupons", expiredCoupons.size());
    }
}
