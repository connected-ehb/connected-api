package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Principal principal;
    @InjectMocks
    private ProjectServiceImpl projectServiceImpl;

    @Test
    public void testGetProjectById() {
        //TODO
    }

    @Test
    public void testGetAllProjects(){
        //TODO
    }

    @Test
    public void testCreateProject(){
        //TODO
    }


    @Test
    public void testUpdateProject() {
        // TODO

    }


    @Test
    public void testDeleteProject(){
        //TODO
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
