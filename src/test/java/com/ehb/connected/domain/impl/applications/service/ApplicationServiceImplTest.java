package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import com.ehb.connected.domain.impl.projects.service.ProjectUserService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock private UserService userService;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ProjectService projectService;
    @Mock private DeadlineService deadlineService;
    @Mock private ApplicationMapper applicationMapper;
    @Mock private NotificationServiceImpl notificationService;
    @Mock private ProjectUserService projectUserService;

    @InjectMocks private ApplicationServiceImpl applicationService;

    private Principal principal;
    private User student;
    private User teacher;
    private Project project;
    private Course course;

    @BeforeEach
    void setUp() {
        principal = () -> "student@example.com";

        student = new User();
        student.setId(10L);
        student.setFirstName("Alice");
        student.setLastName("Doe");
        student.setRole(Role.STUDENT);

        teacher = new User();
        teacher.setId(20L);
        teacher.setRole(Role.TEACHER);

        course = new Course();
        course.setId(5L);
        course.setName("Software Engineering");

        var assignment = new com.ehb.connected.domain.impl.assignments.entities.Assignment();
        assignment.setId(30L);
        assignment.setName("Capstone");
        assignment.setCourse(course);

        project = new Project();
        project.setId(40L);
        project.setAssignment(assignment);
        project.setMembers(new ArrayList<>());
        project.setProductOwner(teacher);
        project.setStatus(ProjectStatusEnum.PUBLISHED);
        project.setTitle("AI Assistant");

        lenient().when(applicationMapper.toDto(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            ApplicationDetailsDto dto = new ApplicationDetailsDto(0L, "m", ApplicationStatusEnum.PENDING, new ProjectDetailsDto(), new UserDetailsDto());
            dto.setId(app.getId());
            dto.setStatus(app.getStatus());
            dto.setMotivationMd(app.getMotivationMd());
            return dto;
        });
    }

    @Test
    void getByIdAllowsTeacherAccess() {
        Application application = new Application(1L, "motivation", ApplicationStatusEnum.PENDING, project, student);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);

        var result = applicationService.getById(principal, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getByIdThrowsWhenUnauthorized() {
        Application application = new Application(1L, "motivation", ApplicationStatusEnum.PENDING, project, teacher);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userService.getUserByPrincipal(principal)).thenReturn(student);

        assertThatThrownBy(() -> applicationService.getById(principal, 1L))
                .isInstanceOf(UserUnauthorizedException.class);
    }

    @Test
    void createApplicationPersistsAndNotifiesOwner() {
        ApplicationCreateDto dto = new ApplicationCreateDto();
        dto.setMotivationMd("I love this project");

        project.setMembers(new ArrayList<>(List.of(teacher)));
        project.setTeamSize(4);

        when(projectService.getProjectById(40L)).thenReturn(project);
        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, project.getAssignment())).thenReturn(false);
        
        Application saved = new Application(2L, "I love this project", ApplicationStatusEnum.PENDING, project, student);
        when(applicationRepository.save(any(Application.class))).thenReturn(saved);

        var result = applicationService.create(principal, 40L, dto);

        assertThat(result.getMotivationMd()).isEqualTo("I love this project");
        verify(notificationService).createNotification(eq(project.getProductOwner()), any(), any());
    }

    @Test
    void createApplicationRejectsWhenProjectNotPublished() {
        project.setStatus(ProjectStatusEnum.PENDING);
        when(projectService.getProjectById(40L)).thenReturn(project);
        when(userService.getUserByPrincipal(principal)).thenReturn(student);

        assertThatThrownBy(() -> applicationService.create(principal, 40L, new ApplicationCreateDto()))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void createApplicationRejectsWhenDeadlineExpired() {
        when(projectService.getProjectById(40L)).thenReturn(project);
        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        Deadline deadline = new Deadline();
        deadline.setDueDate(LocalDateTime.now().minusDays(1));
        when(deadlineService.getDeadlineByAssignmentAndRestrictions(project.getAssignment(), DeadlineRestriction.APPLICATION_SUBMISSION)).thenReturn(deadline);

        assertThatThrownBy(() -> applicationService.create(principal, 40L, new ApplicationCreateDto()))
                .isInstanceOf(DeadlineExpiredException.class);
    }

    @Test
    void getAllApplicationsReturnsStudentSubset() {
        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(applicationRepository.findAllApplicationsByUserIdOrProjectProductOwnerAndAssignment(student.getId(), 30L))
                .thenReturn(List.of(new Application()));

        applicationService.getAllApplications(principal, 30L);

        verify(applicationRepository).findAllApplicationsByUserIdOrProjectProductOwnerAndAssignment(student.getId(), 30L);
    }

    @Test
    void reviewApplicationSetsStatusAndNotifies() {
        Application application = new Application(3L, "Motivation", ApplicationStatusEnum.PENDING, project, student);
        when(applicationRepository.findById(3L)).thenReturn(Optional.of(application));
        when(userService.getUserByPrincipal(principal)).thenReturn(teacher);

        applicationService.reviewApplication(principal, 3L, ApplicationStatusEnum.APPROVED);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatusEnum.APPROVED);
        verify(notificationService).createNotification(eq(student), any(), any());
        verify(applicationRepository).save(application);
    }

    @Test
    void reviewApplicationRejectsWhenNotOwner() {
        Application application = new Application(3L, "Motivation", ApplicationStatusEnum.PENDING, project, student);
        when(applicationRepository.findById(3L)).thenReturn(Optional.of(application));
        when(userService.getUserByPrincipal(principal)).thenReturn(student);

        assertThatThrownBy(() -> applicationService.reviewApplication(principal, 3L, ApplicationStatusEnum.APPROVED))
                .isInstanceOf(UserNotOwnerOfProjectException.class);
    }

    @Test
    void joinProjectAddsMemberAndRejectsOtherApplications() {
        Application approved = new Application(7L, "Motivation", ApplicationStatusEnum.APPROVED, project, student);
        project.setMembers(new ArrayList<>(List.of(teacher)));
        project.setTeamSize(4);
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(approved));
        when(userService.getUserByPrincipal(principal)).thenReturn(student);
        when(projectUserService.isUserMemberOfAnyProjectInAssignment(student, project.getAssignment())).thenReturn(false);
        when(applicationRepository.findByApplicantInAssignment(project.getAssignment().getId(), student))
                .thenReturn(List.of(approved, new Application(8L, "Other", ApplicationStatusEnum.PENDING, project, student)));

        applicationService.joinProject(principal, 7L);

        assertThat(project.getMembers()).contains(student);
        verify(projectService).save(project);
        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ApplicationStatusEnum.REJECTED);
    }

    @Test
    void joinProjectRejectsWhenStatusNotApproved() {
        Application pending = new Application(7L, "Motivation", ApplicationStatusEnum.PENDING, project, student);
        when(applicationRepository.findById(7L)).thenReturn(Optional.of(pending));
        when(userService.getUserByPrincipal(principal)).thenReturn(student);

        assertThatThrownBy(() -> applicationService.joinProject(principal, 7L))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("not been approved");
    }
}





