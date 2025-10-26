package com.tourapi.tourapi.coupon.repository;

import com.tourapi.tourapi.coupon.domain.CouponExchangeHistory;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponExchangeHistoryRepository extends JpaRepository<CouponExchangeHistory, Long> {

    // 특정 회원의 교환 내역 조회
    @Query("SELECT ceh FROM CouponExchangeHistory ceh WHERE ceh.member.id = :memberId")
    Page<CouponExchangeHistory> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);

    // 특정 회원의 교환 타입별 내역 조회
    @Query("SELECT ceh FROM CouponExchangeHistory ceh WHERE ceh.member.id = :memberId " +
           "AND ceh.exchangeType = :exchangeType")
    Page<CouponExchangeHistory> findByMemberIdAndExchangeTypeOrderByCreatedAtDesc(
            @Param("memberId") Long memberId, 
            @Param("exchangeType") ExchangeType exchangeType, 
            Pageable pageable);

    // 특정 회원의 총 교환 횟수
    @Query("SELECT COUNT(ceh) FROM CouponExchangeHistory ceh WHERE ceh.member.id = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);

    // 특정 회원의 총 교환 비용
    @Query("SELECT COALESCE(SUM(ceh.exchangeCost), 0) FROM CouponExchangeHistory ceh WHERE ceh.member.id = :memberId")
    Integer sumExchangeCostByMemberId(@Param("memberId") Long memberId);
}
