package com.tourapi.tourapi.mypet.service;

import com.tourapi.tourapi.mypet.Diary;
import com.tourapi.tourapi.mypet.dto.DiaryCreateRequest;
import com.tourapi.tourapi.mypet.dto.DiaryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryService {

    /**
     * 다이어리 생성
     * @param memberId 회원 ID
     * @param request 다이어리 생성 요청
     * @return 생성된 다이어리
     */
    Diary createDiary(Long memberId, DiaryCreateRequest request);

    /**
     * 다이어리 수정
     * @param diaryId 다이어리 ID
     * @param memberId 회원 ID
     * @param request 다이어리 수정 요청
     * @return 수정된 다이어리
     */
    Diary updateDiary(Long diaryId, Long memberId, DiaryUpdateRequest request);

    /**
     * 다이어리 삭제 (소프트 삭제)
     * @param diaryId 다이어리 ID
     * @param memberId 회원 ID
     */
    void deleteDiary(Long diaryId, Long memberId);

    /**
     * 회원의 모든 다이어리 목록 조회
     * @param memberId 회원 ID
     * @return 다이어리 목록
     */
    List<Diary> getDiariesByMember(Long memberId);

    /**
     * 회원의 다이어리 목록 조회 (페이징)
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 페이징된 다이어리 목록
     */
    Page<Diary> getDiariesByMember(Long memberId, Pageable pageable);

    /**
     * 다이어리 상세 조회
     * @param diaryId 다이어리 ID
     * @param memberId 회원 ID
     * @return 다이어리 상세 정보
     */
    Diary getDiaryDetail(Long diaryId, Long memberId);

    /**
     * 회원의 다이어리 개수 조회
     * @param memberId 회원 ID
     * @return 다이어리 개수
     */
    long getDiaryCount(Long memberId);

    /**
     * 특정 기간 내 다이어리 조회
     * @param memberId 회원 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간의 다이어리 목록
     */
    List<Diary> getDiariesByDateRange(Long memberId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 제목으로 다이어리 검색
     * @param memberId 회원 ID
     * @param title 검색할 제목
     * @return 검색된 다이어리 목록
     */
    List<Diary> searchDiariesByTitle(Long memberId, String title);

    /**
     * 최근 N개 다이어리 조회
     * @param memberId 회원 ID
     * @param limit 조회할 개수
     * @return 최근 다이어리 목록
     */
    List<Diary> getRecentDiaries(Long memberId, int limit);
}