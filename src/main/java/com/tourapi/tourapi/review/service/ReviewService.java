package com.tourapi.tourapi.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.tourapi.tourapi.review.domain.Review;
import com.tourapi.tourapi.review.repository.ReviewRepository;
import com.tourapi.tourapi.review.dto.ReviewCreateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Page<Review> getReviews(Long contentId, Pageable pageable) {
        Pageable sanitized = PageRequest.of(
                pageable != null ? pageable.getPageNumber() : 0,
                pageable != null ? pageable.getPageSize() : 10,
                Sort.by(Sort.Order.desc("createdAt"))
        );
        return reviewRepository.findByContentIdOrderByCreatedAtDesc(contentId, sanitized);
    }

    public long getReviewCount(Long contentId) {
        return reviewRepository.countByContentId(contentId);
    }

    public Review create(ReviewCreateRequest req) {
        Review r = new Review();
        r.setContentId(req.getContentId());
        r.setUserId(req.getUserId());
        r.setRating(req.getRating() != null ? req.getRating() : 0);
        r.setContent(req.getContent());
        r.setImagesJson(req.getImagesJson());
        return reviewRepository.save(r);
    }
}


