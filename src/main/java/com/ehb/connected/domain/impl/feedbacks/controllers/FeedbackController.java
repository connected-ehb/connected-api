package com.ehb.connected.domain.impl.feedbacks.controllers;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.service.FeedbackService;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    @PutMapping("{feedbackId}")
    public ResponseEntity<FeedbackDto> updateFeedback(Principal principal, @PathVariable Long feedbackId, @RequestBody FeedbackCreateDto feedbackDto) {
        return ResponseEntity.ok(feedbackService.updateFeedback(principal, feedbackId, feedbackDto));
    }

    @DeleteMapping("{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(Principal principal, @PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(principal, feedbackId);
        return ResponseEntity.ok().build();
    }

}
