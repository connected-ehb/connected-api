package com.ehb.connected.domain.impl.feedbacks.service;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.mappers.FeedbackMapper;
import com.ehb.connected.domain.impl.feedbacks.repositories.FeedbackRepository;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final ProjectRepository projectRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    private final FeedbackMapper feedbackMapper;

    private Feedback getFeedbackAndCheckPermissions(Principal principal, Long projectId, Long feedbackId) {
        // Check if project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // Find the feedback
        final Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found"));

        // Check if feedback belongs to project
        if (!feedback.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Feedback does not belong to the specified project");
        }

        // Ensure that the user can modify this feedback
        final User currentUser = userService.getUserByEmail(principal.getName());
        if (!feedback.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this feedback");
        }

        return feedback;
    }

    @Override
    public FeedbackDto giveFeedback(Principal principal, Long id, FeedbackCreateDto feedbackDto) {
        final Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        final User user = userService.getUserByEmail(principal.getName());
        System.out.println("comment: " + feedbackDto.getComment());
        final Feedback feedback = new Feedback();
        feedback.setComment(feedbackDto.getComment());
        feedback.setUser(user);
        feedback.setProject(project);

        feedbackRepository.save(feedback);

        return feedbackMapper.toDto(feedback);
    }

    @Override
    public FeedbackDto updateFeedback(Principal principal, Long id, Long feedbackId, FeedbackCreateDto feedbackDto) {
        final Feedback feedback = getFeedbackAndCheckPermissions(principal, id, feedbackId);

        // Update comment
        feedback.setComment(feedbackDto.getComment());
        return feedbackMapper.toDto(feedbackRepository.save(feedback));
    }

    @Override
    public void deleteFeedback(Principal principal, Long id, Long feedbackId) {
        final Feedback feedback = getFeedbackAndCheckPermissions(principal, id, feedbackId);

        // Delete the feedback
        feedbackRepository.delete(feedback);
    }

    @Override
    public List<FeedbackDto> getAllFeedbackForProject(Principal principal, Long id) {
        // Check if project exists
        final Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // Return all feedback related to the project
        return feedbackMapper.toDtoList(feedbackRepository.findAllByProjectOrderByCreatedAtDesc(project));
    }
}

