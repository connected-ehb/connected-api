package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

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

}
