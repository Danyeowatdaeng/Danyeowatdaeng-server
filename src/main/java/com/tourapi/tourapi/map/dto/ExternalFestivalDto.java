package com.tourapi.tourapi.map.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalFestivalDto {
    private String title;           // 축제명
    private String addr1;           // 주소1
    private String addr2;           // 주소2
    private String tel;             // 전화번호
    private String homepage;        // 홈페이지
    private String firstimage;      // 대표이미지1
    private String firstimage2;     // 대표이미지2
    private String mapx;            // 경도
    private String mapy;            // 위도
    private String contenttypeid;   // 콘텐츠타입ID (15)
    private String contentid;       // 콘텐츠ID
    private String eventstartdate;  // 축제 시작일
    private String eventenddate;    // 축제 종료일
    private String overview;        // 축제 개요
}
