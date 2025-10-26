package com.tourapi.tourapi.coupon.repository;

import com.tourapi.tourapi.coupon.domain.Coupon;
import com.tourapi.tourapi.coupon.enums.CouponType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // 활성화된 쿠폰 목록 조회 (교환 가능한 쿠폰)
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND (c.startDate IS NULL OR c.startDate <= :now) " +
           "AND (c.endDate IS NULL OR c.endDate >= :now) " +
           "AND c.currentExchangeCount < c.maxExchangeCount " +
           "ORDER BY c.id DESC")
    Page<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now, Pageable pageable);

    // 특정 타입의 활성화된 쿠폰 조회
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.type = :type " +
           "AND (c.startDate IS NULL OR c.startDate <= :now) " +
           "AND (c.endDate IS NULL OR c.endDate >= :now) " +
           "AND c.currentExchangeCount < c.maxExchangeCount " +
           "ORDER BY c.id DESC")
    Page<Coupon> findAvailableCouponsByType(@Param("type") CouponType type, @Param("now") LocalDateTime now, Pageable pageable);

    // 교환 가능한 쿠폰 조회
    @Query("SELECT c FROM Coupon c WHERE c.id = :couponId " +
           "AND c.isActive = true " +
           "AND (c.startDate IS NULL OR c.startDate <= :now) " +
           "AND (c.endDate IS NULL OR c.endDate >= :now) " +
           "AND c.currentExchangeCount < c.maxExchangeCount")
    Optional<Coupon> findExchangeableCoupon(@Param("couponId") Long couponId, @Param("now") LocalDateTime now);

    // 만료된 쿠폰 조회 (스케줄러용)
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.endDate IS NOT NULL " +
           "AND c.endDate < :now")
    List<Coupon> findExpiredCoupons(@Param("now") LocalDateTime now);

    // 활성화된 모든 쿠폰 조회
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true")
    List<Coupon> findByIsActiveTrue();
}
