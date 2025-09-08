package com.tourapi.tourapi.map.service;

import com.tourapi.tourapi.map.adapter.TourLocationAdapter;
import com.tourapi.tourapi.map.domain.TourLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TourLocationService {

    private final TourLocationAdapter tourLocationAdapter;

    public List<TourLocation> searchByLocation(Double latitude, Double longitude, Integer radius, Integer category) {
        return tourLocationAdapter.fetchTourLocationsByLocation(latitude, longitude, radius, category, true);
    }

    public List<TourLocation> searchByKeyword(String keyword, Pageable pageable) {
        return tourLocationAdapter.fetchTourLocationsByKeyword(keyword, pageable, true);
    }

    public List<TourLocation> findByCategory(Integer category, Pageable pageable) {
        return tourLocationAdapter.fetchTourLocationsByCategory(category, pageable, true);
    }

    public List<TourLocation> sortByDistance(List<TourLocation> locations, Double centerLat, Double centerLng) {
        if (locations == null) return List.of();
        return locations.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(loc -> haversine(centerLat, centerLng, loc.getLatitude(), loc.getLongitude())))
                .toList();
    }

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


