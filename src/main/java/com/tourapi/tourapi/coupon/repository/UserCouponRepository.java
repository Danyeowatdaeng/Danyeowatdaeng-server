package com.tourapi.tourapi.coupon.repository;

import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.enums.UserCouponStatus;
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
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // 특정 회원의 쿠폰 목록 조회
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.member.id = :memberId ORDER BY uc.id DESC")
    Page<UserCoupon> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);

    // 특정 회원의 상태별 쿠폰 목록 조회
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.member.id = :memberId " +
           "AND uc.status = :status ORDER BY uc.id DESC")
    Page<UserCoupon> findByMemberIdAndStatusOrderByCreatedAtDesc(
            @Param("memberId") Long memberId, 
            @Param("status") UserCouponStatus status, 
            Pageable pageable);

    // 쿠폰 코드로 사용자 쿠폰 조회
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.couponCode = :couponCode")
    Optional<UserCoupon> findByCouponCode(@Param("couponCode") String couponCode);

    // 특정 회원의 특정 쿠폰 보유 여부 확인
    @Query("SELECT COUNT(uc) > 0 FROM UserCoupon uc WHERE uc.member.id = :memberId " +
           "AND uc.coupon.id = :couponId " +
           "AND uc.status = 'ACTIVE'")
    boolean existsByMemberIdAndCouponIdAndActive(@Param("memberId") Long memberId, @Param("couponId") Long couponId);

    // 만료된 쿠폰 조회 (스케줄러용)
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.status = 'ACTIVE' " +
           "AND uc.expireDate < :now")
    List<UserCoupon> findExpiredUserCoupons(@Param("now") LocalDateTime now);

    // 특정 회원의 사용 가능한 쿠폰 개수
    @Query("SELECT COUNT(uc) FROM UserCoupon uc WHERE uc.member.id = :memberId " +
           "AND uc.status = 'ACTIVE' " +
           "AND uc.expireDate > :now")
    long countActiveCouponsByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    // 특정 회원의 사용된 쿠폰 개수
    @Query("SELECT COUNT(uc) FROM UserCoupon uc WHERE uc.member.id = :memberId " +
           "AND uc.status = 'USED'")
    long countUsedCouponsByMemberId(@Param("memberId") Long memberId);
}
