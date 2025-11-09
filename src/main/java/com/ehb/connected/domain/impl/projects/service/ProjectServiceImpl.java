package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.notifications.helpers.UrlHelper;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.events.entities.ProjectEventType;
import com.ehb.connected.domain.impl.projects.events.service.ProjectEventService;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.DeadlineExpiredException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.ehb.connected.exceptions.UserNotOwnerOfProjectException;
import com.ehb.connected.exceptions.UserUnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserService projectUserService;
    private final ProjectMapper projectMapper;

    private final DeadlineService deadlineService;
    private final AssignmentRepository assignmentRepository;
    private final ApplicationMapper applicationMapper;
    private final UserService userService;
    private final NotificationServiceImpl notificationService;
    private final ProjectEventService projectEventService;

    private final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Override
    public ProjectDetailsDto getProjectById(Authentication authentication, Long projectId) {
        User user = userService.getUserByAuthentication(authentication);
        Project project = getProjectById(projectId);

        if (user.hasRole(Role.RESEARCHER) && user.isCreator(project)) {
            return projectMapper.toResearcherDetailsDto(project);
        }
        if (user.canViewProject(project)) {
            return projectMapper.toDetailsDto(project);

        } else {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    @Override
    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));
    }

    @Override
    public List<ProjectDetailsDto> getAllProjectsByAssignmentId(Long assignmentId) {
        return projectMapper.toDetailsDtoList(projectRepository.findAllByAssignmentId(assignmentId));
    }

    @Override
    public ProjectDetailsDto getProjectByUserAndAssignmentId(Authentication authentication, Long assignmentId) {
        User user = userService.getUserByAuthentication(authentication);
        //project can be null if the user is not a member of any project in the assignment
        Project project = projectRepository.findByMembersAndAssignmentIdAndStatus(List.of(user), assignmentId, ProjectStatusEnum.PUBLISHED);
        if (project == null) {
            return null;
        }
        return projectMapper.toDetailsDto(project);
    }

    @Override
    public List<ProjectDetailsDto> getAllPublishedOrOwnedProjectsByAssignmentId(Authentication authentication, Long assignmentId) {
        return projectMapper.toDetailsDtoList(
                projectRepository.findAllByAssignmentIdAndStatusOrOwnedBy(
                        assignmentId,
                        ProjectStatusEnum.PUBLISHED,
                        userService.getUserByAuthentication(authentication)
                ));
    }

    @Override
    public ProjectDetailsDto createProject(Authentication authentication, Long assignmentId, ProjectCreateDto projectDto) {

        final User user = userService.getUserByAuthentication(authentication);
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(Assignment.class, assignmentId));

        if (projectUserService.isUserMemberOfAnyProjectInAssignment(user, assignment)) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        final Deadline deadline = deadlineService.getDeadlineByAssignmentAndRestrictions(assignment, DeadlineRestriction.PROJECT_CREATION);

        if (user.hasRole(Role.STUDENT) && deadline != null && deadline.hasExpired()) {
            throw new DeadlineExpiredException(DeadlineRestriction.PROJECT_CREATION);
        }

        Project newProject = projectMapper.toEntity(projectDto);

        // Set the default team size from the assignment if not provided
        if (projectDto.getTeamSize() == null) {
            newProject.setTeamSize(assignment.getDefaultTeamSize());
        }

        // If user is a student, make him product owner, else if he is teacher leave it null as teacher cannot be product owner
        if (user.hasRole(Role.STUDENT)) {
            newProject.setStatus(ProjectStatusEnum.PENDING);
            newProject.setMembers(List.of(user));
            newProject.setProductOwner(user);
        } else if (user.hasRole(Role.TEACHER)) {
            newProject.setMembers(List.of());
            newProject.setStatus(ProjectStatusEnum.APPROVED);
        } else if (user.hasRole(Role.RESEARCHER)) {
            newProject.setMembers(List.of());
            newProject.setStatus(ProjectStatusEnum.APPROVED);
            newProject.setGid(UUID.randomUUID());
        }

        newProject.setCreatedBy(user);
        newProject.setAssignment(assignment);
        Project savedProject = projectRepository.save(newProject);
        projectEventService.logEvent(savedProject.getId(), null, ProjectEventType.PROJECT_CREATED, "Project created");
        logger.info("[{}] Project has been created", ProjectService.class.getName());

        return projectMapper.toDetailsDto(savedProject);
    }

    @Override
    public ProjectDetailsDto save(Authentication authentication, Long projectId, ProjectUpdateDto project) {
        final Project existingProject = getProjectById(projectId);
        final User user = userService.getUserByAuthentication(authentication);

        // Check if user is the owner of the project
        if (!user.hasRole(Role.TEACHER) && !user.isProductOwner(existingProject)) {
            throw new UserNotOwnerOfProjectException();
        }

        // When the project has status needs revision -> revised
        if (existingProject.getStatus().equals(ProjectStatusEnum.NEEDS_REVISION)) {
            existingProject.setStatus(ProjectStatusEnum.REVISED);
            projectEventService.logEvent(projectId, null, ProjectEventType.STATUS_CHANGED, "Project status changed to REVISED");
        }

        projectMapper.updateEntityFromDto(user, project, existingProject);
        Project savedProject;
        try {
            savedProject = projectRepository.save(existingProject);
        } catch (Exception e) {
            throw new BaseRuntimeException("Project could not be updated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        projectEventService.logEvent(projectId, null, ProjectEventType.PROJECT_UPDATED, "Project updated");
        logger.info("[{}] Project with id: {} has been updated", ProjectService.class.getSimpleName(), projectId);

        return projectMapper.toDetailsDto(savedProject);
    }

    @Override
    public List<ProjectDetailsDto> findAllInAssignmentCreatedBy(Long assignmentId, Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        List<Project> projects = projectRepository.findAllByAssignmentIdAndCreatedBy(assignmentId, user);

        List<Project> filtered = projects.stream()
                .filter(p -> p.getCreatedBy().getId().equals((user.getId())) ||
                        p.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId())))
                .toList();
        return projectMapper.toDetailsDtoList(filtered);
    }

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }

    @Override
    public ProjectDetailsDto changeProjectStatus(Authentication authentication, Long projectId, ProjectStatusEnum status) {
        final User user = userService.getUserByAuthentication(authentication);

        final Project project = getProjectById(projectId);

        final Assignment assignment = project.getAssignment();

        if (project.hasStatus(ProjectStatusEnum.REJECTED)) {
            throw new BaseRuntimeException("Cannot change status of a rejected project", HttpStatus.CONFLICT);
        }

        if (assignment == null) {
            throw new BaseRuntimeException("Cannot change status of global project", HttpStatus.CONFLICT);
        }

        ProjectStatusEnum previousStatus = project.getStatus();

        project.setStatus(status);
        projectRepository.save(project);
        projectEventService.logEvent(projectId, user.getId(), ProjectEventType.STATUS_CHANGED,
                "Status changed from " + previousStatus + " to " + status);
        logger.info("[{}] Project ID: {} status changed from {} to {} by User ID: {}",
                ProjectService.class.getSimpleName(), projectId, previousStatus, status, user.getId());

        if (project.getCreatedBy().getRole().equals(Role.RESEARCHER)) {
            String destinationUrl = UrlHelper.urlBuilder("/projects", project.getId().toString());

            notificationService.createNotification(
                    project.getCreatedBy(),
                    "The project in assignment: " + project.getAssignment().getName() + "status has been set to: " + project.getStatus().toString().toLowerCase(),
                    destinationUrl
            );
        }

        if (project.getProductOwner() != null) {
            String destinationUrl = UrlHelper.buildCourseAssignmentUrl(
                    UrlHelper.sluggify(project.getAssignment().getCourse().getName()),
                    UrlHelper.sluggify(project.getAssignment().getName()),
                    "projects/" + project.getId());

            notificationService.createNotification(
                    project.getProductOwner(),
                    "Your project status has been set to: " + project.getStatus().toString().toLowerCase(),
                    destinationUrl
            );
        }
        return projectMapper.toDetailsDto(project);
    }

    @Override
    public List<ProjectDetailsDto> publishAllProjects(Authentication authentication, Long assignmentId) {
        final List<Project> projects = getAllProjectsByStatus(assignmentId, ProjectStatusEnum.APPROVED);
        projects.forEach(project -> changeProjectStatus(authentication, project.getId(), ProjectStatusEnum.PUBLISHED));
        logger.info("All approved projects have been published.");
        return projectMapper.toDetailsDtoList(projects);
    }

    @Override
    public List<ApplicationDetailsDto> getAllApplicationsByProjectId(Authentication authentication, Long projectId) {
        final User user = userService.getUserByAuthentication(authentication);
        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        if (!user.hasRole(Role.TEACHER) && !user.isProductOwner(project)) {
            throw new UserNotOwnerOfProjectException();
        }
        return applicationMapper.toDtoList(project.getApplications());
    }

    @Override
    public void removeMember(Authentication authentication, Long projectId, Long memberId) {
        final Project project = getProjectById(projectId);
        final User actor = userService.getUserByAuthentication(authentication);

        // only teachers may remove members
        if (!actor.hasRole(Role.TEACHER)) {
            throw new UserUnauthorizedException(actor.getId());
        }

        final User kicked = userService.getUserById(memberId);

        // fail fast if the user isn’t a member of this project
        final boolean removed = project.getMembers().removeIf(m -> m.getId().equals(memberId));
        if (!removed) {
            throw new EntityNotFoundException(User.class, memberId);
        }

        projectEventService.logEvent(projectId, actor.getId(), ProjectEventType.MEMBER_REMOVED, "Kicked " + kicked.getFullName());

        // If the removed member was the Product Owner, reassign (or clear)
        if (kicked.isProductOwner(project)) {
            project.setProductOwner(project.hasNoMembers() ? null : project.getMembers().get(0));
            projectEventService.logEvent(projectId, null, ProjectEventType.PRODUCT_OWNER_REASSIGNED, "Product owner reassigned");
        }

        // Mark any application of the removed user to this project as REJECTED
        project.getApplications().stream()
                .filter(a -> a.getApplicant().getId().equals(memberId))
                .findFirst()
                .ifPresent(a -> a.setStatus(ApplicationStatusEnum.REJECTED));

        projectRepository.save(project);

        // Notify removed user
        final String destinationUrl = UrlHelper.buildCourseAssignmentUrl(
                UrlHelper.sluggify(project.getAssignment().getCourse().getName()),
                UrlHelper.sluggify(project.getAssignment().getName()),
                "projects/" + project.getId()
        );
        notificationService.createNotification(
                kicked,
                "You have been removed from project: " + project.getTitle(),
                destinationUrl
        );

        logger.info("[{}] Member ID: {} removed from project ID: {} by User ID: {}",
                ProjectService.class.getSimpleName(), memberId, projectId, actor.getId());
    }

    @Override
    public ProjectDetailsDto claimProject(Authentication authentication, Long projectId) {
        final User user = userService.getUserByAuthentication(authentication);
        final Project project = getProjectById(projectId);

        if (project.getProductOwner() != null) {
            throw new BaseRuntimeException("Cannot claim a project with a product owner", HttpStatus.CONFLICT);
        }

        if (projectUserService.isUserMemberOfAnyProjectInAssignment(user, project.getAssignment())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        // reject all other applications of the user
        user.getApplications().stream()
                .filter(application -> application.hasSameAssignment(project))
                .forEach(application -> application.setStatus(ApplicationStatusEnum.REJECTED));

        project.getMembers().add(user);
        project.setProductOwner(user);
        projectRepository.save(project);
        projectEventService.logEvent(projectId, user.getId(), ProjectEventType.PROJECT_CLAIMED, "Claimed the project");
        logger.info("[{}] Project ID: {} has been claimed by User ID: {}", ProjectService.class.getSimpleName(), projectId, user.getId());
        return projectMapper.toDetailsDto(project);
    }

    @Override
    public ProjectDetailsDto importProject(Authentication authentication, Long assignmentId, Long projectId) {
        final User user = userService.getUserByAuthentication(authentication);
        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));
        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(Assignment.class, assignmentId));

        final UUID gid = project.getGid();
        if (gid == null) {
            throw new BaseRuntimeException("Cannot import project as it is not global", HttpStatus.CONFLICT);
        }
        // Check if there is already an imported project in this assignment with that gid
        if (projectRepository.existsByAssignmentIdAndGid(assignmentId, gid)) {
            throw new BaseRuntimeException("Project with GID: " + gid + " already exists in this assignment", HttpStatus.CONFLICT);
        }

        if (projectUserService.isUserMemberOfAnyProjectInAssignment(user, assignment)) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        Project importedProject = new Project();
        importedProject.setGid(gid);
        importedProject.setTitle(project.getTitle());
        importedProject.setStatus(ProjectStatusEnum.PENDING);
        importedProject.setAssignment(assignment);
        importedProject.setMembers(List.of(user));
        importedProject.setCreatedBy(project.getCreatedBy());
        importedProject.setProductOwner(user);
        importedProject.setDescription(project.getDescription());
        importedProject.setShortDescription(project.getShortDescription());
        importedProject.setTeamSize(project.getTeamSize());
        importedProject.setBackgroundImage(project.getBackgroundImage());
        importedProject.setTags(new ArrayList<>(project.getTags()));
        projectRepository.save(importedProject);

        projectEventService.logEvent(importedProject.getId(), user.getId(), ProjectEventType.PROJECT_IMPORTED,"Project imported");
        logger.info("[{}] Project with GID: {} has been imported to assignment ID: {} by {} {}",
                ProjectService.class.getSimpleName(), gid, assignmentId, user.getFirstName(), user.getLastName());

        String destinationUrl = UrlHelper.urlBuilder("/projects", importedProject.getId().toString());

        notificationService.createNotification(
                project.getCreatedBy(),
                "Your project has been imported to assignment: " + assignment.getName(),
                destinationUrl
        );

        return projectMapper.toDetailsDto(importedProject);
    }

    @Override
    public ProjectDetailsDto createGlobalProject(Authentication authentication, ProjectCreateDto project) {
        final User user = userService.getUserByAuthentication(authentication);

        Project newProject = projectMapper.toEntity(project);
        newProject.setStatus(ProjectStatusEnum.APPROVED);
        newProject.setMembers(List.of());
        newProject.setCreatedBy(user);
        newProject.setGid(UUID.randomUUID());
        newProject.setAssignment(null);
        Project savedProject = projectRepository.save(newProject);
        logger.info("[{}] Global project has been created", ProjectService.class.getName());
        return projectMapper.toDetailsDto(savedProject);
    }

    @Override
    public List<ProjectDetailsDto> getAllGlobalProjects(Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        if (user.hasRole(Role.RESEARCHER)) {
            return projectMapper.toDetailsDtoList(projectRepository.findAllByCreatedBy(user));
        } else {
            // Return all projects where createdBy user has role RESEARCHER and has no assignment
            return projectMapper.toDetailsDtoList(projectRepository.findAllByCreatedByRoleAndAssignmentIsNull(Role.RESEARCHER));
        }

    }

    @Override
    public void leaveProject(Authentication authentication, Long projectId) {
        final User user = userService.getUserByAuthentication(authentication);
        final Project project = getProjectById(projectId);

        // must be member
        if (!project.getMembers().removeIf(m -> m.getId().equals(user.getId()))) {
            throw new UserUnauthorizedException(user.getId());
        }

        // if they were product owner -> reassign or clear
        if (user.isProductOwner(project)) {
            project.setProductOwner(project.hasNoMembers() ? null : project.getMembers().get(0));
        }

        projectRepository.save(project);
        projectEventService.logEvent(projectId, user.getId(), ProjectEventType.USER_LEFT, "Left");
        logger.info("[ProjectService] User ID {} left project ID {}", user.getId(), projectId);
    }

    @Override
    public List<Project> getAllProjectsByStatus(Long assignmentId, ProjectStatusEnum status) {
        return projectRepository.findAllByAssignmentIdAndStatus(assignmentId, status);
    }

}
