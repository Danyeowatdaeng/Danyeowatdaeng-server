package com.tourapi.tourapi.quest.repository;

import com.tourapi.tourapi.quest.domain.Quest;
import com.tourapi.tourapi.quest.enums.QuestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findByIsActiveTrueAndIsDailyTrue();

    Optional<Quest> findByTypeAndIsActiveTrue(QuestType type);
}