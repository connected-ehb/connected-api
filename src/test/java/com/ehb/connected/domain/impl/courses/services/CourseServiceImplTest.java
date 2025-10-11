package com.ehb.connected.domain.impl.courses.services;

import com.ehb.connected.domain.impl.auth.helpers.CanvasTokenService;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.mappers.CourseMapper;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.enrollments.services.EnrollmentService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import com.ehb.connected.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseMapper courseMapper;
    @Mock private UserServiceImpl userService;
    @Mock private EnrollmentService enrollmentService;
    @Mock private CanvasTokenService canvasTokenService;
    @Mock private WebClient webClient;

    @InjectMocks private CourseServiceImpl courseService;

    private Principal principal;
    private User user;

    @BeforeEach
    void setUp() {
        principal = () -> "teacher@ehb.be";
        user = new User();
        user.setId(10L);
        user.setCanvasUserId(555L);
        lenient().when(userService.getUserByPrincipal(principal)).thenReturn(user);
        lenient().when(canvasTokenService.getValidAccessToken(principal)).thenReturn("token");
    }

    @Test
    void getNewCoursesFromCanvasReturnsOnlyUnknownCourses() {
        Map<String, Object> canvasCourse = Map.of(
                "id", 123,
                "name", "Distributed Systems",
                "uuid", "uuid-1",
                "created_at", "2025-01-01T00:00:00Z",
                "start_at", "2025-01-10T09:00:00Z",
                "end_at", "2025-02-10T09:00:00Z"
        );
        mockCanvasCourseResponse(List.of(canvasCourse));
        when(courseRepository.existsByCanvasId(123L)).thenReturn(false);
        when(courseMapper.toCourseDetailsDto(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            CourseDetailsDto dto = new CourseDetailsDto();
            dto.setCanvasId(course.getCanvasId());
            dto.setName(course.getName());
            return dto;
        });

        List<CourseDetailsDto> result = courseService.getNewCoursesFromCanvas(principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Distributed Systems");
        verify(courseRepository).existsByCanvasId(123L);
    }

    @Test
    void getNewCoursesFromCanvasReturnsEmptyWhenCanvasRespondsWithNull() {
        mockCanvasCourseResponse(null);

        List<CourseDetailsDto> result = courseService.getNewCoursesFromCanvas(principal);

        assertThat(result).isEmpty();
    }

    @Test
    void getCoursesByOwnerReturnsMappedDtos() {
        Course course = new Course();
        List<Course> courses = List.of(course);
        CourseDetailsDto dto = new CourseDetailsDto();
        when(courseRepository.findByOwner(user)).thenReturn(courses);
        when(courseMapper.toCourseDetailsDtoList(courses)).thenReturn(List.of(dto));

        List<CourseDetailsDto> result = courseService.getCoursesByOwner(principal);

        assertThat(result).containsExactly(dto);
        verify(courseRepository).findByOwner(user);
    }

    @Test
    void getCoursesByEnrollmentUsesUserCanvasId() {
        Course course = new Course();
        List<Course> courses = List.of(course);
        CourseDetailsDto dto = new CourseDetailsDto();
        when(courseRepository.findByEnrollmentsCanvasUserId(555L)).thenReturn(courses);
        when(courseMapper.toCourseDetailsDtoList(courses)).thenReturn(List.of(dto));

        List<CourseDetailsDto> result = courseService.getCoursesByEnrollment(principal);

        assertThat(result).containsExactly(dto);
        verify(courseRepository).findByEnrollmentsCanvasUserId(555L);
    }

    @Test
    void getCourseByIdReturnsCourseWhenFound() {
        Course course = new Course();
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));

        Course result = courseService.getCourseById(5L);

        assertThat(result).isSameAs(course);
    }

    @Test
    void getCourseByIdThrowsWhenMissing() {
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteCourseByIdDelegatesRepository() {
        courseService.deleteCourseById(12L);

        verify(courseRepository).deleteById(12L);
    }

    private void mockCanvasCourseResponse(List<Map<String, Object>> payload) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())).thenReturn(uriSpec);
        when(uriSpec.header(any(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any()))
                .thenReturn(Mono.justOrEmpty(payload));
    }
}


