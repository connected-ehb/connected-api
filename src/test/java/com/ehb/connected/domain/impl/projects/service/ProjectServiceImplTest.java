package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        Project project = new Project();
        project.setTitle("Project 1");
        project.setDescription("Description 1");

        when(projectRepository.save(project)).thenReturn(project);

        Project result= projectServiceImpl.createProject(project);
        assertNotNull(result);
        assertEquals("Project 1", result.getTitle());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    public void testUpdateProject(){
        Project project = new Project();
        project.setId(1L);
        project.setTitle("Project updated!");

        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectServiceImpl.updateProject(project);
        assertNotNull(result);
        assertEquals("Project updated!",result.getTitle());
        verify(projectRepository,times(1)).save(project);
    }

    @Test
    public void testDeleteProject(){
        Long projectId = 1L;
        doNothing().when(projectRepository).deleteById(1L);
        projectServiceImpl.deleteProject(projectId);
        verify(projectRepository, times(1)).deleteById(projectId);
    }




}
