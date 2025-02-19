package com.ehb.connected.domain.impl.courses.controllers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.services.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;

    @PostMapping("/canvas")
    public ResponseEntity<List<CourseDetailsDto>> getNewCoursesFromCanvas(Principal principal) {
        List<CourseDetailsDto> newCourses = courseService.getNewCoursesFromCanvas(principal);
        return ResponseEntity.ok(newCourses);
    }

    @PostMapping("/")
    public ResponseEntity<CourseDetailsDto> createCourse(Principal principal, @RequestBody CourseCreateDto course) {
        CourseDetailsDto courseDetails = courseService.createCourseWithEnrollments(principal, course);
        return ResponseEntity.ok(courseDetails);
    }

    @GetMapping("/")
    public ResponseEntity<List<CourseDetailsDto>> getCourses(Principal principal) {
        return ResponseEntity.ok(courseService.getCoursesByOwner(principal));
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDetailsDto>> getEnrolledCourses(Principal principal) {
        return ResponseEntity.ok(courseService.getCoursesByEnrollment(principal));
    }

    @GetMapping("/{courseId}/assignments")
    public ResponseEntity<List<AssignmentDetailsDto>> getAllAssignmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAllAssignmentsByCourse(courseId));
    }

}
