package com.tourapi.tourapi.map.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourapi.tourapi.config.TourApiProperties;
import com.tourapi.tourapi.map.dto.ExternalTourApiResponse;
import com.tourapi.tourapi.map.dto.ExternalTourLocationDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class TourApiClient {

    private final WebClient tourApiWebClient;
    private final TourApiProperties properties;
    private static final Logger log = LoggerFactory.getLogger(TourApiClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExternalTourApiResponse fetchTourDataByLocation(Double latitude, Double longitude,
                                                           Integer radius, Integer category, boolean useJson) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("listYN", "Y");
        params.add("arrange", "A");
        params.add("mapY", String.valueOf(latitude));
        params.add("mapX", String.valueOf(longitude));
        if (radius != null) params.add("radius", String.valueOf(radius));
        if (category != null) params.add("contentTypeId", String.valueOf(category));


        String requestUrl = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path(properties.getLocationBasedListPath())
                .queryParams(params)
                .queryParam("serviceKey", properties.getServiceKey())
                .build(false) // keep original (avoid extra encoding)
                .toUriString() + "&_type=json"; // ensure _type=json is the last query param
        log.info("[TourAPI] GET {}", requestUrl);

        return tourApiWebClient.get()
                .uri(java.net.URI.create(requestUrl))
                .exchangeToMono(res -> {
                    MediaType ct = res.headers().contentType().orElse(null);
                    log.info("[TourAPI] Response status={}, contentType={}", res.statusCode(), ct);
                    if (ct != null && ct.includes(MediaType.APPLICATION_JSON)) {
                        return res.bodyToMono(String.class).map(body -> {
                            log.info("[TourAPI] Response body(json)={}", snippet(body));
                            return parseJsonResponse(body);
                        });
                    }
                    return res.bodyToMono(String.class).map(body -> {
                        log.warn("[TourAPI] Non-JSON response received ({}), body={}", ct, snippet(body));
                        return new ExternalTourApiResponse();
                    });
                })
                .block();
    }

    public ExternalTourApiResponse fetchTourDataByKeyword(String keyword, Integer pageNo, Integer numOfRows,
                                                          boolean useJson) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        // 키워드는 URL 인코딩하여 전달 (build(false) 사용으로 인코딩 보존)
        String encodedKeyword = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
        params.add("keyword", encodedKeyword);
        if (pageNo != null) params.add("pageNo", String.valueOf(pageNo));
        if (numOfRows != null) params.add("numOfRows", String.valueOf(numOfRows));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path(properties.getSearchKeywordPath())
                .queryParams(params)
                .queryParam("serviceKey", properties.getServiceKey())
                .build(false)
                .toUriString() + "&_type=json";
        log.info("[TourAPI] GET {}", requestUrl);

        return tourApiWebClient.get()
                .uri(java.net.URI.create(requestUrl))
                .exchangeToMono(res -> {
                    MediaType ct = res.headers().contentType().orElse(null);
                    log.info("[TourAPI] Response status={}, contentType={}", res.statusCode(), ct);
                    if (ct != null && ct.includes(MediaType.APPLICATION_JSON)) {
                        return res.bodyToMono(String.class).map(body -> {
                            log.info("[TourAPI] Response body(json)={}", snippet(body));
                            return parseJsonResponse(body);
                        });
                    }
                    return res.bodyToMono(String.class).map(body -> {
                        log.warn("[TourAPI] Non-JSON response received ({}), body={}", ct, snippet(body));
                        return new ExternalTourApiResponse();
                    });
                })
                .block();
    }

    public ExternalTourApiResponse fetchTourDataByCategory(Integer category, Integer pageNo, Integer numOfRows,
                                                          boolean useJson) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("listYN", "Y");
        params.add("arrange", "A");
        if (category != null) params.add("contentTypeId", String.valueOf(category));
        if (pageNo != null) params.add("pageNo", String.valueOf(pageNo));
        if (numOfRows != null) params.add("numOfRows", String.valueOf(numOfRows));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path(properties.getAreaBasedListPath())
                .queryParams(params)
                .queryParam("serviceKey", properties.getServiceKey())
                .build(false)
                .toUriString() + "&_type=json";
        log.info("[TourAPI] GET {}", requestUrl);

        return tourApiWebClient.get()
                .uri(java.net.URI.create(requestUrl))
                .exchangeToMono(res -> {
                    MediaType ct = res.headers().contentType().orElse(null);
                    log.info("[TourAPI] Response status={}, contentType={}", res.statusCode(), ct);
                    if (ct != null && ct.includes(MediaType.APPLICATION_JSON)) {
                        return res.bodyToMono(String.class).map(body -> {
                            log.info("[TourAPI] Response body(json)={}", snippet(body));
                            return parseJsonResponse(body);
                        });
                    }
                    return res.bodyToMono(String.class).map(body -> {
                        log.warn("[TourAPI] Non-JSON response received ({}), body={}", ct, snippet(body));
                        return new ExternalTourApiResponse();
                    });
                })
                .block();
    }

    private ExternalTourApiResponse parseJsonResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String resultCode = root.path("response").path("header").path("resultCode").asText(null);
            String resultMsg = root.path("response").path("header").path("resultMsg").asText(null);

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                // 일부 API는 items가 빈 객체/배열일 수 있음
                itemsNode = root.path("response").path("body").path("items");
            }

            ExternalTourApiResponse resp = new ExternalTourApiResponse();
            resp.setResultCode(resultCode);
            resp.setResultMsg(resultMsg);

            if (itemsNode.isArray()) {
                resp.setItems(objectMapper.convertValue(itemsNode, new TypeReference<java.util.List<ExternalTourLocationDto>>(){}));
            } else if (itemsNode.isObject()) {
                // 단일 객체인 경우 리스트로 감싸기
                ExternalTourLocationDto one = objectMapper.convertValue(itemsNode, ExternalTourLocationDto.class);
                resp.setItems(java.util.List.of(one));
            } else {
                resp.setItems(java.util.List.of());
            }
            return resp;
        } catch (Exception e) {
            log.error("[TourAPI] JSON parse error: {}", e.getMessage());
            return new ExternalTourApiResponse();
        }
    }

    private String snippet(String body) {
        if (body == null) return null;
        int max = 1000;
        return body.length() <= max ? body : body.substring(0, max) + "...";
    }
}


