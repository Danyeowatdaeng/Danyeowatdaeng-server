package com.tourapi.tourapi.mypet.enums;

public enum DiaryStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");

    private final String displayName;

    DiaryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}