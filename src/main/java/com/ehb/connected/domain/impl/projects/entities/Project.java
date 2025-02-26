package com.ehb.connected.domain.impl.projects.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long gid;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "product_owner_user_id")
    private User productOwner;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();

}
