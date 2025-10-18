package com.ehb.connected.domain.impl.courses.services;

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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserServiceImpl userService;
    private final EnrollmentService enrollmentService;
    private final WebClient webClient;

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Override
    public List<CourseDetailsDto> getNewCoursesFromCanvas(Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);

        List<Map<String, Object>> canvasCourses;
        try {
            canvasCourses = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/courses")
                            .queryParam("per_page", "1000")
                            .queryParam("state[]", "active")
                            .build())
                    .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction
                            .authentication(authentication))
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

        Course courseEntity = courseMapper.CourseCreateToEntity(courseDto, principal);
        importCourse(courseEntity);

        final List<Long> userIds = fetchAllCanvasUserIdsForCourse(courseEntity.getCanvasId());

        for (Long canvasUserId : new HashSet<>(userIds)) {
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
        User owner = userService.getUserByPrincipal(principal);
        return courseMapper.toCourseDetailsDtoList(courseRepository.findByOwner(owner));
    }

    @Override
    public List<CourseDetailsDto> getCoursesByEnrollment(Principal principal) {
        User user = userService.getUserByPrincipal(principal);
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

    @Override
    public void deleteCourseById(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    @Override
    public CourseDetailsDto refreshEnrollments(Principal principal, Long courseId) {
        final User requester = userService.getUserByPrincipal(principal);
        final Course course = getCourseById(courseId);

        if (course.getCanvasId() == null) {
            throw new BaseRuntimeException("Course has no Canvas ID; cannot refresh enrollments.", HttpStatus.BAD_REQUEST);
        }

        final List<Long> canvasUserIds = fetchAllCanvasUserIdsForCourse(course.getCanvasId());

        if (canvasUserIds.isEmpty()) {
            long current = enrollmentService.countByCourseId(course.getId());
            if (current > 0) {
                throw new BaseRuntimeException(
                        "Canvas returned 0 enrollments. Refusing to clear existing enrollments.",
                        HttpStatus.CONFLICT
                );
            }
        }

        enrollmentService.replaceCourseEnrollments(course, canvasUserIds);

        logger.info("[{}] Enrollments refreshed for Course ID: {} ({} users) by User ID: {}",
                CourseService.class.getSimpleName(), courseId, canvasUserIds.size(), requester.getId());

        return courseMapper.toCourseDetailsDto(course);
    }

    private List<Long> fetchAllCanvasUserIdsForCourse(Long canvasCourseId) {
        final List<Long> allUserIds = new ArrayList<>();
        String url = "/api/v1/courses/" + canvasCourseId + "/enrollments?per_page=100";

        while (url != null) {
            ResponseEntity<String> responseEntity;
            try {
                responseEntity = webClient.get()
                        .uri(url)
                        .retrieve()
                        .toEntity(String.class)
                        .block();
            } catch (Exception e) {
                if (e instanceof WebClientResponseException ex && ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new AccessTokenExpiredException();
                }
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error fetching enrollments from Canvas API", e);
            }
            if (responseEntity == null) break;

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> enrollments = objectMapper.readValue(
                        responseEntity.getBody(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> enrollment : enrollments) {
                    Object userIdObj = enrollment.get("user_id");
                    if (userIdObj != null) {
                        allUserIds.add(Long.parseLong(userIdObj.toString()));
                    }
                }
            } catch (Exception e) {
                throw new BaseRuntimeException("Error parsing enrollments", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<String> linkHeaders = responseEntity.getHeaders().get("Link");
            url = (linkHeaders != null && !linkHeaders.isEmpty()) ? extractNextUrl(linkHeaders.get(0)) : null;
        }
        return allUserIds;
    }
}
