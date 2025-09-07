package com.tourapi.tourapi.mypet.repository;

import com.tourapi.tourapi.mypet.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 다이어리 목록 조회
    List<Diary> findByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(Long memberId);

    // 다이어리 페이징 조회
    Page<Diary> findByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 다이어리 조회
    Optional<Diary> findByIdAndMemberIdAndIsActiveTrue(Long id, Long memberId);

    // 다이어리 개수 조회
    long countByMemberIdAndIsActiveTrue(Long memberId);

    // 특정 기간 내 다이어리 조회
    @Query("SELECT d FROM Diary d WHERE d.member.id = :memberId AND d.isActive = true " +
            "AND d.createdAt BETWEEN :startDate AND :endDate ORDER BY d.createdAt DESC")
    List<Diary> findByMemberIdAndDateRange(@Param("memberId") Long memberId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // 최근 다이어리 조회
    Optional<Diary> findTopByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(Long memberId);
}