package com.tourapi.tourapi.point.enums;

public enum PointType {
    WALK_DAILY("일일 산책 적립", 20),
    DIARY_DAILY("일일 다이어리 적립", 30),
    MANUAL_EARN("수동 적립", 0),
    MANUAL_SPEND("수동 사용", 0),
    EVENT_EARN("이벤트 적립", 0),
    PURCHASE_SPEND("구매 사용", 0);

    private final String description;
    private final Integer defaultAmount;

    PointType(String description, Integer defaultAmount) {
        this.description = description;
        this.defaultAmount = defaultAmount;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDefaultAmount() {
        return defaultAmount;
    }
}