package com.tourapi.tourapi.point.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.point.domain.Point;
import com.tourapi.tourapi.point.dto.PointSummaryResponse;
import com.tourapi.tourapi.point.enums.PointType;
import com.tourapi.tourapi.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    @Override
    public Point earnPointsIfNotEarnedToday(Long memberId, PointType pointType, String description, Long relatedId) {
        // 오늘 이미 해당 타입의 포인트를 적립했는지 확인
        if (hasEarnedTodayForType(memberId, pointType)) {
            log.info("Member {} already earned {} points today", memberId, pointType);
            return null; // 이미 적립했으면 null 반환
        }

        // 포인트 적립
        Integer amount = pointType.getDefaultAmount();
        return earnPoints(memberId, pointType, amount, description, relatedId);
    }

    @Override
    public Point earnPoints(Long memberId, PointType pointType, Integer amount, String description, Long relatedId) {
        Member member = getMemberById(memberId);

        Point point = Point.createEarn(member, pointType, amount, description, relatedId);
        Point savedPoint = pointRepository.save(point);

        // Member의 pointBalance 업데이트
        updateMemberPointBalance(memberId);

        log.info("Points earned: memberId={}, pointType={}, amount={}, relatedId={}",
                memberId, pointType, amount, relatedId);

        return savedPoint;
    }

    @Override
    public Point spendPoints(Long memberId, PointType pointType, Integer amount, String description, Long relatedId) {
        Member member = getMemberById(memberId);

        // 현재 보유 포인트 확인
        Integer currentBalance = getCurrentBalance(memberId);
        if (currentBalance < amount) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다. 현재: " + currentBalance + ", 필요: " + amount);
        }

        Point point = Point.createSpend(member, pointType, amount, description, relatedId);
        Point savedPoint = pointRepository.save(point);

        // Member의 pointBalance 업데이트
        updateMemberPointBalance(memberId);

        log.info("Points spent: memberId={}, pointType={}, amount={}, relatedId={}",
                memberId, pointType, amount, relatedId);

        return savedPoint;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentBalance(Long memberId) {
        Integer balance = pointRepository.calculateTotalPointsByMemberId(memberId);
        return balance != null ? balance : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Point> getPointHistory(Long memberId, Pageable pageable) {
        return pointRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PointSummaryResponse getPointSummary(Long memberId) {
        Integer currentBalance = getCurrentBalance(memberId);
        Integer totalEarned = pointRepository.calculateTotalEarnedPointsByMemberId(memberId);
        Integer totalSpent = pointRepository.calculateTotalSpentPointsByMemberId(memberId);
        Long totalTransactions = pointRepository.countByMemberId(memberId);

        return PointSummaryResponse.of(
                currentBalance,
                totalEarned != null ? totalEarned : 0,
                totalSpent != null ? totalSpent : 0,
                totalTransactions
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEarnedTodayForType(Long memberId, PointType pointType) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return pointRepository.existsByMemberIdAndPointTypeAndDateRange(
                memberId, pointType, startOfDay, endOfDay);
    }

    @Override
    public void updateMemberPointBalance(Long memberId) {
        Member member = getMemberById(memberId);
        Integer currentBalance = getCurrentBalance(memberId);
        member.setPointBalance(currentBalance);
        memberRepository.save(member);

        log.debug("Updated member {} point balance to {}", memberId, currentBalance);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }
}