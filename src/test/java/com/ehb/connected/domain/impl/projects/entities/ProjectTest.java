package com.ehb.connected.domain.impl.projects.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest {

    @Test
    void hasStatusReturnsTrueWhenStatusesMatch() {
        Project project = new Project();
        project.setStatus(ProjectStatusEnum.APPROVED);

        assertThat(project.hasStatus(ProjectStatusEnum.APPROVED)).isTrue();
        assertThat(project.hasStatus(ProjectStatusEnum.PENDING)).isFalse();
    }

    @Test
    void hasNoMembersReflectsMembersList() {
        Project project = new Project();
        project.setMembers(new java.util.ArrayList<>());
        assertThat(project.hasNoMembers()).isTrue();

        project.getMembers().add(new User());
        assertThat(project.hasNoMembers()).isFalse();
    }

    @Test
    void hasUserAppliedDetectsMatchingApplication() {
        Project project = new Project();
        User applicant = new User();
        applicant.setId(1L);

        Application application = new Application();
        application.setApplicant(applicant);
        application.setProject(project);
        project.setApplications(new java.util.ArrayList<>(List.of(application)));

        assertThat(project.hasUserApplied(applicant)).isTrue();

        User stranger = new User();
        stranger.setId(2L);
        assertThat(project.hasUserApplied(stranger)).isFalse();
    }

    @Test
    void hasReachedMaxMembersChecksTeamSizeConstraint() {
        Project project = new Project();
        project.setTeamSize(2);
        project.setMembers(new java.util.ArrayList<>(List.of(new User(), new User())));

        assertThat(project.hasReachedMaxMembers()).isTrue();

        project.getMembers().remove(0);
        assertThat(project.hasReachedMaxMembers()).isFalse();
    }

    @Test
    void isEditableFalseForTerminalStatusesTrueOtherwise() {
        Project project = new Project();
        User applicant = new User();
        applicant.setRole(Role.STUDENT);
        project.setStatus(ProjectStatusEnum.PENDING);
        assertThat(project.isEditable(applicant)).isTrue();

        project.setStatus(ProjectStatusEnum.APPROVED);
        assertThat(project.isEditable(applicant)).isFalse();

        project.setStatus(ProjectStatusEnum.REJECTED);
        assertThat(project.isEditable(applicant)).isFalse();

        project.setStatus(ProjectStatusEnum.PUBLISHED);
        assertThat(project.isEditable(applicant)).isFalse();
    }
}
