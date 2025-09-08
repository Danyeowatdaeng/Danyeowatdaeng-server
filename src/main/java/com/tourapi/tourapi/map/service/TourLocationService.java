package com.tourapi.tourapi.map.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tourapi.tourapi.map.adapter.TourLocationAdapter;
import com.tourapi.tourapi.map.domain.TourLocation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TourLocationService {

    private final TourLocationAdapter tourLocationAdapter;


    public List<TourLocation> searchByKeyword(String keyword, Pageable pageable) {
        return tourLocationAdapter.fetchTourLocationsByKeyword(keyword, pageable, true);
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


