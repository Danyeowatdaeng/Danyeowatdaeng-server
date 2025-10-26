package com.tourapi.tourapi.map.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExternalTourApiResponse<T> {
    private String resultCode;
    private String resultMsg;
    private List<T> items;
}


