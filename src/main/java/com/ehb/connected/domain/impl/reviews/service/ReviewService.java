package com.ehb.connected.domain.impl.reviews.service;

import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;

import java.security.Principal;
import java.util.List;

public interface ReviewService {
    List<ReviewDetailsDto> getAllReviewsByProjectId(Principal principal, Long projectId);

    ReviewDetailsDto createOrUpdateReviewForProject(Principal principal, Long projectId, ReviewCreateDto reviewCreateDto);

    void deleteReview(Principal principal, Long reviewId);
}
