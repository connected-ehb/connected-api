package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.services.CourseService;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseService courseService;
    private final AssignmentMapper assignmentMapper;

    private final WebClient webClient;

    @Override
    public AssignmentDetailsDto createAssignment(AssignmentCreateDto assignmentDto) {
        final Course course = courseService.getCourseById(assignmentDto.getCourseId());
        final Assignment assignmentEntity = assignmentMapper.AssignmentCreateToEntity(assignmentDto);
        assignmentEntity.setCourse(course);
        return assignmentMapper.toAssignmentDetailsDto(assignmentRepository.save(assignmentEntity));
    }

    @Override
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(Assignment.class, assignmentId));
    }

    @Override
    public List<AssignmentDetailsDto> getAllAssignmentsByCourse(Long courseId) {
        return assignmentMapper.toAssignmentDetailsDtoList(assignmentRepository.findByCourseId(courseId));
    }

    //TODO: Check .block() usage and refactor to avoid blocking calls.
    @Override
    public List<AssignmentDetailsDto> getNewAssignmentsFromCanvas(Principal principal, Long courseId) {
        final Course course = courseService.getCourseById(courseId);
        final Long canvasCourseId = course.getCanvasId();

        // Retrieve assignments from Canvas as a List of Maps.
        final List<Map<String, Object>> assignments = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses/{canvasCourseId}/assignments")
                        .build(canvasCourseId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .block();

        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }

        return assignments.stream()
                .filter(assignment -> {
                    Long canvasAssignmentId = Long.parseLong(assignment.get("id").toString());
                    return !existsAssignmentByCanvasId(canvasAssignmentId);
                })
                .map(assignment -> assignmentMapper.fromCanvasMapToAssignmentDetailsDto(assignment, course.getId())).toList();
    }

    private boolean existsAssignmentByCanvasId(Long canvasAssignmentId) {
        return assignmentRepository.existsByCanvasId(canvasAssignmentId);
    }

    public void deleteAssignmentById(Principal principal, Long assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);
        assignmentRepository.delete(assignment);
    }
}