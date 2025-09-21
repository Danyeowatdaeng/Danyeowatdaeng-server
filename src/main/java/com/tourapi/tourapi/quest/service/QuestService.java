// src/main/java/com/tourapi/tourapi/quest/service/QuestService.java
package com.tourapi.tourapi.quest.service;

import com.tourapi.tourapi.quest.dto.DailyQuestSummaryResponse;
import com.tourapi.tourapi.quest.enums.QuestType;

import java.time.LocalDate;

public interface QuestService {

    /**
     * 오늘의 퀘스트 달성률 조회
     */
    DailyQuestSummaryResponse getTodayQuestProgress(Long memberId);

    /**
     * 퀘스트 진행도 업데이트 (내부용)
     */
    void updateQuestProgress(Long memberId, QuestType questType, Integer incrementCount);

    /**
     * 일일 퀘스트 초기화 (내부용)
     */
    void initializeDailyQuests(Long memberId, LocalDate date);
}