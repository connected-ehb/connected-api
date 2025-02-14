package com.ehb.connected.domain.impl.feedbacks.service;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface FeedbackService {

    FeedbackDto giveFeedback(Principal principal, Long id, FeedbackCreateDto feedbackDto);
    FeedbackDto updateFeedback(Principal principal, Long id, Long feedbackId, FeedbackCreateDto feedbackDto);
    void deleteFeedback(Principal principal, Long id, Long feedbackId);
    List<FeedbackDto> getAllFeedbackForProject(Principal principal, Long id);
}
