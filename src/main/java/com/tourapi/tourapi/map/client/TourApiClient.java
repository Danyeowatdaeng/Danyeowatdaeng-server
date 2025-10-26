package com.tourapi.tourapi.map.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourapi.tourapi.config.TourApiProperties;
import com.tourapi.tourapi.map.dto.ExternalTourApiResponse;
import com.tourapi.tourapi.map.dto.ExternalTourLocationDto;
import com.tourapi.tourapi.map.dto.ExternalFestivalDto;
import com.tourapi.tourapi.map.dto.DetailIntroResponse;
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

    private ExternalTourApiResponse<ExternalTourLocationDto> fetchTourDataByLocation(Double latitude, Double longitude,
                                                           Integer radius, Integer category) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("listYN", "Y");
        params.add("arrange", "A");
        params.add("mapY", String.valueOf(latitude));
        params.add("mapX", String.valueOf(longitude));
        if (radius != null) params.add("radius", String.valueOf(radius));
        if (category != null) params.add("contentTypeId", String.valueOf(category));


        String requestUrl = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
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

    public ExternalTourApiResponse<ExternalTourLocationDto> fetchTourDataByKeyword(String keyword, Integer pageNo, Integer numOfRows,
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

        String requestUrl = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
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


    public ExternalTourApiResponse<ExternalTourLocationDto> fetchTourDataByBounds(Double swLat, Double swLng, Double neLat, Double neLng, 
                                                        Integer category, Integer zoomLevel, boolean useJson) {
        // 영역 중심점 계산 (남서-북동 좌표 기준)
        Double centerLat = (swLat + neLat) / 2;
        Double centerLng = (swLng + neLng) / 2;
        
        // 줌 레벨에 따른 반경 계산
        Integer radius = calculateRadiusByZoom(zoomLevel);
        
        // 중심점 기반 위치 검색 API 호출
        return fetchTourDataByLocation(centerLat, centerLng, radius, category);
    }

    private Integer calculateRadiusByZoom(Integer zoomLevel) {
        if (zoomLevel == null) return 1000; // 기본값
        
        // 줌 레벨에 따른 반경 매핑 (미터 단위) - 줌 1이 가장 가까운 거리
        return switch (zoomLevel) {
            case 1 -> 200;     // 바로 근처
            case 2 -> 500;     // 매우 근처
            case 3 -> 1000;    // 근처
            case 4 -> 2000;    // 상세 지역
            case 5 -> 5000;    // 읍/면/동
            case 6 -> 10000;   // 시/군/구
            case 7 -> 25000;   // 광역시/도
            case 8 -> 50000;   // 전국
            default -> 1000;   // 기본값
        };
    }

    private ExternalTourApiResponse<ExternalTourLocationDto> parseJsonResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String resultCode = root.path("response").path("header").path("resultCode").asText(null);
            String resultMsg = root.path("response").path("header").path("resultMsg").asText(null);

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                // 일부 API는 items가 빈 객체/배열일 수 있음
                itemsNode = root.path("response").path("body").path("items");
            }

            ExternalTourApiResponse<ExternalTourLocationDto> resp = new ExternalTourApiResponse<>();
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
            return new ExternalTourApiResponse<>();
        }
    }

    private String snippet(String body) {
        if (body == null) return null;
        int max = 1000;
        return body.length() <= max ? body : body.substring(0, max) + "...";
    }

    public ExternalTourApiResponse<ExternalFestivalDto> fetchFestivalData(Integer pageNo, Integer numOfRows, boolean useJson) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("pageNo", String.valueOf(pageNo != null ? pageNo : 1));
        params.add("numOfRows", String.valueOf(numOfRows != null ? numOfRows : 10));
        params.add("arrange", "C");  // 제목순 정렬
        params.add("listYN", "Y");
        params.add("contentTypeId", "15");  // 축제/행사

        String requestUrl = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
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
                            return parseFestivalJsonResponse(body);
                        });
                    }
                    return res.bodyToMono(String.class).map(body -> {
                        log.warn("[TourAPI] Non-JSON response received ({}), body={}", ct, snippet(body));
                        return new ExternalTourApiResponse();
                    });
                })
                .block();
    }

    private ExternalTourApiResponse<ExternalFestivalDto> parseFestivalJsonResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String resultCode = root.path("response").path("header").path("resultCode").asText(null);
            String resultMsg = root.path("response").path("header").path("resultMsg").asText(null);

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                itemsNode = root.path("response").path("body").path("items");
            }

            ExternalTourApiResponse<ExternalFestivalDto> resp = new ExternalTourApiResponse<>();
            resp.setResultCode(resultCode);
            resp.setResultMsg(resultMsg);

            if (itemsNode.isArray()) {
                resp.setItems(objectMapper.convertValue(itemsNode, new TypeReference<java.util.List<ExternalFestivalDto>>(){}));
            } else if (itemsNode.isObject()) {
                ExternalFestivalDto one = objectMapper.convertValue(itemsNode, ExternalFestivalDto.class);
                resp.setItems(java.util.List.of(one));
            } else {
                resp.setItems(java.util.List.of());
            }
            return resp;
        } catch (Exception e) {
            log.error("[TourAPI] Festival JSON parse error: {}", e.getMessage());
            return new ExternalTourApiResponse<>();
        }
    }

    public DetailIntroResponse fetchDetailIntro(Long contentId, Integer contentTypeId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("MobileOS", "ETC");
        params.add("MobileApp", "AppTest");
        params.add("contentId", String.valueOf(contentId));
        params.add("contentTypeId", String.valueOf(contentTypeId));

        String requestUrl = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(properties.getDetailIntroPath())
                .queryParams(params)
                .queryParam("serviceKey", properties.getServiceKey())
                .build(false)
                .toUriString() + "&_type=json";
        log.info("[TourAPI] GET {}", requestUrl);

        String body = tourApiWebClient.get()
                .uri(java.net.URI.create(requestUrl))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(body);
            String resultCode = root.path("response").path("header").path("resultCode").asText();
            if (!"0000".equals(resultCode)) {
                return null; // 상위 계층에서 에러 매핑
            }
            JsonNode item = root.path("response").path("body").path("items").path("item");
            if (item.isArray() && item.size() > 0) item = item.get(0);

            java.util.Iterator<String> fields = item.fieldNames();
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            while (fields.hasNext()) {
                String f = fields.next();
                map.put(f, objectMapper.convertValue(item.path(f), Object.class));
            }

            return DetailIntroResponse.builder()
                    .contentId(contentId)
                    .contentTypeId(contentTypeId)
                    .details(map)
                    .build();
        } catch (Exception e) {
            log.error("[TourAPI] detailIntro parse error: {}", e.getMessage());
            return null;
        }
    }
}


