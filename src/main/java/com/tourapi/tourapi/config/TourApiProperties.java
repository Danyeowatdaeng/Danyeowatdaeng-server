package com.tourapi.tourapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tour.api")
@Data
public class TourApiProperties {
    private String baseUrl = "http://apis.data.go.kr/B551011/KorPetTourService";
    private String serviceKey;
    private String locationBasedListPath = "/locationBasedList";
    private String searchKeywordPath = "/searchKeyword";
    private int connectionTimeout = 5000;
    private int readTimeout = 10000;
    private int maxRetries = 3;
}


