package com.ehb.connected.domain.impl.reviews.repositories;

import com.ehb.connected.domain.impl.reviews.entities.Review;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProjectId(Long projectId);
    Optional<Review> findByProjectIdAndReviewerId(Long projectId, Long reviewerId);
    Optional<Review> findByIdAndReviewer(Long reviewId, User user);
}
