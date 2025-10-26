package com.tourapi.tourapi.map.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Festival {
    private Long id;
    private String title;
    private String address;
    private String description;
    private String imageUrl1;
    private String imageUrl2;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private String homepageUrl;
    private String startDate;
    private String endDate;
    private String overview;
}
