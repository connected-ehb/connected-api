package com.ehb.connected.domain.impl.assignments.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.courses.services.CourseService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final AssignmentMapper assignmentMapper;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final WebClient webClient;
    private final ApplicationServiceImpl applicationService;
    private final ApplicationMapper applicationMapper;


    // TODO move logic to service layer
    @PostMapping("/canvas/{courseId}")
    public ResponseEntity<String> getAssignmentsFromCanvas(@PathVariable Long courseId, Principal principal) {

        Course course = courseService.getCourseById(courseId);
        Long canvasCourseId = course.getCanvasCourseId();
        User user = userService.getUserByEmail(principal.getName());
        String token = user.getAccessToken();

        String jsonResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses/{canvasCourseId}/assignments")
                        .build(canvasCourseId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok().body(jsonResponse);
    }

    // TODO move logic to service layer
    @PostMapping("/")
    public ResponseEntity<Map<String, String>> createAssignment(@RequestBody AssignmentCreateDto assignment) {
        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Assignment assignmentEntity = assignmentMapper.AssignmentCreateToEntity(assignment);
        assignmentEntity.setCourse(course);
        assignmentService.createAssignment(assignmentEntity);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Assignment created successfully");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public AssignmentDetailsDto getAssignmentById(@PathVariable Long id){
        return assignmentMapper.toAssignmentDetailsDto(assignmentService.getAssignmentById(id));
    }

    @PatchMapping("/{id}")
    public Assignment updateAssignment(@PathVariable Long id, Assignment assignment){
        return assignmentService.updateAssignment(assignment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable Long id){
        assignmentService.deleteAssignment(id);
    }


    //TODO mapping should be moved to service layer
    @GetMapping("/{id}/applications")
    public ResponseEntity<List<ApplicationDto>> getAllApplications(@PathVariable Long id){
        List<ApplicationDto> applications = applicationService.findAllApplications(id).stream()
                .map(applicationMapper::toDto)
                .toList();

        return ResponseEntity.ok(applications);
    }


}
