package com.ehb.connected.domain.impl.deadlines.controllers;

import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeadlineController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeadlineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeadlineService deadlineService;

    @Test
    void getDeadlinesByAssignmentReturnsList() throws Exception {
        DeadlineDetailsDto dto = new DeadlineDetailsDto();
        dto.setId(4L);
        when(deadlineService.getAllDeadlinesByAssignmentId(11L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/deadlines/assignment/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4L));

        verify(deadlineService).getAllDeadlinesByAssignmentId(11L);
    }

    @Test
    void getDeadlineByIdReturnsDetails() throws Exception {
        DeadlineDetailsDto dto = new DeadlineDetailsDto();
        dto.setId(5L);
        when(deadlineService.getDeadlineById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/deadlines/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));

        verify(deadlineService).getDeadlineById(5L);
    }

    @Test
    void createDeadlineDelegatesToService() throws Exception {
        DeadlineCreateDto payload = new DeadlineCreateDto();
        payload.setTitle("Submit");
        payload.setDueDate(LocalDateTime.now());
        payload.setRestriction(DeadlineRestriction.PROJECT_CREATION);
        payload.setTimeZone("UTC");

        DeadlineDetailsDto result = new DeadlineDetailsDto();
        result.setTitle("Submit");
        when(deadlineService.createDeadline(eq(3L), any(DeadlineCreateDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/deadlines/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit"));

        verify(deadlineService).createDeadline(eq(3L), any(DeadlineCreateDto.class));
    }

    @Test
    void updateDeadlineReturnsUpdatedDto() throws Exception {
        DeadlineUpdateDto payload = new DeadlineUpdateDto();
        payload.setTitle("Updated");
        payload.setDescription("Desc");
        payload.setDueDate(LocalDateTime.now());
        payload.setRestriction(DeadlineRestriction.APPLICATION_SUBMISSION);

        DeadlineDetailsDto dto = new DeadlineDetailsDto();
        dto.setTitle("Updated");
        when(deadlineService.updateDeadline(eq(9L), any(DeadlineUpdateDto.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/deadlines/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));

        verify(deadlineService).updateDeadline(eq(9L), any(DeadlineUpdateDto.class));
    }

    @Test
    void deleteDeadlineReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/deadlines/12"))
                .andExpect(status().isNoContent());

        verify(deadlineService).deleteDeadline(12L);
    }
}

