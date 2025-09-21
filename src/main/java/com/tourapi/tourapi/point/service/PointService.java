package com.tourapi.tourapi.point.service;

import com.tourapi.tourapi.point.domain.Point;
import com.tourapi.tourapi.point.dto.PointSummaryResponse;
import com.tourapi.tourapi.point.enums.PointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointService {

    /**
     * 포인트 적립 (하루 한 번 제한 체크)
     */
    Point earnPointsIfNotEarnedToday(Long memberId, PointType pointType, String description, Long relatedId);

    /**
     * 포인트 사용
     */
    Point spendPoints(Long memberId, PointType pointType, Integer amount, String description, Long relatedId);

    /**
     * 포인트 적립 (제한 없음 - 관리자용)
     */
    Point earnPoints(Long memberId, PointType pointType, Integer amount, String description, Long relatedId);

    /**
     * 회원의 현재 보유 포인트 조회
     */
    Integer getCurrentBalance(Long memberId);

    /**
     * 회원의 포인트 내역 조회
     */
    Page<Point> getPointHistory(Long memberId, Pageable pageable);

    /**
     * 회원의 포인트 요약 정보 조회
     */
    PointSummaryResponse getPointSummary(Long memberId);

    /**
     * 오늘 해당 타입의 포인트를 이미 적립했는지 확인
     */
    boolean hasEarnedTodayForType(Long memberId, PointType pointType);

    /**
     * Member 엔티티의 pointBalance 업데이트
     */
    void updateMemberPointBalance(Long memberId);
}