package com.tourapi.tourapi.mypet.service;

import com.tourapi.tourapi.mypet.domain.PetDiary;
import com.tourapi.tourapi.mypet.dto.PetDiaryCreateRequest;
import com.tourapi.tourapi.mypet.dto.PetDiaryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PetDiaryService {

    /**
     * 다이어리 생성
     */
    PetDiary createDiary(Long memberId, PetDiaryCreateRequest request);

    /**
     * 다이어리 수정
     */
    PetDiary updateDiary(Long memberId, Long diaryId, PetDiaryUpdateRequest request);

    /**
     * 다이어리 삭제 (soft delete)
     */
    void deleteDiary(Long memberId, Long diaryId);

    /**
     * 다이어리 목록 조회
     */
    Page<PetDiary> getDiaryList(Long memberId, Pageable pageable);

    /**
     * 다이어리 상세 조회
     */
    PetDiary getDiary(Long memberId, Long diaryId);
}