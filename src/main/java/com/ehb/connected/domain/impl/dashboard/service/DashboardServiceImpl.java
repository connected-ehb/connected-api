package com.ehb.connected.domain.impl.dashboard.service;

import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.assignments.dto.DashboardDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.projects.dto.ProjectSummaryDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.dto.UserSummaryDto;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.domain.impl.users.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public DashboardDetailsDto getDashboardDetails(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow();

        Long courseId = assignment.getCourse().getId();

        // ---------- COUNTS ----------
        int totalStudents = userService.getAllStudentsByCourseId(courseId).size();

        // Assigned students = owners APPROVED/PUBLISHED  ∪ applicants APPROVED (distinct by user id)
        Set<Long> ownerIds = projectRepository.findDistinctApprovedOwnerIds(assignmentId);
        Set<Long> applicantIds = applicationRepository.findDistinctApprovedApplicantIds(assignmentId);

        // Union
        ownerIds.addAll(applicantIds);
        int assignedStudents = ownerIds.size();
        int unassignedStudents = Math.max(0, totalStudents - assignedStudents);

        int reviewQueueProjects = projectRepository.countByAssignmentIdAndStatusIn(
                assignmentId, List.of(ProjectStatusEnum.PENDING, ProjectStatusEnum.REVISED));

        int needsRevisionProjects = projectRepository.countByAssignmentIdAndStatus(
                assignmentId, ProjectStatusEnum.NEEDS_REVISION);

        int pendingApplications = applicationRepository.countPendingByAssignment(assignmentId);

        var reviewQueueProjectsList = projectRepository.findTopReviewQueue(assignmentId);
        var needsRevisionList = projectRepository.findTopNeedsRevision(assignmentId);

        List<Long> rosterCanvasIds = enrollmentRepository.findCanvasUserIdsByCourse(courseId);
        List<Long> rosterUserIds = rosterCanvasIds.isEmpty() ? List.of() : userRepository.findStudentIdsByCanvasUserIds(rosterCanvasIds);

        List<Long> unassignedUserIds = rosterUserIds.stream()
                .filter(id -> !ownerIds.contains(id))
                .limit(10)
                .toList();

        DashboardDetailsDto dto = new DashboardDetailsDto();

        DashboardDetailsDto.Counts counts = new DashboardDetailsDto.Counts();
        counts.setTotalStudents(totalStudents);
        counts.setAssignedStudents(assignedStudents);
        counts.setUnassignedStudents(unassignedStudents);
        counts.setReviewQueueProjects(reviewQueueProjects);
        counts.setNeedsRevisionProjects(needsRevisionProjects);
        counts.setPendingApplications(pendingApplications);
        dto.setCounts(counts);

        DashboardDetailsDto.Lists lists = new DashboardDetailsDto.Lists();
        lists.setReviewQueue(projectsToSummary(reviewQueueProjectsList));
        lists.setNeedsRevision(projectsToSummary(needsRevisionList));
        lists.setUnassignedStudents(usersToSummary(unassignedUserIds));
        dto.setLists(lists);

        return dto;
    }

    private List<ProjectSummaryDto> projectsToSummary(List<Project> projects) {
        return projects.stream().map(p -> {
            ProjectSummaryDto s = new ProjectSummaryDto();
            s.setId(p.getId());
            s.setTitle(p.getTitle());
            s.setStatus(p.getStatus().name());
            var owner = p.getCreatedBy();
            if (owner != null) {
                UserSummaryDto u = new UserSummaryDto();
                u.setId(owner.getId());
                u.setFirstName(owner.getFirstName());
                u.setLastName(owner.getLastName());
                s.setCreatedBy(u);
            }
            return s;
        }).toList();
    }

    private List<UserSummaryDto> usersToSummary(List<Long> userIds) {
        if (userIds.isEmpty()) return List.of();
        return userRepository.findAllById(userIds).stream().map(u -> {
            UserSummaryDto s = new UserSummaryDto();
            s.setId(u.getId());
            s.setFirstName(u.getFirstName());
            s.setLastName(u.getLastName());
            return s;
        }).toList();
    }
}
