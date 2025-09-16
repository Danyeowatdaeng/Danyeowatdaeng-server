package com.tourapi.tourapi.walk.repository;

import com.tourapi.tourapi.walk.domain.Walk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalkRepository extends JpaRepository<Walk, Long> {

    // 특정 회원의 삭제되지 않은 산책 기록 목록 조회 (페이징)
    Page<Walk> findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 특정 회원의 특정 산책 기록 조회 (삭제되지 않은 것만)
    Optional<Walk> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);

    // 특정 회원의 산책 기록 개수
    long countByMemberIdAndDeletedFalse(Long memberId);
}