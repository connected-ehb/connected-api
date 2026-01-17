package com.ehb.connected.domain.impl.projects.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID gid;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 500)
    private String shortDescription;
    @Enumerated(EnumType.STRING)
    private ProjectStatusEnum status;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private int teamSize;

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToMany
    @JoinTable(
            name = "project_tag",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "product_owner_user_id")
    private User productOwner;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "project_user",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();

    private static final EnumSet<ProjectStatusEnum> LOCKED =
            EnumSet.of(ProjectStatusEnum.APPROVED, ProjectStatusEnum.REJECTED, ProjectStatusEnum.PUBLISHED);

    public boolean isLocked() {
        return status != null && LOCKED.contains(status);
    }

    public boolean hasStatus(ProjectStatusEnum status) {
        return this.status.equals(status);
    }

    public boolean hasNoMembers() {
        return this.members.isEmpty();
    }

    public boolean hasActiveApplication(User user) {
        return this.getApplications().stream()
                .anyMatch(app -> app.getApplicant().getId().equals(user.getId())
                        && !app.hasStatus(ApplicationStatusEnum.REJECTED));
    }

    public boolean hasReachedMaxMembers() {
        return members.size() >= teamSize;
    }

    public boolean isEditable(User user) {
        return user.hasRole(Role.TEACHER) || !isLocked();
    }
}
