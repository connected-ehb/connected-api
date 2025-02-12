package com.ehb.connected.domain.impl.courses.controllers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.mappers.CourseMapper;
import com.ehb.connected.domain.impl.courses.services.CourseServiceImpl;
import com.ehb.connected.domain.impl.enrollments.services.EnrollmentService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final UserServiceImpl userService;
    private final WebClient webClient;
    private final CourseMapper courseMapper;
    private final CourseServiceImpl courseService;
    private final AssignmentService assignmentService;
    private final AssignmentMapper assignmentMapper;
    private final EnrollmentService enrollmentService;

    //TODO: is EnrollmentType necessary? && move logic to service
    @PostMapping("/canvas")
    public ResponseEntity<String> getCourses(Principal principal, @RequestParam String EnrollmentType) {
        User user = userService.getUserByEmail(principal.getName());
        String token = user.getAccessToken();

        String jsonResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses")
                        .queryParam("EnrollmentType", EnrollmentType.toLowerCase())
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok().body(jsonResponse);
    }

    /**
     * Create a course
     * @param principal the security principal of the user creating the course
     * @param course the courseCreateDto object containing the course information to be created
     * @return a response entity with a message indicating the course was created
     */
    //TODO: use getAuthorities() to check if user has permission to create course
    //@PreAuthorize("hasAuthority('course:create')")
    @PostMapping("/")
    public ResponseEntity<Map<String, String>> createCourse(Principal principal, @RequestBody CourseCreateDto course) {
        User user = userService.getUserByEmail(principal.getName());
        String token = user.getAccessToken();

        Course courseEntity = courseMapper.CourseCreateToEntity(course, principal);
        courseService.createCourse(courseEntity);

        String courseId = courseEntity.getCanvasCourseId().toString();
        String enrollmentsResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses/{course_id}/enrollments")
                        .build(courseId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Enrollments " + enrollmentsResponse);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> enrollments = objectMapper.readValue(enrollmentsResponse, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> enrollment : enrollments) {
                Long canvasUserId = Long.parseLong(enrollment.get("user_id").toString());
                enrollmentService.enrollUser(courseEntity, canvasUserId);
            }
        } catch (Exception e) {
            System.out.println("Error parsing enrollments: " + e.getMessage());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Course created successfully");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/")
    public ResponseEntity<List<CourseDetailsDto>> getCourses(Principal principal) {
        List<Course> courses = courseService.getCoursesByOwner(principal);
        List<CourseDetailsDto> courseDetailsDtos = courseMapper.toCourseDetailsDtoList(courses);
        ResponseEntity<List<CourseDetailsDto>> response = ResponseEntity.ok().body(courseDetailsDtos);
        return response;
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDetailsDto>> getEnrolledCourses(Principal principal) {
        List<Course> courses = courseService.getCoursesByEnrollment(principal);
        List<CourseDetailsDto> courseDetailsDtos = courseMapper.toCourseDetailsDtoList(courses);
        ResponseEntity<List<CourseDetailsDto>> response = ResponseEntity.ok().body(courseDetailsDtos);
        return response;
    }

    @GetMapping("/{courseId}/assignments")
    public ResponseEntity<List<AssignmentDetailsDto>> getAllAssignmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentMapper.toAssignmentDetailsDtoList(assignmentService.getAllAssignmentsByCourse(courseId)));
    }

}
