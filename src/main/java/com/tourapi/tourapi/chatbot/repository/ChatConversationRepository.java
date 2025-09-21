package com.tourapi.tourapi.chatbot.repository;

import com.tourapi.tourapi.chatbot.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    // 특정 회원의 특정 대화방 조회 (삭제되지 않은 것만)
    Optional<ChatConversation> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);

    // 메시지를 포함한 대화방 조회
    @Query("SELECT c FROM ChatConversation c LEFT JOIN FETCH c.messages m WHERE c.id = :id AND c.member.id = :memberId AND c.deleted = false ORDER BY m.createdAt ASC")
    Optional<ChatConversation> findByIdAndMemberIdWithMessages(@Param("id") Long id, @Param("memberId") Long memberId);

    // 특정 회원의 가장 최근 대화방 조회 (없으면 생성)
    @Query("SELECT c FROM ChatConversation c WHERE c.member.id = :memberId AND c.deleted = false ORDER BY c.updatedAt DESC")
    List<ChatConversation> findLatestByMemberId(@Param("memberId") Long memberId);
}