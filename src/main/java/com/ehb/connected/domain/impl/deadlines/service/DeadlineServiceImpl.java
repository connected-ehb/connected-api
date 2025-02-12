package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.repositories.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DeadlineServiceImpl  implements DeadlineService {

    private final DeadlineRepository deadlineRepository;

    @Override
    public List<Deadline> getAllDeadlinesByAssignmentId(Long assignmentId) {
        return deadlineRepository.findAllByAssignmentId(assignmentId);
    }

    @Override
    public Deadline getDeadlineByAssignmentIdAndRestrictions(Long assignmentId, DeadlineRestriction restriction) {
        return deadlineRepository.findTopByAssignmentIdAndRestrictionOrderByDateTimeDesc(assignmentId, restriction);
    }

    @Override
    public Deadline getDeadlineById(Long id) {
        return deadlineRepository.findById(id).orElse(null);
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
