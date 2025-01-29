package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.repositories.DeadlineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class DeadlineServiceImpl  implements DeadlineService{
    @Autowired
    private DeadlineRepository deadlineRepository;
    @Override
    public List<Deadline> getAllDeadlines() {
        return deadlineRepository.findAll();
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
