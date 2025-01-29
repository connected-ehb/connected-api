package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import java.util.List;

public interface AssignmentService {
    List<Assignment> getAllAssignments();
    Assignment getAssignmentById(Long id);
    Assignment createAssignment(Assignment assignment);
    Assignment updateAssignment(Assignment assignment);
    void deleteAssignment(Long id);

}
