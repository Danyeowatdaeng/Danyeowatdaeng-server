package com.tourapi.tourapi.map.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourLocation {
    private Long id;
    private String title;
    private Integer category;      // contenttypeid
    private String address;        // addr1 + addr2
    private String description;
    private String imageUrl1;
    private String imageUrl2;
    private Double latitude;       // mapy
    private Double longitude;      // mapx
    private Integer distance;
    private String phoneNumber;    // tel
    private String homepageUrl;    // homepage
}


