package com.ehb.connected.domain.impl.projects.service;

import org.junit.jupiter.api.Test;


public class ProjectServiceImplTest {

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
