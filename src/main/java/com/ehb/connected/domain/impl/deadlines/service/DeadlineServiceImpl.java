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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl implements DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final DeadlineMapper deadlineMapper;

    private final AssignmentService assignmentService;

    private final Logger logger = LoggerFactory.getLogger(DeadlineServiceImpl.class);

    @Override
    public List<DeadlineDetailsDto> getAllDeadlinesByAssignmentId(Long assignmentId) {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneId.of("UTC"));
        return deadlineMapper.toDeadlineDetailsDtoList(deadlineRepository.findUpcomingDeadlines(assignmentId, nowUtc));
    }

    @Override
    public Deadline getDeadlineByAssignmentAndRestrictions(Assignment assignment, DeadlineRestriction restriction) {
        return deadlineRepository.findTopByAssignmentIdAndRestrictionOrderByDueDateDesc(assignment.getId(), restriction);
    }

    @Override
    public DeadlineDetailsDto getDeadlineById(Long deadlineId) {
        return deadlineMapper.toDeadlineDetailsDto(deadlineRepository.findById(deadlineId).orElseThrow(
                () -> new EntityNotFoundException(Deadline.class, deadlineId)
        ));
    }

    @Override
    public DeadlineDetailsDto createDeadline(Long assignmentId, DeadlineCreateDto deadlineDto) {
        try {
            // Convert the local time to UTC
            LocalDateTime deadlineUtc = convertToUTC(deadlineDto.getDueDate(), deadlineDto.getTimeZone());

            // Log the received data
            logger.info("Creating a new deadline with title: '{}' and datetime: '{}', timezone: '{}'",
                    deadlineDto.getTitle(), deadlineDto.getDueDate(), deadlineDto.getTimeZone());

            Assignment assignment = assignmentService.getAssignmentById(assignmentId);

            // Map DTO to entity
            Deadline deadline = deadlineMapper.toEntity(deadlineDto);
            deadline.setDueDate(deadlineUtc);
            deadline.setAssignment(assignment);

            // Save and return the created deadline
            Deadline savedDeadline = deadlineRepository.save(deadline);

            logger.info("Deadline successfully created with ID: {}", savedDeadline.getId());

            return deadlineMapper.toDeadlineDetailsDto(savedDeadline);

        } catch (Exception e) {
            throw new BaseRuntimeException("Failed to create deadline", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to convert time to UTC
    private LocalDateTime convertToUTC(LocalDateTime localDateTime, String timezone) {
        ZoneId userZone = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = localDateTime.atZone(userZone);
        return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
    }

    @Override
    public DeadlineDetailsDto updateDeadline(Long deadlineId, DeadlineUpdateDto deadlineDto) {
        Deadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new EntityNotFoundException(Deadline.class, deadlineId));
        deadline.setTitle(deadlineDto.getTitle());
        deadline.setDescription(deadline.getDescription());
        deadline.setDueDate(convertToUTC(deadlineDto.getDueDate(), deadline.getTimeZone()));
        deadline.setRestriction(deadlineDto.getRestriction());

        return deadlineMapper.toDeadlineDetailsDto(deadlineRepository.save(deadlineMapper.toEntity(deadlineDto)));
    }

    @Override
    public void deleteDeadline(Long deadlineId) {
        deadlineRepository.deleteById(deadlineId);
    }
}
