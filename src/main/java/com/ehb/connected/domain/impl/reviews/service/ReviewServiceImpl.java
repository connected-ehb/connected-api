package com.ehb.connected.domain.impl.reviews.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewUpdateDto;
import com.ehb.connected.domain.impl.reviews.entities.Review;
import com.ehb.connected.domain.impl.reviews.mappers.ReviewMapper;
import com.ehb.connected.domain.impl.reviews.repositories.ReviewRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ProjectRepository projectRepository;

    @Override
    public List<ReviewDetailsDto> getAllReviewsByProjectId(User principal, Long projectId) {
        return reviewMapper.toDtoList(reviewRepository.findAllByProjectId(projectId));
    }

    @Override
    public ReviewDetailsDto createReviewForProject(User principal, Long projectId, ReviewCreateDto reviewCreateDto) {
        Review review = new Review();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Review.class, projectId));
        review.setProject(project);
        review.setReviewer(principal);
        review.setStatus(reviewCreateDto.getStatus());
        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(User principal, Long reviewId) {
        Review review = reviewRepository.findByIdAndReviewer(reviewId, principal)
                .orElseThrow(() -> new EntityNotFoundException(Review.class, reviewId));
        if (!review.getReviewer().equals(principal)) {
            throw new BaseRuntimeException("You are not allowed to delete this review", HttpStatus.FORBIDDEN);
        }
        reviewRepository.delete(review);
    }

    @Override
    public ReviewDetailsDto updateReview(User principal, Long reviewId, ReviewUpdateDto reviewUpdateDto) {
        Review review = reviewRepository.findByIdAndReviewer(reviewId, principal)
                .orElseThrow(() -> new EntityNotFoundException(Review.class, reviewId));
        if (!review.getReviewer().equals(principal)) {
            throw new BaseRuntimeException("You are not allowed to delete this review", HttpStatus.FORBIDDEN);
        }
        review.setStatus(reviewUpdateDto.getStatus());
        return reviewMapper.toDto(reviewRepository.save(review));
    }
}
