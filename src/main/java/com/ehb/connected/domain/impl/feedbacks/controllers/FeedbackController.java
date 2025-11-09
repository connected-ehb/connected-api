package com.ehb.connected.domain.impl.feedbacks.controllers;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    @PreAuthorize("hasAuthority('feedback:update')")
    @PutMapping("{feedbackId}")
    public ResponseEntity<FeedbackDto> updateFeedback(Authentication authentication, @PathVariable Long feedbackId, @RequestBody FeedbackCreateDto feedbackDto) {
        return ResponseEntity.ok(feedbackService.updateFeedback(authentication, feedbackId, feedbackDto));
    }

    @PreAuthorize("hasAuthority('feedback:delete')")
    @DeleteMapping("{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(Authentication authentication, @PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(authentication, feedbackId);
        return ResponseEntity.ok().build();
    }

}
