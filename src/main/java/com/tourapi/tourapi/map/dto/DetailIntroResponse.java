package com.tourapi.tourapi.map.dto;

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
public class DetailIntroResponse {
    private Long contentId;
    private Integer contentTypeId;
    private Map<String, Object> details; // contentTypeId 별 상이한 필드를 유연하게 보관
}


