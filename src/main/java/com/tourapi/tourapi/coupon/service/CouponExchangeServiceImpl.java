package com.tourapi.tourapi.coupon.service;

import com.tourapi.tourapi.common.exception.coupon.CouponException;
import com.tourapi.tourapi.common.exception.coupon.status.CouponErrorStatus;
import com.tourapi.tourapi.coupon.domain.Coupon;
import com.tourapi.tourapi.coupon.domain.CouponExchangeHistory;
import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import com.tourapi.tourapi.coupon.repository.CouponExchangeHistoryRepository;
import com.tourapi.tourapi.coupon.repository.CouponRepository;
import com.tourapi.tourapi.coupon.repository.UserCouponRepository;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.point.enums.PointType;
import com.tourapi.tourapi.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponExchangeServiceImpl implements CouponExchangeService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponExchangeHistoryRepository exchangeHistoryRepository;
    private final MemberRepository memberRepository;
    private final PointService pointService;

    @Override
    public UserCoupon exchangeWithPoints(Long memberId, Long couponId) {
        // 회원 조회
        Member member = getMemberById(memberId);
        
        // 쿠폰 조회
        Coupon coupon = couponRepository.findExchangeableCoupon(couponId, LocalDateTime.now())
                .orElseThrow(() -> new CouponException(CouponErrorStatus.COUPON_NOT_AVAILABLE));
        
        // 포인트 교환 타입 확인
        if (coupon.getExchangeType() != ExchangeType.POINT) {
            throw new CouponException(CouponErrorStatus.EXCHANGE_TYPE_NOT_SUPPORTED);
        }
        
        // 포인트 잔액 확인
        Integer currentBalance = pointService.getCurrentBalance(memberId);
        if (currentBalance < coupon.getExchangeCost()) {
            throw new CouponException(CouponErrorStatus.INSUFFICIENT_POINTS);
        }
        
        // 이미 교환한 쿠폰인지 확인
        if (userCouponRepository.existsByMemberIdAndCouponIdAndActive(memberId, couponId)) {
            throw new CouponException(CouponErrorStatus.COUPON_ALREADY_EXCHANGED);
        }
        
        // 포인트 차감
        pointService.spendPoints(memberId, PointType.PURCHASE_SPEND, coupon.getExchangeCost(), 
                "쿠폰 교환: " + coupon.getName(), couponId);
        
        // 사용자 쿠폰 생성
        UserCoupon userCoupon = UserCoupon.create(member, coupon);
        UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);
        
        // 교환 내역 기록
        CouponExchangeHistory history = CouponExchangeHistory.create(
                member, coupon, savedUserCoupon, ExchangeType.POINT, 
                coupon.getExchangeCost(), "포인트로 쿠폰 교환");
        exchangeHistoryRepository.save(history);
        
        log.info("Coupon exchanged with points: memberId={}, couponId={}, cost={}", 
                memberId, couponId, coupon.getExchangeCost());
        
        return savedUserCoupon;
    }

    @Override
    public UserCoupon exchangeWithStamps(Long memberId, Long couponId) {
        // TODO: 스탬프 시스템 구현 후 구현
        throw new CouponException(CouponErrorStatus.EXCHANGE_TYPE_NOT_SUPPORTED, "스탬프 교환은 아직 지원하지 않습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canExchange(Long memberId, Long couponId) {
        try {
            // 쿠폰 조회
            Coupon coupon = couponRepository.findExchangeableCoupon(couponId, LocalDateTime.now())
                    .orElseThrow(() -> new CouponException(CouponErrorStatus.COUPON_NOT_AVAILABLE));
            
            // 교환 타입별 확인
            if (coupon.getExchangeType() == ExchangeType.POINT) {
                // 포인트 잔액 확인
                Integer currentBalance = pointService.getCurrentBalance(memberId);
                if (currentBalance < coupon.getExchangeCost()) {
                    return false;
                }
            } else if (coupon.getExchangeType() == ExchangeType.STAMP) {
                // TODO: 스탬프 잔액 확인
                return false;
            }
            
            // 이미 교환한 쿠폰인지 확인
            if (userCouponRepository.existsByMemberIdAndCouponIdAndActive(memberId, couponId)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.warn("Failed to check exchange availability: memberId={}, couponId={}", memberId, couponId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponExchangeHistory> getExchangeHistory(Long memberId, Pageable pageable) {
        return exchangeHistoryRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponExchangeHistory> getExchangeHistoryByType(Long memberId, ExchangeType exchangeType, Pageable pageable) {
        return exchangeHistoryRepository.findByMemberIdAndExchangeTypeOrderByCreatedAtDesc(memberId, exchangeType, pageable);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CouponException(CouponErrorStatus.COUPON_NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
