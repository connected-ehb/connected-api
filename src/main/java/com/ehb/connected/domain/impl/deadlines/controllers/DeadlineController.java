package com.ehb.connected.domain.impl.deadlines.controllers;


import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deadlines")
public class DeadlineController {
    @Autowired
    private DeadlineService deadlineService;

    @GetMapping()
    public List<Deadline> getAllDeadlines(){
        return deadlineService.getAllDeadlines();
    }

    @GetMapping("/{id}")
    public Deadline getDeadlineById(@PathVariable Long id) {
        return deadlineService.getDeadlineById(id);
    }

    @PostMapping()
    public Deadline createDeadline(@RequestBody Deadline deadline) {
        return deadlineService.createDeadline(deadline);
    }

    @PatchMapping("/{id}")
    public Deadline updateDeadline(@PathVariable Long id, @RequestBody Deadline deadline) {
        return deadlineService.updateDeadline(deadline);
    }

    @DeleteMapping("/{id}")
    public void deleteDeadline(@PathVariable Long id) {
        deadlineService.deleteDeadline(id);
    }


}
