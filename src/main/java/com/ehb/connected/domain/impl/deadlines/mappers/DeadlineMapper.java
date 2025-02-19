package com.ehb.connected.domain.impl.deadlines.mappers;

import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineMapper {

    private final AssignmentMapper assignmentMapper;

    public DeadlineDetailsDto toDeadlineDetailsDto(Deadline deadline) {
        if (deadline == null) {
            return null;
        }
        DeadlineDetailsDto deadlineDetailsDto = new DeadlineDetailsDto();
        deadlineDetailsDto.setId(deadline.getId());
        deadlineDetailsDto.setTitle(deadline.getTitle());
        deadlineDetailsDto.setDescription(deadline.getDescription());
        deadlineDetailsDto.setDueDate(deadline.getDueDate());
        deadlineDetailsDto.setRestriction(deadline.getRestriction());
        deadlineDetailsDto.setAssignment(assignmentMapper.toAssignmentDetailsDto(deadline.getAssignment()));

        return deadlineDetailsDto;
    }

    public List<DeadlineDetailsDto> toDeadlineDetailsDtoList(List<Deadline> deadlines) {
       return deadlines.stream().map(this::toDeadlineDetailsDto).toList();
    }

    public Deadline toEntity(DeadlineCreateDto deadlineCreateDto) {
        Deadline deadline = new Deadline();
        deadline.setTitle(deadlineCreateDto.getTitle());
        deadline.setDescription(deadlineCreateDto.getDescription());
        deadline.setDueDate(deadlineCreateDto.getDueDate());
        deadline.setRestriction(deadlineCreateDto.getRestriction());
        deadline.setTimeZone(deadlineCreateDto.getTimeZone());
        return deadline;
    }

    public Deadline toEntity(DeadlineUpdateDto deadlineUpdateDto) {
        Deadline deadline = new Deadline();
        deadline.setTitle(deadlineUpdateDto.getTitle());
        deadline.setDescription(deadlineUpdateDto.getDescription());
        deadline.setDueDate(deadlineUpdateDto.getDueDate());
        deadline.setRestriction(deadlineUpdateDto.getRestriction());

        return deadline;
    }
}
