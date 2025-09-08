package com.tourapi.tourapi.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tourapi.tourapi.review.domain.Review;
import com.tourapi.tourapi.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Page<Review> getReviews(Long contentId, Pageable pageable) {
        return reviewRepository.findByContentIdOrderByCreatedAtDesc(contentId, pageable);
    }

    public long getReviewCount(Long contentId) {
        return reviewRepository.countByContentId(contentId);
    }
}


