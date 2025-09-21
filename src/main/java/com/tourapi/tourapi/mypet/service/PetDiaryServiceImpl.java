package com.tourapi.tourapi.mypet.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.mypet.MyPetHandler;
import com.tourapi.tourapi.common.exception.mypet.status.MyPetErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.mypet.domain.PetDiary;
import com.tourapi.tourapi.mypet.dto.PetDiaryCreateRequest;
import com.tourapi.tourapi.mypet.dto.PetDiaryUpdateRequest;
import com.tourapi.tourapi.mypet.repository.PetDiaryRepository;
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
public class PetDiaryServiceImpl implements PetDiaryService {

    private final PetDiaryRepository petDiaryRepository;
    private final MemberRepository memberRepository;
    private final com.tourapi.tourapi.point.service.PointService pointService;

    @Override
    public PetDiary createDiary(Long memberId, PetDiaryCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));

        PetDiary diary = PetDiary.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .deleted(false)
                .build();

        PetDiary savedDiary = petDiaryRepository.save(diary);

        // 하루 한 번 다이어리 포인트 적립 (30포인트)
        try {
            com.tourapi.tourapi.point.domain.Point earnedPoint = pointService.earnPointsIfNotEarnedToday(
                    memberId,
                    com.tourapi.tourapi.point.enums.PointType.DIARY_DAILY,
                    "일일 다이어리 작성",
                    savedDiary.getId()
            );
            if (earnedPoint != null) {
                log.info("Diary daily points earned: memberId={}, diaryId={}, points={}",
                        memberId, savedDiary.getId(), earnedPoint.getAmount());
            }
        } catch (Exception e) {
            log.error("Failed to earn diary points for member {}: {}", memberId, e.getMessage());
            // 포인트 적립 실패해도 다이어리 작성은 성공으로 처리
        }

        log.info("Pet diary created: diaryId={}, memberId={}, title={}",
                savedDiary.getId(), memberId, request.getTitle());

        return savedDiary;
    }

    @Override
    public PetDiary updateDiary(Long memberId, Long diaryId, PetDiaryUpdateRequest request) {
        PetDiary diary = petDiaryRepository.findByIdAndMemberIdAndDeletedFalse(diaryId, memberId)
                .orElseThrow(() -> new MyPetHandler(MyPetErrorStatus.DIARY_NOT_FOUND));

        // 제목 업데이트 (null이 아닌 경우에만)
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            diary.setTitle(request.getTitle());
        }

        // 내용 업데이트 (null 허용)
        diary.setContent(request.getContent());

        // 이미지 URL 업데이트 (null 허용)
        diary.setImageUrl(request.getImageUrl());

        PetDiary updatedDiary = petDiaryRepository.save(diary);
        log.info("Pet diary updated: diaryId={}, memberId={}", diaryId, memberId);

        return updatedDiary;
    }

    @Override
    public void deleteDiary(Long memberId, Long diaryId) {
        PetDiary diary = petDiaryRepository.findByIdAndMemberIdAndDeletedFalse(diaryId, memberId)
                .orElseThrow(() -> new MyPetHandler(MyPetErrorStatus.DIARY_NOT_FOUND));

        diary.setDeleted(true);
        petDiaryRepository.save(diary);

        log.info("Pet diary deleted: diaryId={}, memberId={}", diaryId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PetDiary> getDiaryList(Long memberId, Pageable pageable) {
        return petDiaryRepository.findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(memberId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PetDiary getDiary(Long memberId, Long diaryId) {
        return petDiaryRepository.findByIdAndMemberIdAndDeletedFalse(diaryId, memberId)
                .orElseThrow(() -> new MyPetHandler(MyPetErrorStatus.DIARY_NOT_FOUND));
    }
}