package com.ehb.connected.domain.impl.reviews.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.entities.Review;
import com.ehb.connected.domain.impl.reviews.mappers.ReviewMapper;
import com.ehb.connected.domain.impl.reviews.repositories.ReviewRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Override
    public List<ReviewDetailsDto> getAllReviewsByProjectId(Principal principal, Long projectId) {
        return reviewMapper.toDtoList(reviewRepository.findAllByProjectId(projectId));
    }

    @Override
    public ReviewDetailsDto createOrUpdateReviewForProject(Principal principal, Long projectId, ReviewCreateDto reviewCreateDto) {
        User reviewer = userService.getUserByPrincipal(principal);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        Review review = reviewRepository.findByProjectIdAndReviewerId(projectId, reviewer.getId())
                .orElse(new Review());

        review.setProject(project);
        review.setReviewer(reviewer);
        review.setStatus(reviewCreateDto.getStatus());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Principal principal, Long reviewId) {
        User user = userService.getUserByPrincipal(principal);
        Review review = reviewRepository.findByIdAndReviewer(reviewId, user)
                .orElseThrow(() -> new EntityNotFoundException(Review.class, reviewId));
        if (!review.isOwner(user)) {
            throw new BaseRuntimeException("You are not allowed to delete this review", HttpStatus.FORBIDDEN);
        }
        reviewRepository.delete(review);
    }
}
