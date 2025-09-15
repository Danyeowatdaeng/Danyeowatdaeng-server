package com.tourapi.tourapi.map.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tourapi.tourapi.map.adapter.TourLocationAdapter;
import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.dto.CommunityFacilityDto;
import com.tourapi.tourapi.map.dto.DetailIntroResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourLocationService {

    private final TourLocationAdapter tourLocationAdapter;
    private final CommunityFacilityCsvService communityFacilityCsvService;


    public List<CommunityFacilityDto> searchByKeyword(String keyword, Pageable pageable) {
        long start = System.currentTimeMillis();
        List<CommunityFacilityCsvService.Row> rows = communityFacilityCsvService.searchByKeyword(keyword, pageable);
        List<CommunityFacilityDto> results = rows.stream()
                .filter(Objects::nonNull)
                .map(r -> CommunityFacilityDto.builder()
                        .name(r.name())
                        .category3(r.category3())
                        .roadAddress(r.roadAddress())
                        .jibunAddress(r.jibunAddress())
                        .homepage(r.homepage())
                        .closedDays(r.closedDays())
                        .openingHours(r.openingHours())
                        .latitude(r.latitude())
                        .longitude(r.longitude())
                        .phone(r.phone())
                        .source("CSV")
                        .build())
                .collect(Collectors.toList());
        
        long ms = System.currentTimeMillis() - start;
        log.info("TourLocationService 검색 완료: '{}' → DTO 변환 {}건, 총 {} ms 소요", 
                keyword, results.size(), ms);
        return results;
    }

    private String buildAddress(String road, String jibun) {
        if (road != null && !road.isBlank() && jibun != null && !jibun.isBlank()) {
            return road + " | " + jibun;
        }
        return road != null && !road.isBlank() ? road : jibun;
    }


    public List<TourLocation> searchByBounds(Double swLat, Double swLng, Double neLat, Double neLng, Integer category, Integer zoomLevel) {
        // 영역 중심점 계산 (남서-북동 좌표 기준)
        Double centerLat = (swLat + neLat) / 2;
        Double centerLng = (swLng + neLng) / 2;
        
        // 영역 기반 검색 수행
        List<TourLocation> results = tourLocationAdapter.fetchTourLocationsByBounds(swLat, swLng, neLat, neLng, category, zoomLevel, true);
        
        // 중심점 기준으로 거리순 정렬
        return sortByDistance(results, centerLat, centerLng);
    }

    public List<TourLocation> sortByDistance(List<TourLocation> locations, Double centerLat, Double centerLng) {
        if (locations == null) return List.of();
        return locations.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(loc -> haversine(centerLat, centerLng, loc.getLatitude(), loc.getLongitude())))
                .toList();
    }

    public DetailIntroResponse getDetailIntro(Long contentId, Integer contentTypeId) {
        return tourLocationAdapter.fetchDetailIntro(contentId, contentTypeId);
    }

    // 서비스 레벨에서는 반경 계산을 수행하지 않습니다 (클라이언트에서 계산).

    private double haversine(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.MAX_VALUE;
        double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


