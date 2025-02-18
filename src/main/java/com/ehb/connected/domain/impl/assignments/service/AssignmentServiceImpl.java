package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id).orElse(null);
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    @Override
    public List<Assignment> getAllAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    @Override
    public Assignment getAssignmentByCanvasAssignmentId(Long canvasAssignmentId) {
        return assignmentRepository.findByCanvasAssignmentId(canvasAssignmentId).orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
    }

    @Override
    public List<ProjectDetailsDto> publishAllProjects(Principal principal, Long assignmentId) {
        List<Project> projects = projectService.getAllProjectsByStatus(assignmentId, ProjectStatusEnum.APPROVED);
        projects.forEach(project -> projectService.changeProjectStatus(principal, project.getId(), ProjectStatusEnum.PUBLISHED));
        return projectService.getAllPublishedProjectsByAssignmentId(assignmentId);
    }
}