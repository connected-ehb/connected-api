package com.ehb.connected.domain.impl.reviews.controllers;

import com.ehb.connected.domain.impl.reviews.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasAnyAuthority('review:delete')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(Authentication authentication, Long reviewId) {
        reviewService.deleteReview(authentication, reviewId);
        return ResponseEntity.noContent().build();
    }
}
