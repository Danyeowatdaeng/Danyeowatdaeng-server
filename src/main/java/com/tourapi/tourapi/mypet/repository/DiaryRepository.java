package com.tourapi.tourapi.mypet.repository;

import com.tourapi.tourapi.mypet.Diary;
import com.tourapi.tourapi.mypet.enums.DiaryStatus;
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

    // 활성 다이어리 조회 (소프트 삭제 제외)
    List<Diary> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, DiaryStatus status);

    // 페이징된 활성 다이어리 조회
    Page<Diary> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, DiaryStatus status, Pageable pageable);

    // 다이어리 개수
    long countByMemberIdAndStatus(Long memberId, DiaryStatus status);

    // 특정 회원의 특정 다이어리 조회 (활성 상태만)
    Optional<Diary> findByIdAndMemberIdAndStatus(Long id, Long memberId, DiaryStatus status);

    // 특정 회원의 다이어리 존재 여부 확인
    boolean existsByIdAndMemberIdAndStatus(Long id, Long memberId, DiaryStatus status);

    // 특정 기간 내 다이어리 조회
    @Query("SELECT d FROM Diary d WHERE d.member.id = :memberId AND d.status = :status " +
            "AND d.createdAt BETWEEN :startDate AND :endDate ORDER BY d.createdAt DESC")
    List<Diary> findByMemberIdAndStatusAndCreatedAtBetween(
            @Param("memberId") Long memberId,
            @Param("status") DiaryStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 제목으로 검색
    List<Diary> findByMemberIdAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long memberId, DiaryStatus status, String title);

    // 최근 N개 다이어리 조회
    @Query("SELECT d FROM Diary d WHERE d.member.id = :memberId AND d.status = :status " +
            "ORDER BY d.createdAt DESC")
    List<Diary> findTopNByMemberIdAndStatus(@Param("memberId") Long memberId,
                                            @Param("status") DiaryStatus status,
                                            Pageable pageable);
}