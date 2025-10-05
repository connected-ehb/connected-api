package com.ehb.connected.domain.impl.assignments.mappers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.courses.entities.Course;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentMapperTest {

    private final AssignmentMapper mapper = new AssignmentMapper();

    @Test
    void assignmentCreateToEntityCopiesFields() {
        AssignmentCreateDto dto = new AssignmentCreateDto();
        dto.setName("Capstone");
        dto.setDescription("Build something great");
        dto.setDefaultTeamSize(4);
        dto.setCanvasId(123L);

        Assignment assignment = mapper.AssignmentCreateToEntity(dto);

        assertThat(assignment.getName()).isEqualTo("Capstone");
        assertThat(assignment.getDescription()).isEqualTo("Build something great");
        assertThat(assignment.getDefaultTeamSize()).isEqualTo(4);
        assertThat(assignment.getCanvasId()).isEqualTo(123L);
    }

    @Test
    void toAssignmentDetailsDtoMapsCourseAndBasics() {
        Course course = new Course();
        course.setId(9L);

        Assignment assignment = new Assignment();
        assignment.setId(5L);
        assignment.setName("AI Project");
        assignment.setDescription("Work with ML models");
        assignment.setDefaultTeamSize(3);
        assignment.setCanvasId(789L);
        assignment.setCourse(course);

        AssignmentDetailsDto dto = mapper.toAssignmentDetailsDto(assignment);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("AI Project");
        assertThat(dto.getDescription()).isEqualTo("Work with ML models");
        assertThat(dto.getDefaultTeamSize()).isEqualTo(3);
        assertThat(dto.getCanvasId()).isEqualTo(789L);
        assertThat(dto.getCourseId()).isEqualTo(9L);
    }

    @Test
    void toAssignmentDetailsDtoListMapsAllAssignments() {
        Course course = new Course();
        course.setId(1L);

        Assignment a1 = new Assignment(); a1.setId(1L); a1.setCourse(course);
        Assignment a2 = new Assignment(); a2.setId(2L); a2.setCourse(course);

        List<AssignmentDetailsDto> dtos = mapper.toAssignmentDetailsDtoList(List.of(a1, a2));

        assertThat(dtos).extracting(AssignmentDetailsDto::getId).containsExactly(1L, 2L);
    }

    @Test
    void fromCanvasMapToAssignmentDetailsDtoBuildsDetails() {
        Map<String, Object> canvas = Map.of(
                "id", 55,
                "name", "Database Systems",
                "description", "Normalize all the things"
        );

        AssignmentDetailsDto dto = mapper.fromCanvasMapToAssignmentDetailsDto(canvas, 11L);

        assertThat(dto.getCanvasId()).isEqualTo(55L);
        assertThat(dto.getName()).isEqualTo("Database Systems");
        assertThat(dto.getDescription()).isEqualTo("Normalize all the things");
        assertThat(dto.getCourseId()).isEqualTo(11L);
        assertThat(dto.getId()).isNull();
        assertThat(dto.getDefaultTeamSize()).isNull();
    }
}
