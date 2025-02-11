package com.ehb.connected.domain.impl.feedbacks.service;

import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.feedbacks.entities.FeedbackDto;
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
    public Feedback giveFeedback(Principal principal, Long id, FeedbackDto feedbackDto) {
        final Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        final User user = userService.getUserByEmail(principal.getName());

        final Feedback feedback = new Feedback(null, feedbackDto.getComment(), user, project, null, null);

        feedbackRepository.save(feedback);

        return feedback;
    }

    @Override
    public Feedback updateFeedback(Principal principal, Long id, Long feedbackId, FeedbackDto feedbackDto) {
        final Feedback feedback = getFeedbackAndCheckPermissions(principal, id, feedbackId);

        // Update comment
        feedback.setComment(feedbackDto.getComment());
        return feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(Principal principal, Long id, Long feedbackId) {
        final Feedback feedback = getFeedbackAndCheckPermissions(principal, id, feedbackId);

        // Delete the feedback
        feedbackRepository.delete(feedback);
    }

    @Override
    public List<Feedback> getAllFeedbackForProject(Principal principal, Long id) {
        // Check if project exists
        final Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // Return all feedback related to the project
        return feedbackRepository.findAllByProject(project);
    }
}

