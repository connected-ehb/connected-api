package com.ehb.connected.domain.impl.users.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void isEnabledRequiresVerifiedEmail() {
        User user = new User();
        user.setEnabled(true);
        user.setEmailVerified(false);

        assertThat(user.isEnabled()).isFalse();

        user.setEmailVerified(true);

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void hasRoleReturnsTrueWhenMatchingRole() {
        User user = new User();
        user.setRole(Role.STUDENT);

        assertThat(user.hasRole(Role.STUDENT)).isTrue();
        assertThat(user.hasRole(Role.TEACHER)).isFalse();
    }

    @Test
    void isProductOwnerAndIsCreatorChecksCorrectly() {
        User owner = new User();
        owner.setId(1L);
        owner.setCanvasUserId(100L);
        Project project = new Project();
        project.setProductOwner(owner);
        project.setCreatedBy(owner);

        assertThat(owner.isProductOwner(project)).isTrue();
        assertThat(owner.isCreator(project)).isTrue();

        User other = new User();
        other.setId(2L);
        other.setCanvasUserId(200L);
        assertThat(other.isProductOwner(project)).isFalse();
        assertThat(other.isCreator(project)).isFalse();
    }

    @Test
    void isApplicantMatchesApplicationApplicant() {
        User applicant = new User();
        applicant.setId(1L);
        applicant.setCanvasUserId(100L);
        Application app = new Application();
        app.setApplicant(applicant);

        User otherApplicant = new User();
        otherApplicant.setId(2L);
        otherApplicant.setCanvasUserId(200L);

        assertThat(applicant.isApplicant(app)).isTrue();
        assertThat(otherApplicant.isApplicant(app)).isFalse();
    }

    @Test
    void getAuthoritiesReturnsEmptyWhenRoleNull() {
        User user = new User();
        user.setRole(null);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).isEmpty();
    }

    @Test
    void getAuthoritiesReturnsRoleAuthoritiesWhenSet() {
        User teacher = new User();
        teacher.setRole(Role.TEACHER);

        Collection<? extends GrantedAuthority> authorities = teacher.getAuthorities();
        assertThat(authorities).isNotEmpty();
    }

    @Test
    void studentCanViewPublishedProject() {
        User student = new User();
        student.setRole(Role.STUDENT);

        Project project = new Project();
        project.setStatus(ProjectStatusEnum.PUBLISHED);
        project.setCreatedBy(new User());
        project.getCreatedBy().setRole(Role.STUDENT);
        project.setProductOwner(new User());

        assertThat(student.canViewProject(project)).isTrue();
    }

    @Test
    void teacherCanViewAllProjectStatuses() {
        User teacher = new User();
        teacher.setRole(Role.TEACHER);

        for (ProjectStatusEnum status : ProjectStatusEnum.values()) {
            Project project = new Project();
            project.setStatus(status);
            project.setCreatedBy(new User());
            project.getCreatedBy().setRole(Role.STUDENT);
            project.setProductOwner(new User());

            assertThat(teacher.canViewProject(project))
                    .as("Teacher should be able to view project with status %s", status)
                    .isTrue();
        }
    }

    @Test
    void studentCanViewWhenCreatorIsTeacherOrResearcher() {
        User student = new User();
        student.setRole(Role.STUDENT);

        // Teacher creator
        Project teacherProject = new Project();
        teacherProject.setStatus(ProjectStatusEnum.PUBLISHED);
        User teacher = new User();
        teacher.setRole(Role.TEACHER);
        teacherProject.setCreatedBy(teacher);
        assertThat(student.canViewProject(teacherProject)).isTrue();

        // Researcher creator
        Project researcherProject = new Project();
        researcherProject.setStatus(ProjectStatusEnum.PUBLISHED);
        User researcher = new User();
        researcher.setRole(Role.RESEARCHER);
        researcherProject.setCreatedBy(researcher);
        assertThat(student.canViewProject(researcherProject)).isTrue();
    }


    @Test
    void studentCanViewProjectWhenProductOwner() {
        User productOwner = new User();
        productOwner.setRole(Role.STUDENT);

        Project project = new Project();
        project.setStatus(ProjectStatusEnum.PENDING);
        project.setCreatedBy(new User());
        project.getCreatedBy().setRole(Role.STUDENT);
        project.setProductOwner(productOwner);

        assertThat(productOwner.canViewProject(project)).isTrue();
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        User u1 = new User();
        u1.setId(1L);
        u1.setCanvasUserId(10L);

        User u2 = new User();
        u2.setId(1L);
        u2.setCanvasUserId(10L);

        User u3 = new User();
        u3.setId(2L);
        u3.setCanvasUserId(11L);

        assertThat(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u1).isNotEqualTo(null);
        assertThat(u1).isNotEqualTo("some string");
    }

    @Test
    void gettersAndSettersWork() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@doe.com");
        user.setPassword("pass");
        user.setFieldOfStudy("CS");
        user.setProfileImageUrl("img.png");
        user.setLinkedinUrl("linkedin");
        user.setAboutMe("Hi there!");
        user.setDeleteRequestedAt(null);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john@doe.com");
        assertThat(user.getPassword()).isEqualTo("pass");
        assertThat(user.getFieldOfStudy()).isEqualTo("CS");
        assertThat(user.getProfileImageUrl()).isEqualTo("img.png");
        assertThat(user.getLinkedinUrl()).isEqualTo("linkedin");
        assertThat(user.getAboutMe()).isEqualTo("Hi there!");
    }

    @Test
    void accountFlagsAllReturnExpectedValues() {
        User user = new User();
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setEmailVerified(true);

        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void usernameReturnsEmail() {
        User user = new User();
        user.setEmail("example@mail.com");

        assertThat(user.getUsername()).isEqualTo("example@mail.com");
    }
}
