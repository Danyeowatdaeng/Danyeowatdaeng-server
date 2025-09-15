package com.tourapi.tourapi.map.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityFacilityDto {
    private String name;           // 시설명
    private String category3;      // 카테고리3
    private String roadAddress;    // 도로명주소
    private String jibunAddress;   // 지번주소
    private String homepage;       // 홈페이지
    private String closedDays;     // 휴무일
    private String openingHours;   // 운영시간
    private Double latitude;       // 위도
    private Double longitude;      // 경도
    private String phone;          // 전화번호
    private String source;         // "CSV"
}


