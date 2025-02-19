package com.ehb.connected.domain.impl.assignments.mappers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

    public List<AssignmentDetailsDto> toAssignmentDetailsDtoList(List<Assignment> assignments) {
        List<AssignmentDetailsDto> assignmentDtos = new ArrayList<>();
        for (Assignment assignment : assignments) {
            assignmentDtos.add(toAssignmentDetailsDto(assignment));
        }
        return assignmentDtos;
    }

    public AssignmentDetailsDto fromCanvasMapToAssignmentDetailsDto(Map<String, Object> canvasAssignment, Long courseId) {
        AssignmentDetailsDto dto = new AssignmentDetailsDto();
        dto.setId(null);
        dto.setName(canvasAssignment.get("name").toString());
        dto.setDescription(canvasAssignment.get("description") != null ? canvasAssignment.get("description").toString() : "");
        dto.setDefaultTeamSize(null);
        dto.setCanvasAssignmentId(Long.parseLong(canvasAssignment.get("id").toString()));
        dto.setCourseId(courseId);
        return dto;
    }
}
