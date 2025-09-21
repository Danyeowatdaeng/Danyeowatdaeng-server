package com.tourapi.tourapi.chatbot.enums;

public enum MessageRole {
    USER("사용자"),
    ASSISTANT("AI 어시스턴트"),
    SYSTEM("시스템");

    private final String description;

    MessageRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}