package com.ehb.connected.domain.impl.courses.services;

import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.mappers.CourseMapper;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.enrollments.services.EnrollmentService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.Principal;
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

        // Retrieve Canvas courses using WebClient.
        final List<Map<String, Object>> canvasCourses = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses")
                        .queryParam("EnrollmentType", "student") // adjust as needed or pass as parameter
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        // Retrieve courses already imported by the user.
        final List<CourseDetailsDto> importedCourses = getCoursesByOwner(principal);
        final Set<Long> importedCanvasIds = importedCourses.stream()
                .map(CourseDetailsDto::getId)
                .collect(Collectors.toSet());

        // Filter out courses already imported and map the remaining ones to CourseDetailsDto.
        assert canvasCourses != null;
        return canvasCourses.stream()
                .filter(courseMap -> {
                    Long canvasCourseId = Long.parseLong(courseMap.get("id").toString());
                    return !importedCanvasIds.contains(canvasCourseId);
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
        importCourse(courseEntity);  // Persist the course (assumed to save via repository)

        // Retrieve enrollments from Canvas using the Canvas course ID.
        final String courseId = courseEntity.getCanvasCourseId().toString();
        final String enrollmentsResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses/{course_id}/enrollments")
                        .build(courseId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> enrollments = objectMapper.readValue(enrollmentsResponse, new TypeReference<List<Map<String, Object>>>() {});
            // Enroll each user based on the Canvas enrollment data.
            for (Map<String, Object> enrollment : enrollments) {
                Long canvasUserId = Long.parseLong(enrollment.get("user_id").toString());
                enrollmentService.enrollUser(courseEntity, canvasUserId);
            }
        } catch (Exception e) {
            throw new BaseRuntimeException("Error parsing enrollments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("[{}] Course created with enrollments: {}", CourseService.class.getSimpleName(), courseEntity);
        return courseMapper.toCourseDetailsDto(courseEntity);
    }

    @Override
    public List<Course> getCoursesByOwner(Principal principal) {
        return courseRepository.findByOwner(userService.getUserByEmail(principal.getName()));
    }

    @Override
    public List<Course> getCoursesByEnrollment(Principal principal) {
        return courseRepository.findByEnrollmentsCanvasUserId(userService.getUserByEmail(principal.getName()).getCanvasUserId());
    }



    @Override
    public void createCourse(Course course) {
        try {
            courseRepository.save(course);
        } catch (Exception e) {
            System.out.println("Error while creating course: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course does not exist"));
    }

    @Override
    public Course getCourseByCanvasCourseId(Long canvasCourseId) {
        return courseRepository.findByCanvasCourseId(canvasCourseId)
                .orElseThrow(() -> new EntityNotFoundException("Course does not exist"));
    }
}
