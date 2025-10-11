package com.ehb.connected.domain.impl.courses.controllers;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.services.CourseService;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private AssignmentService assignmentService;

    @MockitoBean
    private UserService userService;

    private final Principal principal = () -> "teacher@ehb.be";

    @Test
    void getNewCoursesFromCanvasReturnsPayload() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("teacher@ehb.be", "token");
        CourseDetailsDto dto = new CourseDetailsDto();
        dto.setId(1L);
        when(courseService.getNewCoursesFromCanvas(auth)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/courses/canvas").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(courseService).getNewCoursesFromCanvas(auth);
    }

    @Test
    void createCourseDelegatesToServiceAndReturnsDto() throws Exception {
        CourseCreateDto payload = new CourseCreateDto();
        payload.setName("New Course");

        CourseDetailsDto response = new CourseDetailsDto();
        response.setId(5L);
        response.setName("New Course");

        when(courseService.createCourseWithEnrollments(eq(principal), any(CourseCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses/")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New Course"));

        verify(courseService).createCourseWithEnrollments(eq(principal), any(CourseCreateDto.class));
    }

    @Test
    void getCoursesReturnsOwnerCourses() throws Exception {
        CourseDetailsDto dto = new CourseDetailsDto();
        dto.setId(2L);
        when(courseService.getCoursesByOwner(principal)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/courses/").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(courseService).getCoursesByOwner(principal);
    }

    @Test
    void getEnrolledCoursesReturnsList() throws Exception {
        CourseDetailsDto dto = new CourseDetailsDto();
        dto.setId(9L);
        when(courseService.getCoursesByEnrollment(principal)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/courses/enrolled").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9));

        verify(courseService).getCoursesByEnrollment(principal);
    }

    @Test
    void getAssignmentsByCourseReturnsAssignments() throws Exception {
        AssignmentDetailsDto assignment = new AssignmentDetailsDto();
        assignment.setId(7L);
        when(assignmentService.getAllAssignmentsByCourse(5L)).thenReturn(List.of(assignment));

        mockMvc.perform(get("/api/courses/5/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));

        verify(assignmentService).getAllAssignmentsByCourse(5L);
    }

    @Test
    void getStudentsReturnsList() throws Exception {
        UserDetailsDto student = new UserDetailsDto();
        student.setId(3L);
        when(userService.getAllStudentsByCourseId(8L)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/courses/8/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));

        verify(userService).getAllStudentsByCourseId(8L);
    }

    @Test
    void deleteCourseReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/courses/11"))
                .andExpect(status().isNoContent());

        verify(courseService).deleteCourseById(11L);
    }

    @Test
    void refreshEnrollmentsReturnsUpdatedCourse() throws Exception {
        CourseDetailsDto dto = new CourseDetailsDto();
        dto.setId(15L);
        when(courseService.refreshEnrollments(principal, 15L)).thenReturn(dto);

        mockMvc.perform(post("/api/courses/15/enrollments/refresh").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15));

        verify(courseService).refreshEnrollments(principal, 15L);
    }
}
