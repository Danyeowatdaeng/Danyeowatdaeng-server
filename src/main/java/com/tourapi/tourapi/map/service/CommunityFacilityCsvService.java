package com.tourapi.tourapi.map.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommunityFacilityCsvService {

    @Getter
    private List<Row> rows = Collections.emptyList();

    public record Row(
            String name,
            String category3,
            String roadAddress,
            String jibunAddress,
            String homepage,
            String closedDays,
            String openingHours,
            Double latitude,
            Double longitude,
            String phone,
            String nameNormalized
    ) {}

    @PostConstruct
    void load() {
        long start = System.currentTimeMillis();
        List<Row> loaded = new ArrayList<>(75000);
        try {
            ClassPathResource resource = new ClassPathResource("pet_community_location.csv");
            try (InputStream is = resource.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // naive CSV split. Assumes fields themselves don't contain unescaped commas.
                    // If they do, replace with a proper CSV parser later.
                    String[] t = line.split(",", -1);
                    try {
                        // Column mapping based on sample line provided by user
                        // 0: name, 2: category3, 11: lat, 12: lng, 14: road full, 15: jibun full, 16: phone, 17: homepage, 18: closed days, 19: opening hours
                        String name = get(t, 0);
                        String category3 = get(t, 2);
                        Double lat = parseDouble(get(t, 11));
                        Double lng = parseDouble(get(t, 12));
                        String road = get(t, 14);
                        String jibun = get(t, 15);
                        String phone = get(t, 16);
                        String homepage = get(t, 17);
                        String closed = get(t, 18);
                        String hours = get(t, 19);
                        if (name == null || name.isBlank()) continue;
                        String norm = normalize(name);
                        loaded.add(new Row(name, category3, road, jibun, homepage, closed, hours, lat, lng, phone, norm));
                    } catch (Exception e) {
                        // skip bad row
                    }
                }
            }
            this.rows = Collections.unmodifiableList(loaded);
            long ms = System.currentTimeMillis() - start;
            log.info("반려동물 커뮤니티 CSV 로드 완료: {}건, {} ms 소요", rows.size(), ms);
        } catch (Exception e) {
            log.error("pet_community_location.csv 로드 실패", e);
            this.rows = Collections.emptyList();
        }
    }

    @Cacheable("communityFacilitiesByKeyword")
    public List<Row> searchByKeyword(String keyword, Pageable pageable) {
        long start = System.currentTimeMillis();
        if (keyword == null || keyword.isBlank()) {
            log.info("키워드 검색 요청: 빈 키워드 → 결과 0건");
            return List.of();
        }
        String norm = normalize(keyword);
        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();
        
        List<Row> results = rows.stream()
                .filter(Objects::nonNull)
                .filter(r -> r.nameNormalized().contains(norm))
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());
        
        long ms = System.currentTimeMillis() - start;
        log.info("키워드 검색 완료: '{}' → page={}, size={} → 결과 {}건, {} ms 소요", 
                keyword, pageable.getPageNumber(), size, results.size(), ms);
        return results;
    }

    private static String get(String[] arr, int idx) {
        if (idx < 0 || idx >= arr.length) return null;
        String v = arr[idx];
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


