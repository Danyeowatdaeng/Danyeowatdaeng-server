package com.tourapi.tourapi.quest.dto;

import com.tourapi.tourapi.quest.domain.MemberQuest;
import com.tourapi.tourapi.quest.enums.QuestType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestProgressResponse {

    @Schema(description = "퀘스트 타입")
    private QuestType questType;

    @Schema(description = "퀘스트 제목")
    private String title;

    @Schema(description = "퀘스트 설명")
    private String description;

    @Schema(description = "현재 진행도")
    private Integer currentCount;

    @Schema(description = "목표 개수")
    private Integer targetCount;

    @Schema(description = "달성률 (0-100)")
    private Integer progressPercentage;

    @Schema(description = "완료 여부")
    private Boolean isCompleted;

    @Schema(description = "보상 포인트")
    private Integer rewardPoints;

    @Schema(description = "보상 수령 여부")
    private Boolean isRewardClaimed;

    public static QuestProgressResponse from(MemberQuest memberQuest) {
        int percentage = (int) Math.min(100,
                (memberQuest.getCurrentCount() * 100.0 / memberQuest.getQuest().getTargetCount()));

        return QuestProgressResponse.builder()
                .questType(memberQuest.getQuestType())
                .title(memberQuest.getQuest().getTitle())
                .description(memberQuest.getQuest().getDescription())
                .currentCount(memberQuest.getCurrentCount())
                .targetCount(memberQuest.getQuest().getTargetCount())
                .progressPercentage(percentage)
                .isCompleted(memberQuest.getIsCompleted())
                .rewardPoints(memberQuest.getQuest().getRewardPoints())
                .isRewardClaimed(memberQuest.getIsRewardClaimed())
                .build();
    }
}