package com.tourapi.tourapi.quest.enums;

public enum QuestType {
    WALK_DAILY("산책하기", "일일 산책 완료", 20),
    DIARY_DAILY("다이어리 남기기", "일일 다이어리 작성", 30),
    REVIEW_DAILY("리뷰 남기기", "일일 리뷰 작성", 50);

    private final String displayName;
    private final String description;
    private final Integer defaultRewardPoints;

    QuestType(String displayName, String description, Integer defaultRewardPoints) {
        this.displayName = displayName;
        this.description = description;
        this.defaultRewardPoints = defaultRewardPoints;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Integer getDefaultRewardPoints() { return defaultRewardPoints; }
}