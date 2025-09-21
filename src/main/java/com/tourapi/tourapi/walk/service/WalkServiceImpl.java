// src/main/java/com/tourapi/tourapi/walk/service/WalkServiceImpl.java
package com.tourapi.tourapi.walk.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.walk.WalkHandler;
import com.tourapi.tourapi.common.exception.walk.status.WalkErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.walk.domain.Walk;
import com.tourapi.tourapi.walk.dto.WalkCreateRequest;
import com.tourapi.tourapi.walk.repository.WalkRepository;
import com.tourapi.tourapi.quest.service.QuestService; // 추가
import com.tourapi.tourapi.quest.enums.QuestType; // 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalkServiceImpl implements WalkService {

    private final WalkRepository walkRepository;
    private final MemberRepository memberRepository;
    private final com.tourapi.tourapi.point.service.PointService pointService;
    private final QuestService questService; // 추가

    @Override
    public Walk createWalk(Long memberId, WalkCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));

        Walk walk = Walk.builder()
                .member(member)
                .imageUrl(request.getImageUrl())
                .deleted(false)
                .build();

        Walk savedWalk = walkRepository.save(walk);

        // 퀘스트 진행도 업데이트 추가
        try {
            questService.updateQuestProgress(memberId, QuestType.WALK_DAILY, 1);
            log.info("Quest progress updated for walk: memberId={}, walkId={}", memberId, savedWalk.getId());
        } catch (Exception e) {
            log.error("Failed to update quest progress for member {}: {}", memberId, e.getMessage());
        }

        // 하루 한 번 산책 포인트 적립 (20포인트)
        try {
            com.tourapi.tourapi.point.domain.Point earnedPoint = pointService.earnPointsIfNotEarnedToday(
                    memberId,
                    com.tourapi.tourapi.point.enums.PointType.WALK_DAILY,
                    "일일 산책 완료",
                    savedWalk.getId()
            );
            if (earnedPoint != null) {
                log.info("Walk daily points earned: memberId={}, walkId={}, points={}",
                        memberId, savedWalk.getId(), earnedPoint.getAmount());
            }
        } catch (Exception e) {
            log.error("Failed to earn walk points for member {}: {}", memberId, e.getMessage());
            // 포인트 적립 실패해도 산책 등록은 성공으로 처리
        }

        log.info("Walk created: walkId={}, memberId={}", savedWalk.getId(), memberId);

        return savedWalk;
    }

    @Override
    public void deleteWalk(Long memberId, Long walkId) {
        Walk walk = walkRepository.findByIdAndMemberIdAndDeletedFalse(walkId, memberId)
                .orElseThrow(() -> new WalkHandler(WalkErrorStatus.WALK_NOT_FOUND));

        walk.setDeleted(true);
        walkRepository.save(walk);

        log.info("Walk deleted: walkId={}, memberId={}", walkId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Walk> getWalkList(Long memberId, Pageable pageable) {
        return walkRepository.findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(memberId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Walk getWalk(Long memberId, Long walkId) {
        return walkRepository.findByIdAndMemberIdAndDeletedFalse(walkId, memberId)
                .orElseThrow(() -> new WalkHandler(WalkErrorStatus.WALK_NOT_FOUND));
    }
}