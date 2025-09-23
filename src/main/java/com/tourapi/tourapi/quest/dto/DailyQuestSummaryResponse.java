package com.tourapi.tourapi.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DailyQuestSummaryResponse {

    @Schema(description = "전체 퀘스트 개수")
    private Integer totalQuests;

    @Schema(description = "완료된 퀘스트 개수")
    private Integer completedQuests;

    @Schema(description = "전체 달성률 (0-100)")
    private Integer overallProgressPercentage;

    @Schema(description = "개별 퀘스트 진행 상황")
    private List<QuestProgressResponse> questProgresses;

    public static DailyQuestSummaryResponse from(List<QuestProgressResponse> quests) {
        int totalQuests = quests.size();
        int completedQuests = (int) quests.stream().mapToLong(q -> q.getIsCompleted() ? 1 : 0).sum();
        int overallProgress = totalQuests > 0 ? (completedQuests * 100 / totalQuests) : 0;

        return DailyQuestSummaryResponse.builder()
                .totalQuests(totalQuests)
                .completedQuests(completedQuests)
                .overallProgressPercentage(overallProgress)
                .questProgresses(quests)
                .build();
    }
}