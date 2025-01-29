package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssignmentServiceImplTest {
    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentServiceImpl assignmentServiceImpl;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllAssignments(){
        Assignment assignment1 = new Assignment();
        Assignment assignment2 = new Assignment();
        List<Assignment> assignments = Arrays.asList(assignment1, assignment2);

        when(assignmentRepository.findAll()).thenReturn(assignments);

        List<Assignment> result = assignmentServiceImpl.getAllAssignments();
        assertEquals(2, result.size());
        verify(assignmentRepository,times(1)).findAll();


    }

    @Test
    public void testGetAssignmentById() {
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Assignment 1");
        assignment.setDescription("Description 1");

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));

        Assignment result = assignmentServiceImpl.getAssignmentById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Assignment 1", result.getTitle());
        assertEquals("Description 1", result.getDescription());
        verify(assignmentRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateAssignment() {
        Assignment assignment = new Assignment();
        assignment.setTitle("New Assignment");

        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        Assignment result = assignmentServiceImpl.createAssignment(assignment);
        assertNotNull(result);
        assertEquals("New Assignment", result.getTitle());
        verify(assignmentRepository, times(1)).save(assignment);
    }

    @Test
    public void testUpdateAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Updated Assignment");

        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        Assignment result = assignmentServiceImpl.updateAssignment(assignment);
        assertNotNull(result);
        assertEquals("Updated Assignment", result.getTitle());
        verify(assignmentRepository, times(1)).save(assignment);
    }

    @Test
    public void testDeleteAssignment() {
        Long assignmentId = 1L;

        // Act
        assignmentServiceImpl.deleteAssignment(assignmentId);

        // Assert
        verify(assignmentRepository, times(1)).deleteById(assignmentId);
    }


}
