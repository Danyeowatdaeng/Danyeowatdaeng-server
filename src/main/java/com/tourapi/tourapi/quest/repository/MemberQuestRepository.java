package com.tourapi.tourapi.quest.repository;

import com.tourapi.tourapi.quest.domain.MemberQuest;
import com.tourapi.tourapi.quest.enums.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberQuestRepository extends JpaRepository<MemberQuest, Long> {

    List<MemberQuest> findByMemberIdAndQuestDate(Long memberId, LocalDate questDate);

    Optional<MemberQuest> findByMemberIdAndQuestTypeAndQuestDate(
            Long memberId, QuestType questType, LocalDate questDate);

    @Query("SELECT mq FROM MemberQuest mq JOIN FETCH mq.quest q " +
            "WHERE mq.member.id = :memberId AND mq.questDate = :questDate")
    List<MemberQuest> findByMemberIdAndQuestDateWithQuest(
            @Param("memberId") Long memberId,
            @Param("questDate") LocalDate questDate);
}