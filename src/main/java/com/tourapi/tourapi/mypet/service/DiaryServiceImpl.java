package com.tourapi.tourapi.mypet.service;

import com.tourapi.tourapi.common.exception.diary.DiaryHandler;
import com.tourapi.tourapi.common.exception.diary.status.DiaryErrorStatus;
import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.mypet.Diary;
import com.tourapi.tourapi.mypet.dto.DiaryCreateRequest;
import com.tourapi.tourapi.mypet.dto.DiaryUpdateRequest;
import com.tourapi.tourapi.mypet.enums.DiaryStatus;
import com.tourapi.tourapi.mypet.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Diary createDiary(Long memberId, DiaryCreateRequest request) {
        Member member = getMemberById(memberId);

        Diary diary = Diary.create(
                member,
                request.getTitle(),
                request.getContent(),
                request.getImageUrl()
        );

        Diary savedDiary = diaryRepository.save(diary);
        log.info("Diary created for member {}: {}", memberId, savedDiary.getId());

        return savedDiary;
    }

    @Override
    @Transactional
    public Diary updateDiary(Long diaryId, Long memberId, DiaryUpdateRequest request) {
        Diary diary = getDiaryByIdAndMemberId(diaryId, memberId);

        diary.updateDiary(
                request.getTitle(),
                request.getContent(),
                request.getImageUrl()
        );

        Diary updatedDiary = diaryRepository.save(diary);
        log.info("Diary updated for member {}: {}", memberId, diaryId);

        return updatedDiary;
    }

    @Override
    @Transactional
    public void deleteDiary(Long diaryId, Long memberId) {
        Diary diary = getDiaryByIdAndMemberId(diaryId, memberId);

        diary.delete();
        diaryRepository.save(diary);

        log.info("Diary deleted for member {}: {}", memberId, diaryId);
    }

    @Override
    public List<Diary> getDiariesByMember(Long memberId) {
        validateMemberExists(memberId);
        return diaryRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, DiaryStatus.ACTIVE);
    }

    @Override
    public Page<Diary> getDiariesByMember(Long memberId, Pageable pageable) {
        validateMemberExists(memberId);
        return diaryRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, DiaryStatus.ACTIVE, pageable);
    }

    @Override
    public Diary getDiaryDetail(Long diaryId, Long memberId) {
        return getDiaryByIdAndMemberId(diaryId, memberId);
    }

    @Override
    public long getDiaryCount(Long memberId) {
        validateMemberExists(memberId);
        return diaryRepository.countByMemberIdAndStatus(memberId, DiaryStatus.ACTIVE);
    }

    @Override
    public List<Diary> getDiariesByDateRange(Long memberId, LocalDateTime startDate, LocalDateTime endDate) {
        validateMemberExists(memberId);
        return diaryRepository.findByMemberIdAndStatusAndCreatedAtBetween(
                memberId, DiaryStatus.ACTIVE, startDate, endDate);
    }

    @Override
    public List<Diary> searchDiariesByTitle(Long memberId, String title) {
        validateMemberExists(memberId);
        return diaryRepository.findByMemberIdAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                memberId, DiaryStatus.ACTIVE, title);
    }

    @Override
    public List<Diary> getRecentDiaries(Long memberId, int limit) {
        validateMemberExists(memberId);
        Pageable pageable = PageRequest.of(0, limit);
        return diaryRepository.findTopNByMemberIdAndStatus(memberId, DiaryStatus.ACTIVE, pageable);
    }

    // Private helper methods
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    private void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND);
        }
    }

    private Diary getDiaryByIdAndMemberId(Long diaryId, Long memberId) {
        return diaryRepository.findByIdAndMemberIdAndStatus(diaryId, memberId, DiaryStatus.ACTIVE)
                .orElseThrow(() -> new DiaryHandler(DiaryErrorStatus.DIARY_NOT_FOUND));
    }
}