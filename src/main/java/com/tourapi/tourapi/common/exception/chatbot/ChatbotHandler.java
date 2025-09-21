package com.tourapi.tourapi.common.exception.chatbot;

import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotErrorStatus;
import com.tourapi.tourapi.common.exception.general.GeneralException;

public class ChatbotHandler extends GeneralException {
    public ChatbotHandler(ChatbotErrorStatus status) {
        super(status);
    }
}