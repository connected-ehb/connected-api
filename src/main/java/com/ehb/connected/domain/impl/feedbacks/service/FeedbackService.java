package com.ehb.connected.domain.impl.feedbacks.service;

import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.feedbacks.entities.FeedbackDto;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface FeedbackService {

    Feedback giveFeedback(Principal principal, Long id, FeedbackDto feedbackDto);
    Feedback updateFeedback(Principal principal, Long id, Long feedbackId, FeedbackDto feedbackDto);
    void deleteFeedback(Principal principal, Long id, Long feedbackId);
    List<Feedback> getAllFeedbackForProject(Principal principal, Long id);
}
