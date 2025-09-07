package com.tourapi.tourapi.map.client;

import com.tourapi.tourapi.map.config.TourApiProperties;
import com.tourapi.tourapi.map.dto.ExternalTourApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private final WebClient tourApiWebClient;
    private final TourApiProperties properties;

    public ExternalTourApiResponse fetchTourDataByLocation(Double latitude, Double longitude,
                                                           Integer radius, Integer category, boolean useJson) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("mapY", String.valueOf(latitude));
        params.add("mapX", String.valueOf(longitude));
        if (radius != null) params.add("radius", String.valueOf(radius));
        if (category != null) params.add("contentTypeId", String.valueOf(category));
        if (useJson) params.add("_type", "json");

        return tourApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getLocationBasedListPath())
                        .queryParams(params)
                        .queryParam("serviceKey", properties.getServiceKey())
                        .build())
                .retrieve()
                .bodyToMono(ExternalTourApiResponse.class)
                .block();
    }

    public ExternalTourApiResponse fetchTourDataByKeyword(String keyword, Integer pageNo, Integer numOfRows,
                                                          boolean useJson) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("keyword", keyword);
        if (pageNo != null) params.add("pageNo", String.valueOf(pageNo));
        if (numOfRows != null) params.add("numOfRows", String.valueOf(numOfRows));
        if (useJson) params.add("_type", "json");

        return tourApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getSearchKeywordPath())
                        .queryParams(params)
                        .queryParam("serviceKey", properties.getServiceKey())
                        .build())
                .retrieve()
                .bodyToMono(ExternalTourApiResponse.class)
                .block();
    }
}


