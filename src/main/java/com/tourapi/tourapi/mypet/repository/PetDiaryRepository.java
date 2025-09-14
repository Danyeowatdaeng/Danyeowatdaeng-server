package com.tourapi.tourapi.mypet.repository;

import com.tourapi.tourapi.mypet.domain.PetDiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetDiaryRepository extends JpaRepository<PetDiary, Long> {

    // 특정 회원의 삭제되지 않은 다이어리 목록 조회 (페이징)
    Page<PetDiary> findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 특정 회원의 특정 다이어리 조회 (삭제되지 않은 것만)
    Optional<PetDiary> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);

    // 특정 회원의 다이어리 개수
    long countByMemberIdAndDeletedFalse(Long memberId);
}