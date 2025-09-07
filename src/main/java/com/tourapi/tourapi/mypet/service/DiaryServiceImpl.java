package com.tourapi.tourapi.mypet.service;

import com.tourapi.tourapi.common.exception.mypet.DiaryHandler;
import com.tourapi.tourapi.common.exception.mypet.status.DiaryErrorStatus;
import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.mypet.Diary;
import com.tourapi.tourapi.mypet.dto.DiaryCreateRequest;
import com.tourapi.tourapi.mypet.dto.DiaryListResponse;
import com.tourapi.tourapi.mypet.dto.DiaryResponse;
import com.tourapi.tourapi.mypet.dto.DiaryUpdateRequest;
import com.tourapi.tourapi.mypet.repository.DiaryRepository;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public DiaryResponse createDiary(Long memberId, DiaryCreateRequest request) {
        Member member = getMemberById(memberId);

        Diary diary = Diary.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        Diary savedDiary = diaryRepository.save(diary);
        log.info("Diary created for member {}: {}", memberId, savedDiary.getId());

        return DiaryResponse.from(savedDiary);
    }

    @Override
    public DiaryListResponse getDiaries(Long memberId, Pageable pageable) {
        Member member = getMemberById(memberId);

        Page<Diary> diaryPage = diaryRepository.findByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(memberId, pageable);

        List<DiaryResponse> diaryResponses = diaryPage.getContent().stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());

        return DiaryListResponse.from(
                diaryResponses,
                (int) diaryPage.getTotalElements(),
                diaryPage.hasNext(),
                diaryPage.getNumber(),
                diaryPage.getSize()
        );
    }

    @Override
    public List<DiaryResponse> getAllDiaries(Long memberId) {
        Member member = getMemberById(memberId);

        List<Diary> diaries = diaryRepository.findByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(memberId);

        return diaries.stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public DiaryResponse getDiary(Long memberId, Long diaryId) {
        Diary diary = getDiaryByIdAndMemberId(diaryId, memberId);
        return DiaryResponse.from(diary);
    }

    @Override
    @Transactional
    public DiaryResponse updateDiary(Long memberId, Long diaryId, DiaryUpdateRequest request) {
        Diary diary = getDiaryByIdAndMemberId(diaryId, memberId);

        diary.updateDiary(request.getTitle(), request.getContent(), request.getImageUrl());

        Diary savedDiary = diaryRepository.save(diary);
        log.info("Diary updated for member {}: {}", memberId, diaryId);

        return DiaryResponse.from(savedDiary);
    }

    @Override
    @Transactional
    public void deleteDiary(Long memberId, Long diaryId) {
        Diary diary = getDiaryByIdAndMemberId(diaryId, memberId);

        diary.deactivate();
        diaryRepository.save(diary);

        log.info("Diary deleted for member {}: {}", memberId, diaryId);
    }

    @Override
    public List<DiaryResponse> getDiariesByDateRange(Long memberId, LocalDate startDate, LocalDate endDate) {
        Member member = getMemberById(memberId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Diary> diaries = diaryRepository.findByMemberIdAndDateRange(memberId, startDateTime, endDateTime);

        return diaries.stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public long getDiaryCount(Long memberId) {
        Member member = getMemberById(memberId);
        return diaryRepository.countByMemberIdAndIsActiveTrue(memberId);
    }

    @Override
    public DiaryResponse getLatestDiary(Long memberId) {
        Member member = getMemberById(memberId);

        Diary diary = diaryRepository.findTopByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new DiaryHandler(DiaryErrorStatus.DIARY_NOT_FOUND));

        return DiaryResponse.from(diary);
    }

    // Private helper methods
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    private Diary getDiaryByIdAndMemberId(Long diaryId, Long memberId) {
        return diaryRepository.findByIdAndMemberIdAndIsActiveTrue(diaryId, memberId)
                .orElseThrow(() -> new DiaryHandler(DiaryErrorStatus.DIARY_NOT_FOUND));
    }
}