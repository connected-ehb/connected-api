package com.ehb.connected.domain.impl.courses.controllers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.services.CourseService;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final UserService userService;

    //@PreAuthorize("hasAnyAuthority('canvas:sync')")
    @PostMapping("/canvas")
    public ResponseEntity<List<CourseDetailsDto>> getNewCoursesFromCanvas(Authentication authentication) {
        List<CourseDetailsDto> newCourses = courseService.getNewCoursesFromCanvas(authentication);
        return ResponseEntity.ok(newCourses);
    }

    @PreAuthorize("hasAnyAuthority('course:create')")
    @PostMapping("/")
    public ResponseEntity<CourseDetailsDto> createCourse(Principal principal, @RequestBody CourseCreateDto course) {
        CourseDetailsDto courseDetails = courseService.createCourseWithEnrollments(principal, course);
        return ResponseEntity.ok(courseDetails);
    }

    @PreAuthorize("hasAnyAuthority('course:read_all')")
    @GetMapping("/")
    public ResponseEntity<List<CourseDetailsDto>> getCourses(Principal principal) {
        return ResponseEntity.ok(courseService.getCoursesByOwner(principal));
    }

    @PreAuthorize("hasAnyAuthority('course:read_enrolled')")
    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDetailsDto>> getEnrolledCourses(Principal principal) {
        return ResponseEntity.ok(courseService.getCoursesByEnrollment(principal));
    }

    @PreAuthorize("hasAnyAuthority('assignment:read_all')")
    @GetMapping("/{courseId}/assignments")
    public ResponseEntity<List<AssignmentDetailsDto>> getAllAssignmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(assignmentService.getAllAssignmentsByCourse(courseId));
    }

    @PreAuthorize("hasAnyAuthority('course:view_students')")
    @GetMapping("/{courseId}/students")
    public List<UserDetailsDto> getAllEnrolledStudentsByCourse(@PathVariable Long courseId){
        return userService.getAllStudentsByCourseId(courseId);
    }

    @PreAuthorize("hasAnyAuthority('course:delete')")
    @DeleteMapping("/{courseId}")
    public  ResponseEntity<Void> deleteCourse(@PathVariable Long courseId){
        courseService.deleteCourseById(courseId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('course:refresh')")
    @PostMapping("/{courseId}/enrollments/refresh")
    public ResponseEntity<CourseDetailsDto> refreshEnrollments(Principal principal, @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.refreshEnrollments(principal, courseId));
    }
}
