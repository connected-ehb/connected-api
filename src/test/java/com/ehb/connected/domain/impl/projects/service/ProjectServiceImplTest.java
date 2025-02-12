package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.antlr.v4.runtime.tree.xpath.XPath.findAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Principal principal;
    @InjectMocks
    private ProjectServiceImpl projectServiceImpl;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetProjectById() {
        Project project = new Project();
        project.setId(1L);
        project.setTitle("Project 1");
        project.setDescription("Description 1");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectServiceImpl.getProjectById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Project 1", result.getTitle());
        assertEquals("Description 1", result.getDescription());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAllProjects(){
        Project project1 = new Project();
        Project project2 = new Project();
        List<Project> projects = Arrays.asList(project1, project2);

        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectServiceImpl.getAllProjects();
        assertEquals(2, result.size());
        verify(projectRepository,times(1)).findAll();

    }

    @Test
    public void testCreateProject(){
        //TODO
    }


    @Test
    public void testUpdateProject() {
        // Create an existing project in the repository
        Project existingProject = new Project();
        existingProject.setId(1L);
        existingProject.setTitle("Old Title");

        User user = new User();
        user.setEmail("testmail");
        existingProject.setCreatedBy(user);

        // Create the new project data
        ProjectUpdateDto updatedProject = new ProjectUpdateDto();
        updatedProject.setTitle("Project updated!");

        // Mock Principal behavior
        when(principal.getName()).thenReturn("testmail");

        // Mock repository behavior
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the service method
        Project result = projectServiceImpl.updateProject(principal, 1L, updatedProject);

        // Assertions
        assertNotNull(result);
        assertEquals("Project updated!", result.getTitle());

        // Verify repository interactions
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(existingProject);
    }


    @Test
    public void testDeleteProject(){
        Long projectId = 1L;
        doNothing().when(projectRepository).deleteById(1L);
        projectServiceImpl.deleteProject(projectId);
        verify(projectRepository, times(1)).deleteById(projectId);
    }


    @Test
    public void testApproveProject() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(ProjectStatusEnum.PENDING);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        projectServiceImpl.approveProject(1L);

        assertEquals(ProjectStatusEnum.APPROVED, project.getStatus());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    public void testRejectProject() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(ProjectStatusEnum.PENDING);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        projectServiceImpl.rejectProject(1L);

        assertEquals(ProjectStatusEnum.REJECTED, project.getStatus());
        verify(projectRepository, times(1)).save(project);
    }



}
