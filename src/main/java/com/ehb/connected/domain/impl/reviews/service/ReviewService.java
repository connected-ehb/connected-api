package com.ehb.connected.domain.impl.reviews.service;

import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ReviewService {
    List<ReviewDetailsDto> getAllReviewsByProjectId(Authentication authentication, Long projectId);

    ReviewDetailsDto createOrUpdateReviewForProject(Authentication authentication, Long projectId, ReviewCreateDto reviewCreateDto);

    void deleteReview(Authentication authentication, Long reviewId);
}
