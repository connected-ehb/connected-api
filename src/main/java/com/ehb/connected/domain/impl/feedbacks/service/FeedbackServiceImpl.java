package com.ehb.connected.domain.impl.feedbacks.service;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.mappers.FeedbackMapper;
import com.ehb.connected.domain.impl.feedbacks.repositories.FeedbackRepository;
import com.ehb.connected.domain.impl.notifications.helpers.UrlHelper;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.ehb.connected.exceptions.UserUnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final ProjectRepository projectRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;
    private final NotificationServiceImpl notificationService;
    private final FeedbackMapper feedbackMapper;

    @Override
    public FeedbackDto giveFeedback(Principal principal, Long projectId, FeedbackCreateDto feedbackDto) {
        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        final User user = userService.getUserByEmail(principal.getName());
        if (user.getRole() == Role.STUDENT) {
            throw new UserUnauthorizedException(user.getId());
        }
        final Feedback feedback = new Feedback();
        feedback.setComment(feedbackDto.getComment());
        feedback.setUser(user);
        feedback.setProject(project);

        feedbackRepository.save(feedback);

        // Check if receiver exists and send notification
        if (project.getProductOwner() != null) {
            String destinationUrl = UrlHelper.BuildCourseAssignmentUrl(
                    UrlHelper.Sluggify(project.getAssignment().getCourse().getName()),
                    UrlHelper.Sluggify(project.getAssignment().getName()),
                    "projects/" + project.getId(),
                    "feedback");

            notificationService.createNotification(
                    project.getProductOwner(),
                    "feedback has been written for  " + project.getTitle(),
                    destinationUrl
            );
        }



        return feedbackMapper.toDto(feedback);
    }

    @Override
    public FeedbackDto updateFeedback(Principal principal, Long feedbackId, FeedbackCreateDto feedbackDto) {
        //Retrieve the feedback by id
        final Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException(Feedback.class, feedbackId));

        // Ensure that the current user is the owner of the feedback
        final User currentUser = userService.getUserByEmail(principal.getName());
        if (!feedback.getUser().getId().equals(currentUser.getId())) {
            throw new UserUnauthorizedException(currentUser.getId());
        }

        // Update comment
        feedback.setComment(feedbackDto.getComment());
        return feedbackMapper.toDto(feedbackRepository.save(feedback));
    }

    @Override
    public void deleteFeedback(Principal principal,Long feedbackId) {
        // Retrieve the feedback by id
        final Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException(Feedback.class, feedbackId));

        final User currentUser = userService.getUserByEmail(principal.getName());
        if(!feedback.getUser().getId().equals(currentUser.getId())) {
            throw new UserUnauthorizedException(currentUser.getId());
        }
        // Delete the feedback
        feedbackRepository.delete(feedback);
    }

    @Override
    public List<FeedbackDto> getAllFeedbackForProject(Principal principal, Long projectId) {
        // Check if project exists
        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        // Return all feedback related to the project
        return feedbackMapper.toDtoList(feedbackRepository.findAllByProjectOrderByCreatedAtDesc(project));
    }
}

