package com.ehb.connected.domain.impl.assignments.mappers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentMapper {

    public Assignment AssignmentCreateToEntity(AssignmentCreateDto assignmentCreateDto) {
        Assignment assignment = new Assignment();
        assignment.setName(assignmentCreateDto.getName());
        assignment.setDescription(assignmentCreateDto.getDescription());
        assignment.setDefaultTeamSize(assignmentCreateDto.getDefaultTeamSize());
        assignment.setCanvasAssignmentId(assignmentCreateDto.getCanvasAssignmentId());
        return assignment;
    }

    public AssignmentDetailsDto toAssignmentDetailsDto(Assignment assignment) {
        AssignmentDetailsDto assignmentDetailsDto = new AssignmentDetailsDto();
        assignmentDetailsDto.setId(assignment.getId());
        assignmentDetailsDto.setName(assignment.getName());
        assignmentDetailsDto.setDescription(assignment.getDescription());
        assignmentDetailsDto.setDefaultTeamSize(assignment.getDefaultTeamSize());
        assignmentDetailsDto.setCanvasAssignmentId(assignment.getCanvasAssignmentId());
        assignmentDetailsDto.setCourseId(assignment.getCourse().getId());
        return assignmentDetailsDto;
    }
}
