package com.ehb.connected.domain.impl.assignments.dto;

import com.ehb.connected.domain.impl.projects.dto.ProjectSummaryDto;
import com.ehb.connected.domain.impl.users.dto.UserSummaryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardDetailsDto {

    private Counts counts;
    private Lists lists;

    @Getter
    @Setter
    public static class Counts {
        private int totalStudents;
        private int assignedStudents;
        private int unassignedStudents;
        private int reviewQueueProjects;      // PENDING + REVISED
        private int needsRevisionProjects;    // NEEDS_REVISION
        private int pendingApplications;      // optional
    }

    @Getter
    @Setter
    public static class Lists {
        private List<ProjectSummaryDto> reviewQueue;
        private List<ProjectSummaryDto> needsRevision;
        private List<UserSummaryDto> unassignedStudents;
    }
}
