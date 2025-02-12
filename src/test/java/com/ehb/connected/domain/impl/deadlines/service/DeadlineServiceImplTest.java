package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.repositories.DeadlineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeadlineServiceImplTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @InjectMocks
    private DeadlineServiceImpl deadlineServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllDeadlines() {
        // TODO
    }

    @Test
    public void testGetDeadlineById() {
        Deadline deadline = new Deadline();
        deadline.setId(1L);
        deadline.setTitle("Deadline 1");
        deadline.setDescription("Description 1");

        when(deadlineRepository.findById(1L)).thenReturn(Optional.of(deadline));

        Deadline result = deadlineServiceImpl.getDeadlineById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Deadline 1", result.getTitle());
        assertEquals("Description 1", result.getDescription());
        verify(deadlineRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateDeadline() {
        Deadline deadline = new Deadline();
        deadline.setTitle("New Deadline");

        when(deadlineRepository.save(deadline)).thenReturn(deadline);

        Deadline result = deadlineServiceImpl.createDeadline(deadline);
        assertNotNull(result);
        assertEquals("New Deadline", result.getTitle());
        verify(deadlineRepository, times(1)).save(deadline);
    }

    @Test
    public void testUpdateDeadline() {
        Deadline deadline = new Deadline();
        deadline.setId(1L);
        deadline.setTitle("Updated Deadline");

        when(deadlineRepository.save(deadline)).thenReturn(deadline);

        Deadline result = deadlineServiceImpl.updateDeadline(deadline);
        assertNotNull(result);
        assertEquals("Updated Deadline", result.getTitle());
        verify(deadlineRepository, times(1)).save(deadline);
    }

    @Test
    public void testDeleteDeadline() {
        Long deadlineId = 1L;

        // Act
        deadlineServiceImpl.deleteDeadline(deadlineId);

        // Assert
        verify(deadlineRepository, times(1)).deleteById(deadlineId);
    }
}