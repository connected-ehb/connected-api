package com.ehb.connected.domain.impl.assignments.controllers;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementCreateDto;
import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;
import com.ehb.connected.domain.impl.announcements.service.AnnouncementService;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.service.ApplicationService;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.service.AssignmentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssignmentServiceImpl assignmentService;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private AnnouncementService announcementService;

    private final Principal principal = () -> "teacher@example.com";

    @Test
    void getAssignmentsFromCanvasReturnsFilteredAssignments() throws Exception {
        AssignmentDetailsDto dto = new AssignmentDetailsDto();
        dto.setId(1L);
        when(assignmentService.getNewAssignmentsFromCanvas(principal, 10L)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/assignments/canvas/10").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(assignmentService).getNewAssignmentsFromCanvas(principal, 10L);
    }

    @Test
    void createAssignmentReturnsCreatedDto() throws Exception {
        AssignmentCreateDto payload = new AssignmentCreateDto();
        payload.setName("New Assignment");

        AssignmentDetailsDto response = new AssignmentDetailsDto();
        response.setId(7L);
        response.setName("New Assignment");

        when(assignmentService.createAssignment(any(AssignmentCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/assignments/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.name").value("New Assignment"));

        verify(assignmentService).createAssignment(any(AssignmentCreateDto.class));
    }

    @Test
    void deleteAssignmentReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/assignments/15").principal(principal))
                .andExpect(status().isNoContent());

        verify(assignmentService).deleteAssignmentById(principal, 15L);
    }

    @Test
    void getApplicationsByAssignmentReturnsList() throws Exception {
        ApplicationDetailsDto application = new ApplicationDetailsDto(3L, "motivation", null, null, null);
        application.setId(3L);
        when(applicationService.getAllApplications(principal, 5L)).thenReturn(List.of(application));

        mockMvc.perform(get("/api/assignments/5/applications").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));

        verify(applicationService).getAllApplications(principal, 5L);
    }

    @Test
    void createAnnouncementDelegatesToService() throws Exception {
        AnnouncementCreateDto payload = new AnnouncementCreateDto();
        AnnouncementDetailsDto response = new AnnouncementDetailsDto(9L, 6L, "Title", "Message", java.time.LocalDateTime.now(), null);
        response.setId(9L);
        when(announcementService.createAnnouncementByAssignment(eq(principal), eq(6L), any(AnnouncementCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/assignments/6/announcements")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L));

        verify(announcementService).createAnnouncementByAssignment(eq(principal), eq(6L), any(AnnouncementCreateDto.class));
    }

    @Test
    void getAnnouncementsReturnsList() throws Exception {
        AnnouncementDetailsDto dto = new AnnouncementDetailsDto(11L, 8L, "Title", "Body", java.time.LocalDateTime.now(), null);
        dto.setId(11L);
        when(announcementService.getAnnouncementsByAssignment(principal, 8L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/assignments/8/announcements").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11L));

        verify(announcementService).getAnnouncementsByAssignment(principal, 8L);
    }
}

