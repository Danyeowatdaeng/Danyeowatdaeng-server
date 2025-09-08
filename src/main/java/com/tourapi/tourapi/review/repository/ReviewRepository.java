package com.tourapi.tourapi.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tourapi.tourapi.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByContentIdOrderByCreatedAtDesc(Long contentId, Pageable pageable);
    long countByContentId(Long contentId);
}


