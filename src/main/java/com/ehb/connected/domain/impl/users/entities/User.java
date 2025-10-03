package com.ehb.connected.domain.impl.users.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails, Serializable {

    // ---------- Identifiers ----------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long canvasUserId;

    // ---------- Personal Info ----------
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;
    private String fieldOfStudy;
    private String profileImageUrl;
    private String linkedinUrl;
    private String aboutMe;

    // ---------- OAuth2 / Tokens ----------
    private String accessToken;
    private String refreshToken;

    // ---------- Email Verification ----------
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;
    private boolean emailVerified = false;

    // ---------- Role & Security ----------
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    // ---------- Relations ----------
    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Project> createdProjects = new ArrayList<>();

    @OneToMany(mappedBy = "productOwner", cascade = CascadeType.ALL)
    private List<Project> productOwnedProjects = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<Project> projects = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    // ---------- Account Management ----------
    private LocalDateTime deleteRequestedAt;

    // ---------- Utility Methods ----------
    public boolean hasRole(Role role) {
        return this.role.equals(role);
    }

    public boolean isProductOwner(Project project) {
        return this.equals(project.getProductOwner());
    }

    public boolean isApplicant(Application application) {
        return this.equals(application.getApplicant());
    }

    public boolean isCreator(Project project) {
        return project.getCreatedBy().equals(this);
    }

    public boolean canViewProject(Project project) {
        return project.hasStatus(ProjectStatusEnum.PUBLISHED) ||
                project.getCreatedBy().hasRole(Role.TEACHER) ||
                project.getCreatedBy().hasRole(Role.RESEARCHER) ||
                this.hasRole(Role.TEACHER) ||
                this.isProductOwner(project);
    }

    // ---------- UserDetails Implementation ----------
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return new HashSet<>();
        }
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled && emailVerified;
    }

    // ---------- Equals & HashCode ----------
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(canvasUserId, user.canvasUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, canvasUserId);
    }
}