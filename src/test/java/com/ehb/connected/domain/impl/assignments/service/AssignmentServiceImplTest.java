package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.services.CourseService;
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
class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private CourseService courseService;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private WebClient webClient;

    @InjectMocks private AssignmentServiceImpl assignmentService;

    private Principal principal;
    private Course course;

    @BeforeEach
    void setUp() {
        principal = () -> "teacher@example.com";
        course = new Course();
        course.setId(20L);
        course.setCanvasId(200L);
        lenient().when(courseService.getCourseById(20L)).thenReturn(course);
    }

    @Test
    void createAssignmentPersistsEntityWithCourse() {
        AssignmentCreateDto dto = new AssignmentCreateDto();
        dto.setCourseId(20L);
        Assignment assignment = new Assignment();
        AssignmentDetailsDto mapped = new AssignmentDetailsDto(); mapped.setId(5L);

        when(assignmentMapper.AssignmentCreateToEntity(dto)).thenReturn(assignment);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toAssignmentDetailsDto(assignment)).thenReturn(mapped);

        AssignmentDetailsDto result = assignmentService.createAssignment(dto);

        assertThat(assignment.getCourse()).isSameAs(course);
        assertThat(result).isSameAs(mapped);
        verify(assignmentRepository).save(assignment);
    }

    @Test
    void getAssignmentByIdReturnsAssignmentWhenPresent() {
        Assignment assignment = new Assignment();
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));

        assertThat(assignmentService.getAssignmentById(3L)).isSameAs(assignment);
    }

    @Test
    void getAssignmentByIdThrowsWhenMissing() {
        when(assignmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.getAssignmentById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAllAssignmentsByCourseReturnsMappedDtos() {
        List<Assignment> assignments = List.of(new Assignment());
        AssignmentDetailsDto dto = new AssignmentDetailsDto();
        when(assignmentRepository.findByCourseId(20L)).thenReturn(assignments);
        when(assignmentMapper.toAssignmentDetailsDtoList(assignments)).thenReturn(List.of(dto));

        List<AssignmentDetailsDto> result = assignmentService.getAllAssignmentsByCourse(20L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getNewAssignmentsFromCanvasReturnsNonExistingAssignments() {
        Map<String, Object> payload = Map.of(
                "id", 555,
                "name", "New Canvas Assignment",
                "description", "Canvas imported"
        );
        mockCanvasAssignmentsResponse(List.of(payload));
        when(assignmentRepository.existsByCanvasId(555L)).thenReturn(false);
        AssignmentDetailsDto mapped = new AssignmentDetailsDto(); mapped.setName("New Canvas Assignment");
        when(assignmentMapper.fromCanvasMapToAssignmentDetailsDto(payload, course.getId())).thenReturn(mapped);

        List<AssignmentDetailsDto> result = assignmentService.getNewAssignmentsFromCanvas(principal, 20L);

        assertThat(result).containsExactly(mapped);
        verify(assignmentRepository).existsByCanvasId(555L);
    }

    @Test
    void getNewAssignmentsFromCanvasReturnsEmptyWhenNoneFound() {
        mockCanvasAssignmentsResponse(null);

        List<AssignmentDetailsDto> result = assignmentService.getNewAssignmentsFromCanvas(principal, 20L);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAssignmentByIdRemovesEntity() {
        Assignment assignment = new Assignment(); assignment.setId(9L);
        when(assignmentRepository.findById(9L)).thenReturn(Optional.of(assignment));

        assignmentService.deleteAssignmentById(principal, 9L);

        verify(assignmentRepository).delete(assignment);
    }

    private void mockCanvasAssignmentsResponse(List<Map<String, Object>> payload) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        lenient().when(webClient.get()).thenReturn(uriSpec);
        lenient().when(uriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())).thenReturn(uriSpec);
        lenient().when(uriSpec.header(any(), any())).thenReturn(headersSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<List<Map<String, Object>>>>any()))
                .thenReturn(Mono.justOrEmpty(payload));
    }
}


