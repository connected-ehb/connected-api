package com.ehb.connected.domain.impl.projects.controllers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.service.ApplicationService;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.service.FeedbackService;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.service.ReviewService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private FeedbackService feedbackService;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private ReviewService reviewService;

    private final Principal principal = () -> "user";

    @Test
    void getProjectByIdReturnsProjectDetails() throws Exception {
        ProjectDetailsDto dto = new ProjectDetailsDto();
        dto.setId(42L);
        dto.setTitle("Capstone");
        when(projectService.getProjectById(principal, 42L)).thenReturn(dto);

        mockMvc.perform(get("/api/projects/42").principal(principal))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("Capstone"));

        verify(projectService).getProjectById(principal, 42L);
    }

    @Test
    void createProjectDelegatesToService() throws Exception {
        ProjectCreateDto payload = new ProjectCreateDto();
        payload.setTitle("New Project");

        ProjectDetailsDto response = new ProjectDetailsDto();
        response.setId(5L);
        response.setTitle("New Project");

        when(projectService.createProject(eq(principal), eq(11L), any(ProjectCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/11")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("New Project"));

        verify(projectService).createProject(eq(principal), eq(11L), any(ProjectCreateDto.class));
    }

    @Test
    void updateProjectReturnsUpdatedDetails() throws Exception {
        ProjectUpdateDto payload = new ProjectUpdateDto();
        payload.setTitle("Updated");
        payload.setDescription("Desc");
        payload.setShortDescription("Short");
        payload.setTeamSize(4);
        payload.setTags(List.of());

        ProjectDetailsDto response = new ProjectDetailsDto();
        response.setTitle("Updated");

        when(projectService.save(eq(principal), eq(9L), any(ProjectUpdateDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/projects/9")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));

        verify(projectService).save(eq(principal), eq(9L), any(ProjectUpdateDto.class));
    }

    @Test
    void changeStatusReadsEnumFromHeader() throws Exception {
        ProjectDetailsDto response = new ProjectDetailsDto();
        response.setStatus(ProjectStatusEnum.APPROVED);
        when(projectService.changeProjectStatus(principal, 7L, ProjectStatusEnum.APPROVED)).thenReturn(response);

        mockMvc.perform(post("/api/projects/7/status")
                        .principal(principal)
                        .header("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(projectService).changeProjectStatus(principal, 7L, ProjectStatusEnum.APPROVED);
    }

    @Test
    void getAllGlobalProjectsReturnsList() throws Exception {
        ProjectDetailsDto dto = new ProjectDetailsDto();
        dto.setId(1L);
        when(projectService.getAllGlobalProjects(principal)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/projects/global").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(projectService).getAllGlobalProjects(principal);
    }

    @Test
    void applyForProjectReturnsApplicationDetails() throws Exception {
        ApplicationCreateDto payload = new ApplicationCreateDto();
        ApplicationDetailsDto response = new ApplicationDetailsDto(1L, "motivation", null, null, null);
        when(applicationService.create(eq(principal), eq(15L), any(ApplicationCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/15/apply")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(applicationService).create(eq(principal), eq(15L), any(ApplicationCreateDto.class));
    }

    @Test
    void removeMemberReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/projects/3/members/9").principal(principal))
                .andExpect(status().isOk());

        verify(projectService).removeMember(principal, 3L, 9L);
    }

    @Test
    void publishAllProjectsReturnsPayload() throws Exception {
        when(projectService.publishAllProjects(principal, 12L)).thenReturn(List.of(new ProjectDetailsDto()));

        mockMvc.perform(post("/api/projects/12/publish").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(projectService).publishAllProjects(principal, 12L);
    }

    @Test
    void importProjectDelegatesToService() throws Exception {
        ProjectDetailsDto dto = new ProjectDetailsDto();
        dto.setId(77L);
        when(projectService.importProject(principal, 4L, 99L)).thenReturn(dto);

        mockMvc.perform(post("/api/projects/99/import/4").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77));

        verify(projectService).importProject(principal, 4L, 99L);
    }

    @Test
    void feedbackEndpointsDelegateToFeedbackService() throws Exception {
        FeedbackCreateDto payload = new FeedbackCreateDto();
        FeedbackDto feedbackDto = new FeedbackDto();
        feedbackDto.setId(55L);
        when(feedbackService.giveFeedback(eq(principal), eq(8L), any(FeedbackCreateDto.class))).thenReturn(feedbackDto);

        mockMvc.perform(post("/api/projects/8/feedback")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55L));

        verify(feedbackService).giveFeedback(eq(principal), eq(8L), any(FeedbackCreateDto.class));

        when(feedbackService.getAllFeedbackForProject(principal, 8L)).thenReturn(List.of(feedbackDto));

        mockMvc.perform(get("/api/projects/8/feedback").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(55L));

        verify(feedbackService).getAllFeedbackForProject(principal, 8L);
    }

    @Test
    void reviewEndpointsDelegateToReviewService() throws Exception {
        ReviewCreateDto payload = new ReviewCreateDto();
        ReviewDetailsDto reviewDto = new ReviewDetailsDto();
        reviewDto.setId(88L);

        when(reviewService.createOrUpdateReviewForProject(eq(principal), eq(6L), any(ReviewCreateDto.class))).thenReturn(reviewDto);

        mockMvc.perform(post("/api/projects/6/reviews")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(88L));

        verify(reviewService).createOrUpdateReviewForProject(eq(principal), eq(6L), any(ReviewCreateDto.class));

        when(reviewService.getAllReviewsByProjectId(principal, 6L)).thenReturn(List.of(reviewDto));

        mockMvc.perform(get("/api/projects/6/reviews").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(88L));

        verify(reviewService).getAllReviewsByProjectId(principal, 6L);
    }
}

