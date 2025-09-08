package com.tourapi.tourapi.map.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailAggregate {
    private Long contentId;
    private Integer contentTypeId;
    private Map<String, Object> summary; // title, address, rating, reviewCount 등(선택)
    private Map<String, Object> intro;   // 외부 detailIntro 맵핑
    private List<Map<String, Object>> events; // 간단한 리스트 형태(스켈레톤)
    private List<Map<String, Object>> reviews; // 페이지 없이 샘플(스켈레톤)
}


