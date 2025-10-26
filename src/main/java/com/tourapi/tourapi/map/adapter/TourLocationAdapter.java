package com.tourapi.tourapi.map.adapter;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.tourapi.tourapi.map.client.TourApiClient;
import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.domain.Festival;
import com.tourapi.tourapi.map.dto.ExternalTourApiResponse;
import com.tourapi.tourapi.map.dto.ExternalTourLocationDto;
import com.tourapi.tourapi.map.dto.ExternalFestivalDto;
import com.tourapi.tourapi.map.dto.DetailIntroResponse;
import com.tourapi.tourapi.map.mapper.TourLocationMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TourLocationAdapter {

    private final TourApiClient tourApiClient;
    private final TourLocationMapper tourLocationMapper;


    public List<TourLocation> fetchTourLocationsByKeyword(String keyword, Pageable pageable, boolean useJson) {
        Integer pageNo = pageable != null ? pageable.getPageNumber() + 1 : null;
        Integer size = pageable != null ? pageable.getPageSize() : null;
        ExternalTourApiResponse<ExternalTourLocationDto> response = tourApiClient.fetchTourDataByKeyword(keyword, pageNo, size, useJson);
        return tourLocationMapper.toTourLocationList(response != null ? response.getItems() : null);
    }


    public List<TourLocation> fetchTourLocationsByBounds(Double swLat, Double swLng, Double neLat, Double neLng, 
                                                        Integer category, Integer zoomLevel, boolean useJson) {
        ExternalTourApiResponse<ExternalTourLocationDto> response = tourApiClient.fetchTourDataByBounds(swLat, swLng, neLat, neLng, category, zoomLevel, useJson);
        return tourLocationMapper.toTourLocationList(response != null ? response.getItems() : null);
    }

    public List<Festival> fetchFestivals(Integer pageNo, Integer numOfRows, boolean useJson) {
        ExternalTourApiResponse<ExternalFestivalDto> response = tourApiClient.fetchFestivalData(pageNo, numOfRows, useJson);
        return mapToFestivals(response != null ? response.getItems() : null);
    }

    private List<Festival> mapToFestivals(List<ExternalFestivalDto> festivalDtos) {
        if (festivalDtos == null) return List.of();
        
        return festivalDtos.stream()
                .map(this::mapToFestival)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private Festival mapToFestival(ExternalFestivalDto dto) {
        try {
            return Festival.builder()
                    .id(Long.parseLong(dto.getContentid()))
                    .title(dto.getTitle())
                    .address(buildAddress(dto.getAddr1(), dto.getAddr2()))
                    .description(dto.getOverview())
                    .imageUrl1(dto.getFirstimage())
                    .imageUrl2(dto.getFirstimage2())
                    .latitude(parseDouble(dto.getMapy()))
                    .longitude(parseDouble(dto.getMapx()))
                    .phoneNumber(dto.getTel())
                    .homepageUrl(dto.getHomepage())
                    .startDate(dto.getEventstartdate())
                    .endDate(dto.getEventenddate())
                    .overview(dto.getOverview())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private String buildAddress(String addr1, String addr2) {
        if (addr1 != null && !addr1.isBlank() && addr2 != null && !addr2.isBlank()) {
            return addr1 + " " + addr2;
        }
        return addr1 != null && !addr1.isBlank() ? addr1 : addr2;
    }

    private Double parseDouble(String value) {
        try {
            return value != null && !value.isBlank() ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public DetailIntroResponse fetchDetailIntro(Long contentId, Integer contentTypeId) {
        return tourApiClient.fetchDetailIntro(contentId, contentTypeId);
    }
}


