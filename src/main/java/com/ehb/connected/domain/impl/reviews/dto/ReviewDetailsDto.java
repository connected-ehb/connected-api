package com.ehb.connected.domain.impl.reviews.dto;

import com.ehb.connected.domain.impl.reviews.entities.ReviewStatusEnum;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDetailsDto {

    private Long id;
    private ReviewStatusEnum status;
    private UserDetailsDto reviewer;
    private Long projectId;
}
