package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ResearcherProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectUserService projectUserService;
    @Mock private ProjectMapper projectMapper;
    @Mock private DeadlineService deadlineService;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private UserService userService;
    @Mock private NotificationServiceImpl notificationService;
    @Mock private com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository assignmentRepository;

    @InjectMocks private ProjectServiceImpl projectService;

    private Principal principal;

    @BeforeEach
    void setUp() {
        principal = () -> "principal";
    }

    @Test
    void getProjectByIdReturnsResearcherDetailsWhenUserIsCreatorResearcher() {
        User researcher = userWithRole(1L, Role.RESEARCHER);
        Project project = new Project();
        project.setId(10L);
        project.setCreatedBy(researcher);
        ResearcherProjectDetailsDto researcherDto = new ResearcherProjectDetailsDto();

        when(userService.getUserByPrincipal(principal)).thenReturn(researcher);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectMapper.toResearcherDetailsDto(project)).thenReturn(researcherDto);

        ProjectDetailsDto result = projectService.getProjectById(principal, 10L);

        assertThat(result).isSameAs(researcherDto);
        verify(projectMapper).toResearcherDetailsDto(project);
        verify(projectMapper, never()).toDetailsDto(any());
    }

    @Test
    void getProjectByIdReturnsDetailsForViewableProject() {
        User student = userWithRole(2L, Role.STUDENT);
        User teacher = userWithRole(3L, Role.TEACHER);
        Project project = new Project();
        project.setId(11L);
        project.setStatus(ProjectStatusEnum.PUBLISHED);
        project.setCreatedBy(teacher);
        ProjectDetailsDto dto = new ProjectDetailsDto();

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));
        when(projectMapper.toDetailsDto(project)).thenReturn(dto);

        ProjectDetailsDto result = projectService.getProjectById(principal, 11L);

        assertThat(result).isSameAs(dto);
        verify(projectMapper).toDetailsDto(project);
    }

    @Test
    void getProjectByIdWithoutPrincipalReturnsEntity() {
        Project project = new Project();
        project.setId(99L);

        when(projectRepository.findById(99L)).thenReturn(Optional.of(project));

        assertThat(projectService.getProjectById(99L)).isSameAs(project);
    }

    @Test
    void getProjectByIdWithoutPrincipalThrowsWhenMissing() {
        when(projectRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(123L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Project");
    }


    @Test
    void getProjectByIdThrowsWhenUserCannotViewProject() {
        User student = userWithRole(4L, Role.STUDENT);
        Project project = new Project();
        project.setId(12L);
        project.setStatus(ProjectStatusEnum.PENDING);
        User creator = userWithRole(5L, Role.STUDENT);
        project.setCreatedBy(creator);
        project.setProductOwner(userWithRole(6L, Role.STUDENT));

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectRepository.findById(12L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.getProjectById(principal, 12L))
                .isInstanceOf(UserUnauthorizedException.class)
                .hasMessageContaining("4");
    }

    @Test
    void createProjectThrowsWhenUserAlreadyMemberOfProjectInAssignment() {
        User student = userWithRole(7L, Role.STUDENT);
        ProjectCreateDto dto = new ProjectCreateDto();
        Assignment assignment = new Assignment();

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(assignmentRepository.findById(20L)).thenReturn(Optional.of(assignment));
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, assignment)).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(principal, 20L, dto))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void createProjectThrowsWhenDeadlineExpiredForStudent() {
        User student = userWithRole(8L, Role.STUDENT);
        ProjectCreateDto dto = new ProjectCreateDto();
        Assignment assignment = new Assignment();
        Deadline deadline = mock(Deadline.class);

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(assignmentRepository.findById(21L)).thenReturn(Optional.of(assignment));
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, assignment)).thenReturn(false);
        when(deadlineService.getDeadlineByAssignmentAndRestrictions(assignment, DeadlineRestriction.PROJECT_CREATION)).thenReturn(deadline);
        when(deadline.hasExpired()).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(principal, 21L, dto))
                .isInstanceOf(DeadlineExpiredException.class);
    }

    @Test
    void createProjectAsStudentInitialisesProjectWithDefaults() {
        User student = userWithRole(9L, Role.STUDENT);
        Assignment assignment = new Assignment();
        assignment.setDefaultTeamSize(5);

        ProjectCreateDto dto = new ProjectCreateDto();
        Project mappedEntity = new Project();
        ProjectDetailsDto mappedDetails = new ProjectDetailsDto();

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(assignmentRepository.findById(22L)).thenReturn(Optional.of(assignment));
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, assignment)).thenReturn(false);
        when(deadlineService.getDeadlineByAssignmentAndRestrictions(assignment, DeadlineRestriction.PROJECT_CREATION)).thenReturn(null);
        when(projectMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(projectRepository.save(mappedEntity)).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            saved.setId(123L);
            return saved;
        });
        when(projectMapper.toDetailsDto(any(Project.class))).thenReturn(mappedDetails);

        ProjectDetailsDto result = projectService.createProject(principal, 22L, dto);

        assertThat(result).isSameAs(mappedDetails);
        assertThat(mappedEntity.getStatus()).isEqualTo(ProjectStatusEnum.PENDING);
        assertThat(mappedEntity.getMembers()).containsExactly(student);
        assertThat(mappedEntity.getProductOwner()).isEqualTo(student);
        assertThat(mappedEntity.getTeamSize()).isEqualTo(5);
        assertThat(mappedEntity.getAssignment()).isSameAs(assignment);
        assertThat(mappedEntity.getCreatedBy()).isSameAs(student);
    }

    @Test
    void createProjectAsTeacherSetsApprovedStatusWithoutMembers() {
        User teacher = userWithRole(10L, Role.TEACHER);
        Assignment assignment = new Assignment();
        ProjectCreateDto dto = new ProjectCreateDto();
        Project mappedEntity = new Project();

        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(assignmentRepository.findById(23L)).thenReturn(Optional.of(assignment));
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(teacher, assignment)).thenReturn(false);
        when(deadlineService.getDeadlineByAssignmentAndRestrictions(assignment, DeadlineRestriction.PROJECT_CREATION)).thenReturn(null);
        when(projectMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(projectRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(projectMapper.toDetailsDto(mappedEntity)).thenReturn(new ProjectDetailsDto());

        projectService.createProject(principal, 23L, dto);

        assertThat(mappedEntity.getStatus()).isEqualTo(ProjectStatusEnum.APPROVED);
        assertThat(mappedEntity.getMembers()).isEmpty();
        assertThat(mappedEntity.getProductOwner()).isNull();
    }

    @Test
    void saveThrowsWhenUserNotProductOwner() {
        User actor = userWithRole(11L, Role.STUDENT);
        Project project = new Project();
        project.setId(55L);
        project.setProductOwner(userWithRole(12L, Role.STUDENT));

        when(projectRepository.findById(55L)).thenReturn(Optional.of(project));
        when(userService.getUserByPrincipal(principal)).thenReturn(actor);

        assertThatThrownBy(() -> projectService.save(principal, 55L, new ProjectUpdateDto()))
                .isInstanceOf(UserNotOwnerOfProjectException.class);
    }

    @Test
    void saveMarksNeedsRevisionProjectsAsRevisedAndPersistsChanges() {
        User actor = userWithRole(13L, Role.STUDENT);
        Project project = new Project();
        project.setId(56L);
        project.setStatus(ProjectStatusEnum.NEEDS_REVISION);
        project.setProductOwner(actor);
        project.setTags(new java.util.ArrayList<>());

        ProjectUpdateDto dto = new ProjectUpdateDto();

        when(projectRepository.findById(56L)).thenReturn(Optional.of(project));
        when(userService.getUserByPrincipal(principal)).thenReturn(actor);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDetailsDto(project)).thenReturn(new ProjectDetailsDto());

        projectService.save(principal, 56L, dto);

        assertThat(project.getStatus()).isEqualTo(ProjectStatusEnum.REVISED);
        verify(projectMapper).updateEntityFromDto(actor, dto, project);
        verify(projectRepository).save(project);
    }

    @Test
    void changeProjectStatusRejectsAlreadyRejectedProject() {
        User teacher = userWithRole(14L, Role.TEACHER);
        Project project = new Project();
        project.setId(60L);
        project.setStatus(ProjectStatusEnum.REJECTED);
        project.setAssignment(new Assignment());

        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(projectRepository.findById(60L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.changeProjectStatus(principal, 60L, ProjectStatusEnum.APPROVED))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("rejected project");
    }

    @Test
    void changeProjectStatusRejectsGlobalProject() {
        User teacher = userWithRole(15L, Role.TEACHER);
        Project project = new Project();
        project.setId(61L);
        project.setStatus(ProjectStatusEnum.PENDING);
        project.setAssignment(null);

        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(projectRepository.findById(61L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.changeProjectStatus(principal, 61L, ProjectStatusEnum.APPROVED))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("global project");
    }

    @Test
    void changeProjectStatusSendsNotificationsToResearcherCreatorAndProductOwner() {
        User admin = userWithRole(16L, Role.TEACHER);
        User researcherCreator = userWithRole(17L, Role.RESEARCHER);
        User productOwner = userWithRole(18L, Role.STUDENT);

        com.ehb.connected.domain.impl.courses.entities.Course course = new com.ehb.connected.domain.impl.courses.entities.Course();
        course.setName("Advanced Software");

        Assignment assignment = new Assignment();
        assignment.setId(90L);
        assignment.setName("Capstone Project");
        assignment.setCourse(course);

        Project project = new Project();
        project.setId(62L);
        project.setStatus(ProjectStatusEnum.PENDING);
        project.setAssignment(assignment);
        project.setCreatedBy(researcherCreator);
        project.setProductOwner(productOwner);

        when(userService.getUserByPrincipal(principal)).thenReturn(admin);
        when(projectRepository.findById(62L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDetailsDto(project)).thenReturn(new ProjectDetailsDto());

        projectService.changeProjectStatus(principal, 62L, ProjectStatusEnum.APPROVED);

        verify(notificationService).createNotification(
                eq(researcherCreator),
                eq("The project in assignment: Capstone Projectstatus has been set to: approved"),
                eq("/projects/62")
        );

        verify(notificationService).createNotification(
                eq(productOwner),
                eq("Your project status has been set to: approved"),
                eq("course/advanced-software/assignment/capstone-project/projects/62")
        );
    }

    @Test
    void publishAllProjectsDelegatesToChangeStatusForEachApprovedProject() {
        Project project1 = new Project();
        project1.setId(70L);
        Project project2 = new Project();
        project2.setId(71L);

        when(projectRepository.findAllByAssignmentIdAndStatus(30L, ProjectStatusEnum.APPROVED))
                .thenReturn(List.of(project1, project2));
        when(projectMapper.toDetailsDtoList(List.of(project1, project2))).thenReturn(List.of(new ProjectDetailsDto(), new ProjectDetailsDto()));

        ProjectServiceImpl spyService = org.mockito.Mockito.spy(projectService);
        org.mockito.Mockito.doReturn(new ProjectDetailsDto()).when(spyService)
                .changeProjectStatus(principal, 70L, ProjectStatusEnum.PUBLISHED);
        org.mockito.Mockito.doReturn(new ProjectDetailsDto()).when(spyService)
                .changeProjectStatus(principal, 71L, ProjectStatusEnum.PUBLISHED);

        List<ProjectDetailsDto> result = spyService.publishAllProjects(principal, 30L);

        assertThat(result).hasSize(2);
        verify(spyService).changeProjectStatus(principal, 70L, ProjectStatusEnum.PUBLISHED);
        verify(spyService).changeProjectStatus(principal, 71L, ProjectStatusEnum.PUBLISHED);
    }

    @Test
    void getAllApplicationsByProjectIdThrowsWhenActorNotOwnerOrTeacher() {
        User actor = userWithRole(19L, Role.STUDENT);
        User productOwner = userWithRole(20L, Role.STUDENT);
        Project project = new Project();
        project.setId(80L);
        project.setProductOwner(productOwner);

        when(userService.getUserByPrincipal(principal)).thenReturn(actor);
        when(projectRepository.findById(80L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.getAllApplicationsByProjectId(principal, 80L))
                .isInstanceOf(UserNotOwnerOfProjectException.class);
    }

    @Test
    void getAllApplicationsByProjectIdReturnsMappedDtos() {
        User actor = userWithRole(21L, Role.TEACHER);
        Project project = new Project();
        project.setId(81L);
        project.setApplications(List.of(new Application()));

        List<ApplicationDetailsDto> mapped = List.of(mock(ApplicationDetailsDto.class));

        when(userService.getUserByPrincipal(principal)).thenReturn(actor);
        when(projectRepository.findById(81L)).thenReturn(Optional.of(project));
        when(applicationMapper.toDtoList(project.getApplications())).thenReturn(mapped);

        List<ApplicationDetailsDto> result = projectService.getAllApplicationsByProjectId(principal, 81L);

        assertThat(result).isEqualTo(mapped);
    }

    @Test
    void removeMemberThrowsWhenActorNotTeacher() {
        User actor = userWithRole(22L, Role.STUDENT);
        Project project = new Project();
        project.setId(90L);

        when(projectRepository.findById(90L)).thenReturn(Optional.of(project));
        when(userService.getUserByPrincipal(principal)).thenReturn(actor);

        assertThatThrownBy(() -> projectService.removeMember(principal, 90L, 200L))
                .isInstanceOf(UserUnauthorizedException.class);
    }

    @Test
    void removeMemberUpdatesMembershipApplicationsAndNotifications() {
        User teacher = userWithRole(23L, Role.TEACHER);
        User kicked = userWithRole(24L, Role.STUDENT);
        User remaining = userWithRole(25L, Role.STUDENT);
        User productOwner = kicked;

        Assignment assignment = new Assignment();
        assignment.setName("Innovation Lab");

        com.ehb.connected.domain.impl.courses.entities.Course course = new com.ehb.connected.domain.impl.courses.entities.Course();
        course.setName("Creative Engineering");
        assignment.setCourse(course);

        Project project = new Project();
        project.setId(91L);
        project.setAssignment(assignment);
        project.setTitle("Prototype Builder");
        project.setMembers(new java.util.ArrayList<>(List.of(kicked, remaining)));
        project.setProductOwner(productOwner);

        Application application = new Application();
        application.setApplicant(kicked);
        project.setApplications(new java.util.ArrayList<>(List.of(application)));

        when(projectRepository.findById(91L)).thenReturn(Optional.of(project));
        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(userService.getUserById(24L)).thenReturn(kicked);

        projectService.removeMember(principal, 91L, 24L);

        assertThat(project.getMembers()).containsExactly(remaining);
        assertThat(project.getProductOwner()).isEqualTo(remaining);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatusEnum.REJECTED);

        verify(projectRepository).save(project);
        verify(notificationService).createNotification(
                eq(kicked),
                eq("You have been removed from project: Prototype Builder"),
                eq("course/creative-engineering/assignment/innovation-lab/projects/91")
        );
    }

    @Test
    void claimProjectThrowsWhenUserAlreadyMemberInAssignment() {
        User student = userWithRole(26L, Role.STUDENT);
        Assignment assignment = new Assignment();
        Project project = new Project();
        project.setId(100L);
        project.setAssignment(assignment);

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, assignment)).thenReturn(true);

        assertThatThrownBy(() -> projectService.claimProject(principal, 100L))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void claimProjectAddsUserAndRejectsOtherApplications() {
        User student = userWithRole(27L, Role.STUDENT);
        Assignment assignment = new Assignment();
        Project project = new Project();
        project.setId(101L);
        project.setAssignment(assignment);
        project.setMembers(new java.util.ArrayList<>());

        Application application1 = new Application();
        application1.setApplicant(student);
        application1.setStatus(ApplicationStatusEnum.PENDING);
        application1.setProject(project);
        Application application2 = new Application();
        application2.setApplicant(new User());
        application2.setStatus(ApplicationStatusEnum.PENDING);
        application2.setProject(project);
        project.setApplications(new java.util.ArrayList<>(List.of(application1, application2)));

        student.setApplications(List.of(application1));

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectRepository.findById(101L)).thenReturn(Optional.of(project));
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, assignment)).thenReturn(false);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toDetailsDto(project)).thenReturn(new ProjectDetailsDto());

        projectService.claimProject(principal, 101L);

        assertThat(project.getMembers()).contains(student);
        assertThat(project.getProductOwner()).isEqualTo(student);
        assertThat(application1.getStatus()).isEqualTo(ApplicationStatusEnum.REJECTED);
        assertThat(application2.getStatus()).isEqualTo(ApplicationStatusEnum.PENDING);
    }

    @Test
    void importProjectRejectsWhenProjectNotGlobal() {
        User teacher = userWithRole(28L, Role.TEACHER);
        Project existing = new Project();
        existing.setId(110L);
        existing.setAssignment(new Assignment());
        existing.setGid(null);

        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(projectRepository.findById(110L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(40L)).thenReturn(Optional.of(new Assignment()));

        assertThatThrownBy(() -> projectService.importProject(principal, 40L, 110L))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("not global");
    }

    @Test
    void importProjectRejectsWhenDuplicateGidExists() {
        User teacher = userWithRole(29L, Role.TEACHER);
        Assignment targetAssignment = new Assignment();
        Project source = new Project();
        source.setId(111L);
        source.setGid(UUID.randomUUID());

        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(projectRepository.findById(111L)).thenReturn(Optional.of(source));
        when(assignmentRepository.findById(41L)).thenReturn(Optional.of(targetAssignment));
        when(projectRepository.existsByAssignmentIdAndGid(41L, source.getGid())).thenReturn(true);

        assertThatThrownBy(() -> projectService.importProject(principal, 41L, 111L))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void importProjectRejectsWhenUserAlreadyMemberInAssignment() {
        User student = userWithRole(30L, Role.STUDENT);
        Assignment targetAssignment = new Assignment();
        Project source = new Project();
        source.setId(112L);
        source.setGid(UUID.randomUUID());

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectRepository.findById(112L)).thenReturn(Optional.of(source));
        when(assignmentRepository.findById(42L)).thenReturn(Optional.of(targetAssignment));
        when(projectRepository.existsByAssignmentIdAndGid(42L, source.getGid())).thenReturn(false);
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, targetAssignment)).thenReturn(true);

        assertThatThrownBy(() -> projectService.importProject(principal, 42L, 112L))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void importProjectPersistsClonedProjectAndNotifiesCreator() {
        User student = userWithRole(31L, Role.STUDENT);
        User researcherCreator = userWithRole(32L, Role.RESEARCHER);

        Assignment targetAssignment = new Assignment();
        targetAssignment.setName("Data Science");

        com.ehb.connected.domain.impl.courses.entities.Course course = new com.ehb.connected.domain.impl.courses.entities.Course();
        course.setName("Machine Learning");
        targetAssignment.setCourse(course);

        Project source = new Project();
        source.setId(113L);
        source.setGid(UUID.randomUUID());
        source.setTitle("Global AI Project");
        source.setDescription("description");
        source.setShortDescription("short");
        source.setTeamSize(6);
        source.setBackgroundImage("bg.png");
        source.setCreatedBy(researcherCreator);
        source.setTags(new java.util.ArrayList<>());

        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectRepository.findById(113L)).thenReturn(Optional.of(source));
        when(assignmentRepository.findById(43L)).thenReturn(Optional.of(targetAssignment));
        when(projectRepository.existsByAssignmentIdAndGid(43L, source.getGid())).thenReturn(false);
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, targetAssignment)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project savedProject = invocation.getArgument(0);
            savedProject.setId(987L);
            return savedProject;
        });
        when(projectMapper.toDetailsDto(any(Project.class))).thenReturn(new ProjectDetailsDto());

        projectService.importProject(principal, 43L, 113L);

        ArgumentCaptor<Project> savedCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(savedCaptor.capture());
        Project saved = savedCaptor.getValue();

        assertThat(saved.getGid()).isEqualTo(source.getGid());
        assertThat(saved.getAssignment()).isSameAs(targetAssignment);
        assertThat(saved.getMembers()).containsExactly(student);
        assertThat(saved.getProductOwner()).isEqualTo(student);
        assertThat(saved.getCreatedBy()).isEqualTo(researcherCreator);
        assertThat(saved.getStatus()).isEqualTo(ProjectStatusEnum.PENDING);

        verify(notificationService).createNotification(
                eq(researcherCreator),
                eq("Your project has been imported to assignment: Data Science"),
                eq("/projects/" + saved.getId())
        );
    }

    @Test
    void getAllGlobalProjectsReturnsResearcherOwnedWhenActorResearcher() {
        User researcher = userWithRole(33L, Role.RESEARCHER);
        when(userService.getUserByPrincipal(principal)).thenReturn(researcher);
        when(projectRepository.findAllByCreatedBy(researcher)).thenReturn(List.of(new Project()));
        when(projectMapper.toDetailsDtoList(any())).thenReturn(List.of(new ProjectDetailsDto()));

        assertThat(projectService.getAllGlobalProjects(principal)).hasSize(1);
        verify(projectRepository).findAllByCreatedBy(researcher);
        verify(projectRepository, never()).findAllByCreatedByRoleAndAssignmentIsNull(any());
    }

    @Test
    void getAllGlobalProjectsReturnsAllResearcherProjectsForNonResearchers() {
        User teacher = userWithRole(34L, Role.TEACHER);
        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);
        when(projectRepository.findAllByCreatedByRoleAndAssignmentIsNull(Role.RESEARCHER)).thenReturn(List.of(new Project()));
        when(projectMapper.toDetailsDtoList(any())).thenReturn(List.of(new ProjectDetailsDto()));

        assertThat(projectService.getAllGlobalProjects(principal)).hasSize(1);
        verify(projectRepository).findAllByCreatedByRoleAndAssignmentIsNull(Role.RESEARCHER);
    }

    private User userWithRole(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}














