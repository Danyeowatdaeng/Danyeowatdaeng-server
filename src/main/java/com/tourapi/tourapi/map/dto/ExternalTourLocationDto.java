package com.tourapi.tourapi.map.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalTourLocationDto {
    private String title;
    private String addr1;
    private String addr2;
    private String tel;
    private String homepage;
    private String firstimage;
    private String firstimage2;
    private String mapx;
    private String mapy;
    private String contenttypeid;
    private String contentid;
}


