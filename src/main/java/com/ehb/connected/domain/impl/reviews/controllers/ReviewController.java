package com.ehb.connected.domain.impl.reviews.controllers;

import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewUpdateDto;
import com.ehb.connected.domain.impl.reviews.service.ReviewService;
import com.ehb.connected.domain.impl.users.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasAnyAuthority('review:update')")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailsDto> updateReview(User principal, Long reviewId, ReviewUpdateDto reviewUpdateDto) {
        return ResponseEntity.ok(reviewService.updateReview(principal, reviewId, reviewUpdateDto));
    }

    @PreAuthorize("hasAnyAuthority('review:delete')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(User principal, Long reviewId) {
        reviewService.deleteReview(principal, reviewId);
        return ResponseEntity.noContent().build();
    }
}
