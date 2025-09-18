package com.tourapi.tourapi.point.repository;

import com.tourapi.tourapi.point.domain.Point;
import com.tourapi.tourapi.point.enums.PointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    // 특정 회원의 포인트 내역 조회 (페이징)
    Page<Point> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 특정 회원의 총 포인트 계산
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.member.id = :memberId")
    Integer calculateTotalPointsByMemberId(@Param("memberId") Long memberId);

    // 특정 날짜에 특정 타입의 포인트를 이미 적립했는지 확인
    @Query("SELECT COUNT(p) > 0 FROM Point p WHERE p.member.id = :memberId " +
            "AND p.pointType = :pointType " +
            "AND p.amount > 0 " +
            "AND p.createdAt >= :startOfDay " +
            "AND p.createdAt < :endOfDay")
    boolean existsByMemberIdAndPointTypeAndDateRange(
            @Param("memberId") Long memberId,
            @Param("pointType") PointType pointType,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // 특정 회원의 적립 포인트 합계
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.member.id = :memberId AND p.amount > 0")
    Integer calculateTotalEarnedPointsByMemberId(@Param("memberId") Long memberId);

    // 특정 회원의 사용 포인트 합계 (절댓값)
    @Query("SELECT COALESCE(SUM(ABS(p.amount)), 0) FROM Point p WHERE p.member.id = :memberId AND p.amount < 0")
    Integer calculateTotalSpentPointsByMemberId(@Param("memberId") Long memberId);

    // 특정 회원의 포인트 내역 개수
    long countByMemberId(Long memberId);
}