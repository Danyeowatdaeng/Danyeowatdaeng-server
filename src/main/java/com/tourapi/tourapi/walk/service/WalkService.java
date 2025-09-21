package com.tourapi.tourapi.walk.service;

import com.tourapi.tourapi.walk.domain.Walk;
import com.tourapi.tourapi.walk.dto.WalkCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalkService {

    /**
     * 산책 기록 생성
     */
    Walk createWalk(Long memberId, WalkCreateRequest request);

    /**
     * 산책 기록 삭제 (soft delete)
     */
    void deleteWalk(Long memberId, Long walkId);

    /**
     * 산책 기록 목록 조회
     */
    Page<Walk> getWalkList(Long memberId, Pageable pageable);

    /**
     * 산책 기록 상세 조회
     */
    Walk getWalk(Long memberId, Long walkId);
}