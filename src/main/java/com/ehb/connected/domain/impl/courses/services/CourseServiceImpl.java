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
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    private final CourseMapper courseMapper;

    private final UserServiceImpl userService;
    private final EnrollmentService enrollmentService;

    private final WebClient webClient;

    Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Override
    public List<CourseDetailsDto> getNewCoursesFromCanvas(Principal principal) {
        final User user = userService.getUserByEmail(principal.getName());
        final String token = user.getAccessToken();
        List<Map<String, Object>> canvasCourses;

        // Retrieve Canvas courses using WebClient.
        try {
            canvasCourses = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/courses")
                            .queryParam("EnrollmentType", "teacher") // adjust as needed or pass as parameter
                            .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            if (e instanceof WebClientResponseException ex) {
                if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new AccessTokenExpiredException();
                } else {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching courses from Canvas API", e);
                }
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred while fetching Canvas courses", e);
            }
        }

        // Retrieve courses already imported by the user.
        final List<CourseDetailsDto> importedCourses = getCoursesByOwner(principal);
        final Set<Long> importedCanvasIds = importedCourses.stream()
                .map(CourseDetailsDto::getCanvasId)
                .collect(Collectors.toSet());

        // Filter out courses already imported and map the remaining ones to CourseDetailsDto.
        assert canvasCourses != null;
        return canvasCourses.stream()
                .filter(courseMap -> {
                    Long canvasId = Long.parseLong(courseMap.get("id").toString());
                    return !importedCanvasIds.contains(canvasId);
                })
                .map(courseMapper::fromCanvasMapToCourseDetailsDto)
                .toList();
    }

    @Override
    public CourseDetailsDto createCourseWithEnrollments(Principal principal, CourseCreateDto courseDto) {
        final User user = userService.getUserByEmail(principal.getName());
        final String token = user.getAccessToken();

        // Create and persist the new course.
        final Course courseEntity = courseMapper.CourseCreateToEntity(courseDto, principal);
        importCourse(courseEntity);  // Persist the course

        final String courseId = courseEntity.getCanvasId().toString();
        List<Map<String, Object>> allEnrollments = new ArrayList<>();

        // Start with the first page; set per_page to a high number (max 100 is typical).
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
                List<Map<String, Object>> enrollments = objectMapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {});
                allEnrollments.addAll(enrollments);
            } catch (Exception e) {
                throw new BaseRuntimeException("Error parsing enrollments", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Look for a "next" link in the Link header
            List<String> linkHeaders = responseEntity.getHeaders().get("Link");
            if (linkHeaders != null && !linkHeaders.isEmpty()) {
                String linkHeader = linkHeaders.get(0);
                url = extractNextUrl(linkHeader);
            } else {
                url = null;
            }
        }

        // Process all retrieved enrollments.
        for (Map<String, Object> enrollment : allEnrollments) {
            Long canvasUserId = Long.parseLong(enrollment.get("user_id").toString());
            enrollmentService.enrollUser(courseEntity, canvasUserId);
        }

        logger.info("[{}] Course created with enrollments: {}", CourseService.class.getSimpleName(), courseEntity);
        return courseMapper.toCourseDetailsDto(courseEntity);
    }

    /**
     * Helper method to extract the next URL from the Canvas Link header.
     * Example Link header:
     * <https://canvas.instructure.com/api/v1/courses/123/enrollments?page=2&per_page=100>; rel="next", <...>; rel="last"
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
        return courseMapper.toCourseDetailsDtoList(courseRepository.findByOwner(userService.getUserByEmail(principal.getName())));
    }

    @Override
    public List<CourseDetailsDto> getCoursesByEnrollment(Principal principal) {
        List<CourseDetailsDto> enrolledCourses = courseMapper.toCourseDetailsDtoList(courseRepository.findByEnrollmentsCanvasUserId(userService.getUserByEmail(principal.getName()).getCanvasUserId()));
        return enrolledCourses;
    }

    private void importCourse(Course course) {
        if (course == null) {
            throw new BaseRuntimeException("Course cannot be null", HttpStatus.BAD_REQUEST);
        }
        try {
            courseRepository.save(course);
        } catch (Exception e) {
            throw new BaseRuntimeException("An unexpected error occurred while saving the course", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(Course.class, courseId));
    }
}
