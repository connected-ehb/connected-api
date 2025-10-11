package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.mappers.DeadlineMapper;
import com.ehb.connected.domain.impl.deadlines.repositories.DeadlineRepository;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadlineServiceImplTest {

    @Mock private DeadlineRepository deadlineRepository;
    @Mock private DeadlineMapper deadlineMapper;
    @Mock private AssignmentService assignmentService;

    @InjectMocks private DeadlineServiceImpl deadlineService;

    private Assignment assignment;
    private Deadline deadline;
    private DeadlineDetailsDto detailsDto;

    @BeforeEach
    void setUp() {
        assignment = new Assignment();
        assignment.setId(3L);

        deadline = new Deadline(5L, "Submit", LocalDateTime.now().plusDays(2), "description",
                DeadlineRestriction.APPLICATION_SUBMISSION, "Europe/Brussels", assignment);

        detailsDto = new DeadlineDetailsDto();
        detailsDto.setId(5L);

        lenient().when(deadlineMapper.toDeadlineDetailsDto(deadline)).thenReturn(detailsDto);
    }

    @Test
    void getAllDeadlinesByAssignmentFiltersUpcoming() {
        when(deadlineRepository.findUpcomingDeadlines(any(), any())).thenReturn(List.of(deadline));
        when(deadlineMapper.toDeadlineDetailsDtoList(List.of(deadline))).thenReturn(List.of(detailsDto));

        List<DeadlineDetailsDto> result = deadlineService.getAllDeadlinesByAssignmentId(3L);

        assertThat(result).containsExactly(detailsDto);
        verify(deadlineRepository).findUpcomingDeadlines(any(), any());
    }

    @Test
    void getDeadlineByIdReturnsMappedDto() {
        when(deadlineRepository.findById(5L)).thenReturn(Optional.of(deadline));
        when(deadlineMapper.toDeadlineDetailsDto(deadline)).thenReturn(detailsDto);

        assertThat(deadlineService.getDeadlineById(5L)).isEqualTo(detailsDto);
    }

    @Test
    void getDeadlineByIdThrowsWhenMissing() {
        when(deadlineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deadlineService.getDeadlineById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createDeadlineConvertsTimezoneAndSaves() {
        DeadlineCreateDto dto = new DeadlineCreateDto();
        dto.setTitle("Submit");
        dto.setDescription("Desc");
        dto.setRestriction(DeadlineRestriction.PROJECT_CREATION);
        dto.setDueDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        dto.setTimeZone("Europe/Brussels");

        when(assignmentService.getAssignmentById(3L)).thenReturn(assignment);
        when(deadlineMapper.toEntity(dto)).thenReturn(new Deadline());
        when(deadlineMapper.toDeadlineDetailsDto(any())).thenReturn(detailsDto);
        when(deadlineRepository.save(any(Deadline.class))).thenAnswer(invocation -> {
            Deadline saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        DeadlineDetailsDto result = deadlineService.createDeadline(3L, dto);

        assertThat(result).isEqualTo(detailsDto);
        ArgumentCaptor<Deadline> captor = ArgumentCaptor.forClass(Deadline.class);
        verify(deadlineRepository).save(captor.capture());
        assertThat(captor.getValue().getAssignment()).isSameAs(assignment);
        assertThat(captor.getValue().getDueDate()).isEqualTo(dto.getDueDate().atZone(java.time.ZoneId.of("Europe/Brussels"))
                .withZoneSameInstant(java.time.ZoneId.of("UTC")).toLocalDateTime());
    }

    @Test
    void createDeadlineWrapsUnexpectedErrors() {
        DeadlineCreateDto dto = new DeadlineCreateDto();
        dto.setDueDate(LocalDateTime.now());
        dto.setTimeZone("UTC");
        when(assignmentService.getAssignmentById(3L)).thenThrow(new RuntimeException("Boom"));

        assertThatThrownBy(() -> deadlineService.createDeadline(3L, dto))
                .isInstanceOf(BaseRuntimeException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void updateDeadlinePersistsChanges() {
        DeadlineUpdateDto dto = new DeadlineUpdateDto();
        dto.setTitle("Updated");
        dto.setDescription("New description");
        dto.setDueDate(LocalDateTime.of(2025, 2, 2, 12, 0));
        dto.setRestriction(DeadlineRestriction.PROJECT_CREATION);

        when(deadlineRepository.findById(5L)).thenReturn(Optional.of(deadline));
        when(deadlineMapper.toEntity(dto)).thenReturn(new Deadline());
        when(deadlineRepository.save(any(Deadline.class))).thenReturn(deadline);
        when(deadlineMapper.toDeadlineDetailsDto(deadline)).thenReturn(detailsDto);

        DeadlineDetailsDto result = deadlineService.updateDeadline(5L, dto);

        assertThat(result).isEqualTo(detailsDto);
        verify(deadlineRepository).save(any(Deadline.class));
    }

    @Test
    void deleteDeadlineDelegatesRepository() {
        deadlineService.deleteDeadline(9L);
        verify(deadlineRepository).deleteById(9L);
    }
}
