package com.ehb.connected.domain.impl.feedbacks.service;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FeedbackService {

    FeedbackDto giveFeedback(Authentication authentication, Long id, FeedbackCreateDto feedbackDto);
    FeedbackDto updateFeedback(Authentication authentication, Long feedbackId, FeedbackCreateDto feedbackDto);
    void deleteFeedback(Authentication authentication, Long feedbackId);
    List<FeedbackDto> getAllFeedbackForProject(Authentication authentication, Long id);
}
