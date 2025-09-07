package com.tourapi.tourapi.chatbot.repository;

import com.tourapi.tourapi.chatbot.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 세션 메시지 조회 (최신순)
    List<ChatMessage> findByMemberIdAndSessionIdAndIsActiveTrueOrderByCreatedAtAsc(Long memberId, String sessionId);

    // 모든 세션 조회 (최신 세션순)
    @Query("SELECT DISTINCT cm.sessionId FROM ChatMessage cm WHERE cm.member.id = :memberId AND cm.isActive = true ORDER BY MAX(cm.createdAt) DESC")
    List<String> findDistinctSessionIdsByMemberIdOrderByLatestMessageDesc(@Param("memberId") Long memberId);

    // 회원의 메시지 개수 조회
    long countByMemberIdAndIsActiveTrue(Long memberId);

    // 세션의 메시지 개수 조회
    long countByMemberIdAndSessionIdAndIsActiveTrue(Long memberId, String sessionId);

    // 회원의 최근 메시지 조회 (페이징)
    Page<ChatMessage> findByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 기간 내 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.member.id = :memberId AND cm.isActive = true " +
            "AND cm.createdAt BETWEEN :startDate AND :endDate ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByMemberIdAndDateRange(@Param("memberId") Long memberId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // 세션의 최근 메시지들 조회 (대화 컨텍스트용)
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.member.id = :memberId AND cm.sessionId = :sessionId " +
            "AND cm.isActive = true ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessagesBySession(@Param("memberId") Long memberId,
                                                  @Param("sessionId") String sessionId,
                                                  Pageable pageable);

    // 회원의 세션별 마지막 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.member.id = :memberId AND cm.isActive = true " +
            "AND cm.id IN (SELECT MAX(cm2.id) FROM ChatMessage cm2 WHERE cm2.member.id = :memberId " +
            "AND cm2.isActive = true GROUP BY cm2.sessionId) ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLastMessageBySession(@Param("memberId") Long memberId);
}