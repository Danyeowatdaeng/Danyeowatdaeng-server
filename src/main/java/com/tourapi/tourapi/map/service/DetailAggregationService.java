package com.tourapi.tourapi.map.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.tourapi.tourapi.map.adapter.TourLocationAdapter;
import com.tourapi.tourapi.map.dto.DetailAggregate;
import com.tourapi.tourapi.map.dto.DetailIntroResponse;
import com.tourapi.tourapi.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DetailAggregationService {

    private final TourLocationAdapter tourLocationAdapter;
    private final ReviewService reviewService;

    public DetailAggregate getDetail(Long contentId, Integer contentTypeId) {
        DetailIntroResponse intro = tourLocationAdapter.fetchDetailIntro(contentId, contentTypeId);

        Map<String, Object> summary = new HashMap<>();
        long reviewCount = reviewService.getReviewCount(contentId);
        summary.put("reviewCount", reviewCount);

        // 리뷰 샘플 3개
        var reviewPage = reviewService.getReviews(contentId, PageRequest.of(0, 3));
        List<Map<String, Object>> reviewItems = reviewPage.map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("rating", r.getRating());
            m.put("content", r.getContent());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).getContent();

        // 이벤트는 스켈레톤: 우선 빈 리스트 형태로 노출(서비스 의존성 사용 표시)
        List<Map<String, Object>> events = java.util.Collections.emptyList();

        return DetailAggregate.builder()
                .contentId(contentId)
                .contentTypeId(contentTypeId)
                .summary(summary)
                .intro(intro != null ? intro.getDetails() : Map.of())
                .events(events)
                .reviews(reviewItems)
                .build();
    }
}


