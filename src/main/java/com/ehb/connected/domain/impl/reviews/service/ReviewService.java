package com.ehb.connected.domain.impl.reviews.service;

import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewUpdateDto;
import com.ehb.connected.domain.impl.users.entities.User;

import java.util.List;

public interface ReviewService {
    List<ReviewDetailsDto> getAllReviewsByProjectId(User principal, Long projectId);

    ReviewDetailsDto createReviewForProject(User principal, Long projectId, ReviewCreateDto reviewCreateDto);

    void deleteReview(User principal, Long reviewId);

    ReviewDetailsDto updateReview(User principal, Long reviewId, ReviewUpdateDto reviewUpdateDto);
}
