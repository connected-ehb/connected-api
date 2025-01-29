package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;

import java.util.List;

public interface DeadlineService {
    List<Deadline> getAllDeadlines();
    Deadline getDeadlineById(Long id);
    Deadline createDeadline(Deadline deadline);
    Deadline updateDeadline(Deadline deadline);
    void deleteDeadline(Long id);

}
