package com.ehb.connected.domain.impl.reviews.dto;

import com.ehb.connected.domain.impl.reviews.entities.ReviewStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateDto {

    private ReviewStatusEnum status;
}
