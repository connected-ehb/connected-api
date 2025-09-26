package com.ehb.connected.domain.impl.courses.services;

import com.ehb.connected.domain.impl.auth.helpers.CanvasTokenService;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.mappers.CourseMapper;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.enrollments.services.EnrollmentService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import com.ehb.connected.exceptions.AccessTokenExpiredException;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserServiceImpl userService;
    private final EnrollmentService enrollmentService;
    private final CanvasTokenService canvasTokenService;
    private final WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Override
    public List<CourseDetailsDto> getNewCoursesFromCanvas(Principal principal) {
        User user = userService.getUserFromAnyPrincipal(principal);
        String token = canvasTokenService.getValidAccessToken(principal);

        List<Map<String, Object>> canvasCourses;
        try {
            canvasCourses = webClient.get()
                    .uri(uriBuilder -> {
                        return uriBuilder
                                .path("/api/v1/courses")
                                //TODO: enable filtering by teacher in production
                                //.queryParam("enrollment_type", "teacher")
                                .queryParam("per_page", "1000")
                                .queryParam("state[]", "active")
                                .build();
                    })
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            if (e instanceof WebClientResponseException ex) {
                if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new AccessTokenExpiredException();
                } else {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error fetching courses from Canvas API", e);
                }
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected error occurred while fetching Canvas courses", e);
            }
        }

        if (canvasCourses == null) {
            return new ArrayList<>();
        }

        List<CourseDetailsDto> newCourses = new ArrayList<>();
        for (Map<String, Object> canvasCourse : canvasCourses) {
            Long canvasId = Long.parseLong(canvasCourse.get("id").toString());
            if (!existsByCanvasId(canvasId)) {
                Course course = new Course();
                course.setName((String) canvasCourse.get("name"));
                course.setUuid((String) canvasCourse.get("uuid"));
                course.setCanvasCreatedAt(OffsetDateTime.parse((String) canvasCourse.get("created_at")));
                course.setCanvasId(canvasId);
                course.setOwner(user);

                // Parse dates
                String startAtStr = (String) canvasCourse.get("start_at");
                String endAtStr = (String) canvasCourse.get("end_at");
                if (startAtStr != null) {
                    course.setStartAt(LocalDateTime.parse(startAtStr.replace("Z", "")));
                }
                if (endAtStr != null) {
                    course.setEndAt(LocalDateTime.parse(endAtStr.replace("Z", "")));
                }

                newCourses.add(courseMapper.toCourseDetailsDto(course));
            }
        }

        return newCourses;
    }

    private boolean existsByCanvasId(Long canvasId) {
        return courseRepository.existsByCanvasId(canvasId);
    }

    @Override
    public CourseDetailsDto createCourseWithEnrollments(Principal principal, CourseCreateDto courseDto) {
        User user = userService.getUserFromAnyPrincipal(principal);
        String token = canvasTokenService.getValidAccessToken(principal);

        Course courseEntity = courseMapper.CourseCreateToEntity(courseDto, principal);
        importCourse(courseEntity);

        String courseId = courseEntity.getCanvasId().toString();
        List<Map<String, Object>> allEnrollments = new ArrayList<>();
        String url = "/api/v1/courses/" + courseId + "/enrollments?per_page=100";

        while (url != null) {
            ResponseEntity<String> responseEntity = webClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (responseEntity == null) {
                break;
            }

            String body = responseEntity.getBody();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> enrollments = objectMapper.readValue(body,
                        new TypeReference<List<Map<String, Object>>>() {});
                allEnrollments.addAll(enrollments);
            } catch (Exception e) {
                throw new BaseRuntimeException("Error parsing enrollments", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<String> linkHeaders = responseEntity.getHeaders().get("Link");
            url = (linkHeaders != null && !linkHeaders.isEmpty()) ? extractNextUrl(linkHeaders.get(0)) : null;
        }

        for (Map<String, Object> enrollment : allEnrollments) {
            Long canvasUserId = Long.parseLong(enrollment.get("user_id").toString());
            enrollmentService.enrollUser(courseEntity, canvasUserId);
        }

        logger.info("[{}] Course created with enrollments: {}", CourseService.class.getSimpleName(), courseEntity);
        return courseMapper.toCourseDetailsDto(courseEntity);
    }

    /**
     * Extracts the next URL from the Canvas Link header.
     *
     * @param linkHeader the Link header containing pagination links.
     * @return the next URL if available, otherwise null.
     */
    private String extractNextUrl(String linkHeader) {
        String[] parts = linkHeader.split(",");
        for (String part : parts) {
            if (part.contains("rel=\"next\"")) {
                int start = part.indexOf("<") + 1;
                int end = part.indexOf(">");
                if (start > 0 && end > start) {
                    return part.substring(start, end);
                }
            }
        }
        return null;
    }

    @Override
    public List<CourseDetailsDto> getCoursesByOwner(Principal principal) {
        User owner = userService.getUserFromAnyPrincipal(principal);
        return courseMapper.toCourseDetailsDtoList(courseRepository.findByOwner(owner));
    }

    @Override
    public List<CourseDetailsDto> getCoursesByEnrollment(Principal principal) {
        User user = userService.getUserFromAnyPrincipal(principal);
        return courseMapper.toCourseDetailsDtoList(courseRepository.findByEnrollmentsCanvasUserId(user.getCanvasUserId()));
    }

    private void importCourse(Course course) {
        if (course == null) {
            throw new BaseRuntimeException("Course cannot be null", HttpStatus.BAD_REQUEST);
        }
        try {
            courseRepository.save(course);
        } catch (Exception e) {
            throw new BaseRuntimeException("An unexpected error occurred while saving the course",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(Course.class, courseId));
    }
}
