package com.ehb.connected.domain.impl.courses.mappers;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseMapperTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private AssignmentMapper assignmentMapper;

    @InjectMocks
    private CourseMapper courseMapper;

    private Principal principal;
    private User owner;

    @BeforeEach
    void setUp() {
        principal = () -> "teacher@example.com";
        owner = new User();
        owner.setId(7L);
    }

    @Test
    void courseCreateToEntityCopiesFieldsAndSetsOwner() {
        when(userService.getUserByPrincipal(principal)).thenReturn(owner);

        CourseCreateDto dto = new CourseCreateDto();
        dto.setName("Software Engineering");
        dto.setUuid("uuid-123");
        dto.setCanvasCreatedAt(OffsetDateTime.now());
        dto.setStartAt(LocalDateTime.now());
        dto.setEndAt(LocalDateTime.now().plusDays(30));
        dto.setCanvasId(55L);

        Course course = courseMapper.CourseCreateToEntity(dto, principal);

        assertThat(course.getName()).isEqualTo("Software Engineering");
        assertThat(course.getUuid()).isEqualTo("uuid-123");
        assertThat(course.getCanvasCreatedAt()).isEqualTo(dto.getCanvasCreatedAt());
        assertThat(course.getStartAt()).isEqualTo(dto.getStartAt());
        assertThat(course.getEndAt()).isEqualTo(dto.getEndAt());
        assertThat(course.getAssignments()).isEmpty();
        assertThat(course.getOwner()).isSameAs(owner);
        assertThat(course.getCanvasId()).isEqualTo(55L);
    }

    @Test
    void toCourseDetailsDtoMapsNestedAssignments() {
        Course course = new Course();
        course.setId(10L);
        course.setName("AI");
        course.setUuid("uuid");
        course.setCanvasCreatedAt(OffsetDateTime.now());
        course.setStartAt(LocalDateTime.now());
        course.setEndAt(LocalDateTime.now().plusWeeks(1));
        course.setCanvasId(999L);
        course.setOwner(owner);
        course.setAssignments(List.of(new Assignment()));

        var assignmentDto = new com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto();
        assignmentDto.setId(3L);
        when(assignmentMapper.toAssignmentDetailsDtoList(any())).thenReturn(List.of(assignmentDto));

        CourseDetailsDto dto = courseMapper.toCourseDetailsDto(course);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("AI");
        assertThat(dto.getUuid()).isEqualTo("uuid");
        assertThat(dto.getCanvasCreatedAt()).isEqualTo(course.getCanvasCreatedAt());
        assertThat(dto.getStartAt()).isEqualTo(course.getStartAt());
        assertThat(dto.getEndAt()).isEqualTo(course.getEndAt());
        assertThat(dto.getOwnerId()).isEqualTo(owner.getId());
        assertThat(dto.getCanvasId()).isEqualTo(999L);
        assertThat(dto.getAssignments()).containsExactly(assignmentDto);
    }

    @Test
    void toCourseDetailsDtoListMapsEachCourse() {
        Course c1 = new Course(); c1.setId(1L); c1.setOwner(owner);
        Course c2 = new Course(); c2.setId(2L); c2.setOwner(owner);

        CourseDetailsDto dto1 = new CourseDetailsDto(); dto1.setId(1L);
        CourseDetailsDto dto2 = new CourseDetailsDto(); dto2.setId(2L);
        when(assignmentMapper.toAssignmentDetailsDtoList(any())).thenReturn(List.of());

        List<CourseDetailsDto> dtos = courseMapper.toCourseDetailsDtoList(List.of(c1, c2));

        assertThat(dtos).extracting(CourseDetailsDto::getId).containsExactly(1L, 2L);
    }

    @Test
    void fromCanvasMapToCourseDetailsDtoParsesDatesWhenPresent() {
        Map<String, Object> canvas = Map.of(
                "id", 77,
                "name", "Design Patterns",
                "start_at", "2025-01-10T09:00:00",
                "end_at", "2025-03-01T17:00:00"
        );

        CourseDetailsDto dto = courseMapper.fromCanvasMapToCourseDetailsDto(canvas);

        assertThat(dto.getCanvasId()).isEqualTo(77L);
        assertThat(dto.getName()).isEqualTo("Design Patterns");
        assertThat(dto.getStartAt()).isEqualTo(LocalDateTime.parse("2025-01-10T09:00:00"));
        assertThat(dto.getEndAt()).isEqualTo(LocalDateTime.parse("2025-03-01T17:00:00"));
    }

    @Test
    void fromCanvasMapToCourseDetailsDtoHandlesMissingDates() {
        java.util.Map<String, Object> canvas = new java.util.HashMap<>();
        canvas.put("id", 88);
        canvas.put("name", "DevOps");
        canvas.put("start_at", null);

        CourseDetailsDto dto = courseMapper.fromCanvasMapToCourseDetailsDto(canvas);

        assertThat(dto.getCanvasId()).isEqualTo(88L);
        assertThat(dto.getName()).isEqualTo("DevOps");
        assertThat(dto.getStartAt()).isNull();
        assertThat(dto.getEndAt()).isNull();
    }
}


