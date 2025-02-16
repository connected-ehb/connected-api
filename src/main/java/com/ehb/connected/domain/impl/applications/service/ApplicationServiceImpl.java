package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final UserServiceImpl userService;

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final DeadlineService deadlineService;
    private final ApplicationMapper applicationMapper;

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Application not found"));
    }

    @Override
    public ApplicationDto createApplication(Principal principal, Long projectId, ApplicationCreateDto application) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("Project not found"));
        User currentUser = userService.getUserByEmail(principal.getName());
        if(project.getCreatedBy().equals(currentUser)) {
            throw new RuntimeException("User cannot apply to own project");
        }
        // Check if user has already applied to the project
        List<Application> applications = project.getApplications();
        for (Application app : applications) {
            if (app.getApplicant().equals(currentUser)) {
                throw new RuntimeException("User has already applied to this project");
            }
        }
        Deadline deadline = deadlineService.getDeadlineByAssignmentIdAndRestrictions(project.getAssignment().getId(), DeadlineRestriction.APPLICATION_SUBMISSION);
        if (deadline != null && deadline.getDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Project creation is no longer allowed. The deadline has passed.");
        }
        Application newApplication = new Application();
        newApplication.setStatus(ApplicationStatusEnum.PENDING);
        newApplication.setMotivationMd(application.getMotivationMd());
        newApplication.setApplicant(currentUser);
        newApplication.setProject(project);
        applicationRepository.save(newApplication);
        return applicationMapper.toDto(newApplication);
    }

    @Override
    public Application updateApplication(Application application) {
        return applicationRepository.save(application);
    }

    @Override
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);

    }

    @Override
    public List<Application> findAllApplications(Long id) {
        return applicationRepository.findAllApplications(id);
    }

    @Override
    public List<Application> findAllApplicationsByUserAndAssignment(Long id, Long assignmentId) {
        return applicationRepository.findAllApplicationsByUserId(id, assignmentId);
    }


}
