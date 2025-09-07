package com.tourapi.tourapi.map.adapter;

import com.tourapi.tourapi.map.client.TourApiClient;
import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.dto.ExternalTourApiResponse;
import com.tourapi.tourapi.map.mapper.TourLocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TourLocationAdapter {

    private final TourApiClient tourApiClient;
    private final TourLocationMapper tourLocationMapper;

    public List<TourLocation> fetchTourLocationsByLocation(Double latitude, Double longitude,
                                                           Integer radius, Integer category, boolean useJson) {
        ExternalTourApiResponse response = tourApiClient.fetchTourDataByLocation(latitude, longitude, radius, category, useJson);
        return tourLocationMapper.toTourLocationList(response != null ? response.getItems() : null);
    }

    public List<TourLocation> fetchTourLocationsByKeyword(String keyword, Pageable pageable, boolean useJson) {
        Integer pageNo = pageable != null ? pageable.getPageNumber() + 1 : null;
        Integer size = pageable != null ? pageable.getPageSize() : null;
        ExternalTourApiResponse response = tourApiClient.fetchTourDataByKeyword(keyword, pageNo, size, useJson);
        return tourLocationMapper.toTourLocationList(response != null ? response.getItems() : null);
    }
}


