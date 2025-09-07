package com.tourapi.tourapi.chatbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {

    private String apiKey;
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String model = "gemini-1.5-flash";
    private Integer maxTokens = 1000;
    private Double temperature = 0.7;
    private String systemPrompt = "당신은 반려동물 전문 상담사입니다. 반려동물과 관련된 질문에 친절하고 정확하게 답변해주세요. 한국어로 답변하며, 전문적이면서도 따뜻한 톤으로 응답해주세요.";

    @Bean
    public RestTemplate geminiRestTemplate() {
        return new RestTemplate();
    }
}