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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl  implements DeadlineService {

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
    public DeadlineDetailsDto getDeadlineByAssignmentIdAndRestrictions(Long assignmentId, DeadlineRestriction restriction) {
        return deadlineMapper.toDeadlineDetailsDto(deadlineRepository.findTopByAssignmentIdAndRestrictionOrderByDateTimeDesc(assignmentId, restriction));
    }

    @Override
    public DeadlineDetailsDto getDeadlineById(Long deadlineId) {
        return deadlineMapper.toDeadlineDetailsDto(deadlineRepository.findById(deadlineId).orElseThrow(
                () -> new EntityNotFoundException(Deadline.class, deadlineId)
        ));
    }

    @Override
    public Deadline createDeadline(Deadline deadline) {
        return deadlineRepository.save(deadline);
    }

    @Override
    public Deadline updateDeadline(Deadline deadline) {
        return deadlineRepository.save(deadline);
    }

    @Override
    public void deleteDeadline(Long id) {
        deadlineRepository.deleteById(id);
    }
}
