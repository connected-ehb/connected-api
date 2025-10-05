package com.ehb.connected.domain.impl.applications.controllers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationServiceImpl applicationService;

    private final Principal principal = () -> "student@example.com";

    private ApplicationDetailsDto buildDto(long id, ApplicationStatusEnum status) {
        ProjectDetailsDto project = new ProjectDetailsDto();
        project.setId(44L);
        UserDetailsDto applicant = new UserDetailsDto();
        applicant.setId(55L);
        return new ApplicationDetailsDto(id, "motivation", status, project, applicant);
    }

    @Test
    void reviewApplicationUpdatesStatus() throws Exception {
        ApplicationDetailsDto dto = buildDto(3L, ApplicationStatusEnum.APPROVED);
        when(applicationService.reviewApplication(principal, 3L, ApplicationStatusEnum.APPROVED)).thenReturn(dto);

        mockMvc.perform(post("/api/applications/3/review")
                        .principal(principal)
                        .header("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(applicationService).reviewApplication(principal, 3L, ApplicationStatusEnum.APPROVED);
    }

    @Test
    void joinProjectReturnsUpdatedApplication() throws Exception {
        ApplicationDetailsDto dto = buildDto(5L, ApplicationStatusEnum.APPROVED);
        when(applicationService.joinProject(principal, 5L)).thenReturn(dto);

        mockMvc.perform(post("/api/applications/5/join").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));

        verify(applicationService).joinProject(principal, 5L);
    }

    @Test
    void getApplicationByIdReturnsDto() throws Exception {
        ApplicationDetailsDto dto = buildDto(7L, ApplicationStatusEnum.PENDING);
        when(applicationService.getById(principal, 7L)).thenReturn(dto);

        mockMvc.perform(get("/api/applications/7").principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(applicationService).getById(principal, 7L);
    }
}
