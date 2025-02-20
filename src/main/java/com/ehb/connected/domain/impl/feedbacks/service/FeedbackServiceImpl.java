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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final ProjectRepository projectRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;
    private final UrlHelper urlHelper;
    private final NotificationServiceImpl notificationService;

    private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);
    private final FeedbackMapper feedbackMapper;

    private Feedback getFeedbackAndCheckPermissions(Principal principal, Long projectId, Long feedbackId) {
        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        logger.info("Project found: {}", project.getId());

        // find the feedback by id.
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException(Feedback.class, feedbackId));

        logger.info("Feedback found: {}", feedback.getId());

        // Ensure that the user can modify this feedback
        final User currentUser = userService.getUserByEmail(principal.getName());
        if (!feedback.getUser().getId().equals(currentUser.getId())) {
            throw new UserUnauthorizedException(currentUser.getId());
        }

        return feedback;
    }

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

        String destinationUrl = urlHelper.UrlBuilder(
                UrlHelper.Sluggify(project.getAssignment().getCourse().getName()),
                UrlHelper.Sluggify(project.getAssignment().getName()),
                "projects/" + project.getId(),
                "feedback");

        notificationService.createNotification(
                project.getCreatedBy(),
                "feedback has been written for  " + project.getTitle(),
                destinationUrl
        );

        return feedbackMapper.toDto(feedback);
    }

    @Override
    public FeedbackDto updateFeedback(Principal principal, Long projectId, Long feedbackId, FeedbackCreateDto feedbackDto) {
        final Feedback feedback = getFeedbackAndCheckPermissions(principal, projectId, feedbackId);

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
    public void deleteFeedback(Principal principal, Long projectId, Long feedbackId) {
        final Feedback feedback = getFeedbackAndCheckPermissions(principal, projectId, feedbackId);

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

