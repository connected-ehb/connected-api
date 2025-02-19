package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.services.CourseService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final AssignmentMapper assignmentMapper;

    private final WebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

    @Override
    public AssignmentDetailsDto createAssignment(AssignmentCreateDto assignmentDto) {
        final Course course = courseService.getCourseById(assignmentDto.getCourseId());
        final Assignment assignmentEntity = assignmentMapper.AssignmentCreateToEntity(assignmentDto);
        assignmentEntity.setCourse(course);
        return assignmentMapper.toAssignmentDetailsDto(assignmentRepository.save(assignmentEntity));
    }

    @Override
    public List<AssignmentDetailsDto> getAllAssignmentsByCourse(Long courseId) {
        return assignmentMapper.toAssignmentDetailsDtoList(assignmentRepository.findByCourseId(courseId));
    }

    /**
     * Retrieves new assignments from Canvas for a given course that have not yet been imported into the system.
     * <p>
     * The method performs the following steps:
     * <ul>
     *   <li>Obtains the currently authenticated user and retrieves their Canvas access token.</li>
     *   <li>Fetches the course using its internal ID to determine the corresponding Canvas course ID.</li>
     *   <li>Calls the Canvas API to retrieve a list of assignments for the specified Canvas course.</li>
     *   <li>Filters the returned assignments by checking if each assignment (based on its Canvas assignment ID)
     *       already exists in the system. Only assignments that are not found (i.e., new assignments) are kept.</li>
     *   <li>Maps the new assignments to {@link AssignmentDetailsDto} objects using the {@code AssignmentMapper}.</li>
     * </ul>
     * </p>
     *
     * @param principal the security principal representing the currently authenticated user.
     * @param courseId the internal identifier of the course for which new assignments are to be fetched.
     * @return a list of {@link AssignmentDetailsDto} objects representing the new assignments from Canvas.
     * @throws EntityNotFoundException if the course or any required entity cannot be found.
     */
    //TODO: Check .block() usage and refactor to avoid blocking calls.
    @Override
    public List<AssignmentDetailsDto> getNewAssignmentsFromCanvas(Principal principal, Long courseId) {
        final User user = userService.getUserByEmail(principal.getName());
        final String token = user.getAccessToken();
        final Course course = courseService.getCourseById(courseId);
        final Long canvasCourseId = course.getCanvasCourseId();

        // Retrieve assignments from Canvas as a List of Maps.
        final List<Map<String, Object>> assignments = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/courses/{canvasCourseId}/assignments")
                        .build(canvasCourseId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        assert assignments != null;
        return assignments.stream()
                .filter(assignment -> {
                    Long canvasAssignmentId = Long.parseLong(assignment.get("id").toString());
                    return !existsAssignmentByCanvasAssignmentId(canvasAssignmentId);
                })
                .map(assignment -> assignmentMapper.fromCanvasMapToAssignmentDetailsDto(assignment, course.getId())).toList();
    }

    private boolean existsAssignmentByCanvasAssignmentId(Long canvasAssignmentId) {
        return assignmentRepository.existsByCanvasAssignmentId(canvasAssignmentId);
    }
}