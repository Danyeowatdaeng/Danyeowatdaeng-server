package com.tourapi.tourapi.mypet.service;

import com.tourapi.tourapi.mypet.Diary;
import com.tourapi.tourapi.mypet.dto.DiaryCreateRequest;
import com.tourapi.tourapi.mypet.dto.DiaryListResponse;
import com.tourapi.tourapi.mypet.dto.DiaryResponse;
import com.tourapi.tourapi.mypet.dto.DiaryUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DiaryService {

   // 다이어리 생성
    DiaryResponse createDiary(Long memberId, DiaryCreateRequest request);

    // 다이어리 목록 조회(페이징)
    DiaryListResponse getDiaries(Long memberId, Pageable pageable);

    // 다이어리 목록 조회(전체)
    List<DiaryResponse> getAllDiaries(Long memberId);

    // 다이어리 상세 조회
    DiaryResponse getDiary(Long memberId, Long diaryId);

    // 다이어리 수정
    DiaryResponse updateDiary(Long memberId, Long diaryId, DiaryUpdateRequest request);

    // 다이어리 삭제
    void deleteDiary(Long memberId, Long diaryId);

    // 특정 기간 다이어리 조회
    List<DiaryResponse> getDiariesByDateRange(Long memberId, LocalDate startDate, LocalDate endDate);

    // 다이어리 개수 조회
    long getDiaryCount(Long memberId);

    // 최근 다이어리 조회
    DiaryResponse getLatestDiary(Long memberId);
}