package com.ehb.connected.domain.impl.courses.controllers;

import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final UserServiceImpl userService;
    private final WebClient webClient;

    //TODO: is EnrollmentType necessary?
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

//    //Create a course in the local database not in Canvas
//    @PostMapping("/create")
//    public ResponseEntity<String> createCourse(Principal principal, @RequestBody Course course) {
//
//    }
}
