package com.ehb.connected.domain.impl.reviews.mappers;

import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.entities.Review;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final UserDetailsMapper userDetailsMapper;

    public ReviewDetailsDto toDto(Review review) {
        ReviewDetailsDto dto = new ReviewDetailsDto();
        dto.setId(review.getId());
        dto.setReviewer(userDetailsMapper.toUserDetailsDto(review.getReviewer()));
        dto.setProjectId(review.getProject().getId());
        dto.setStatus(review.getStatus());
        return dto;
    }

    public List<ReviewDetailsDto> toDtoList(List<Review> reviews) {
        return reviews.stream().map(this::toDto).toList();
    }
}
