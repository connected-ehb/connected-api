package com.ehb.connected.domain.impl.deadlines.mappers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadlineMapperTest {

    @Mock private AssignmentMapper assignmentMapper;

    @InjectMocks private DeadlineMapper mapper;

    @Test
    void toDeadlineDetailsDtoMapsAssignmentAndFields() {
        Assignment assignment = new Assignment(); assignment.setId(1L);
        Deadline deadline = new Deadline();
        deadline.setId(5L);
        deadline.setTitle("Submission");
        deadline.setDescription("Upload files");
        deadline.setDueDate(LocalDateTime.now());
        deadline.setRestriction(DeadlineRestriction.APPLICATION_SUBMISSION);
        deadline.setTimeZone("Europe/Brussels");
        deadline.setAssignment(assignment);

        AssignmentDetailsDto assignmentDto = new AssignmentDetailsDto();
        assignmentDto.setId(1L);
        when(assignmentMapper.toAssignmentDetailsDto(assignment)).thenReturn(assignmentDto);

        DeadlineDetailsDto dto = mapper.toDeadlineDetailsDto(deadline);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getTitle()).isEqualTo("Submission");
        assertThat(dto.getDescription()).isEqualTo("Upload files");
        assertThat(dto.getDueDate()).isEqualTo(deadline.getDueDate());
        assertThat(dto.getRestriction()).isEqualTo(DeadlineRestriction.APPLICATION_SUBMISSION);
        assertThat(dto.getTimeZone()).isEqualTo("Europe/Brussels");
        assertThat(dto.getAssignment()).isSameAs(assignmentDto);
        verify(assignmentMapper).toAssignmentDetailsDto(assignment);
    }

    @Test
    void toDeadlineDetailsDtoListMapsCollection() {
        Deadline d1 = new Deadline(); d1.setAssignment(new Assignment());
        Deadline d2 = new Deadline(); d2.setAssignment(new Assignment());
        when(assignmentMapper.toAssignmentDetailsDto(d1.getAssignment())).thenReturn(new AssignmentDetailsDto());
        when(assignmentMapper.toAssignmentDetailsDto(d2.getAssignment())).thenReturn(new AssignmentDetailsDto());

        List<DeadlineDetailsDto> result = mapper.toDeadlineDetailsDtoList(List.of(d1, d2));

        assertThat(result).hasSize(2);
    }

    @Test
    void toEntityFromCreateCopiesAllFields() {
        DeadlineCreateDto dto = new DeadlineCreateDto();
        dto.setTitle("Review");
        dto.setDescription("Teacher review");
        dto.setDueDate(LocalDateTime.now());
        dto.setRestriction(DeadlineRestriction.PROJECT_CREATION);
        dto.setTimeZone("UTC");

        Deadline deadline = mapper.toEntity(dto);

        assertThat(deadline.getTitle()).isEqualTo("Review");
        assertThat(deadline.getDescription()).isEqualTo("Teacher review");
        assertThat(deadline.getDueDate()).isEqualTo(dto.getDueDate());
        assertThat(deadline.getRestriction()).isEqualTo(DeadlineRestriction.PROJECT_CREATION);
        assertThat(deadline.getTimeZone()).isEqualTo("UTC");
    }

    @Test
    void toEntityFromUpdateCopiesFieldsExceptTimezone() {
        DeadlineUpdateDto dto = new DeadlineUpdateDto();
        dto.setTitle("Follow-up");
        dto.setDescription("Fix issues");
        dto.setDueDate(LocalDateTime.now());
        dto.setRestriction(DeadlineRestriction.PROJECT_CREATION);

        Deadline entity = mapper.toEntity(dto);

        assertThat(entity.getTitle()).isEqualTo("Follow-up");
        assertThat(entity.getDescription()).isEqualTo("Fix issues");
        assertThat(entity.getDueDate()).isEqualTo(dto.getDueDate());
        assertThat(entity.getRestriction()).isEqualTo(DeadlineRestriction.PROJECT_CREATION);
        assertThat(entity.getTimeZone()).isNull();
    }
}
