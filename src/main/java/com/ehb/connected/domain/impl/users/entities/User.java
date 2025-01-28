package com.ehb.connected.domain.impl.users.entities;

import com.ehb.connected.domain.impl.discussions.entities.Discussion;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.roles.entities.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String first_Name;
    private String last_Name;
    private String field_Of_Study;
    private String profile_Image;
    private String linkedin_Url;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = true)
    private Role role;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Project> createdProjects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Feedback> feedbackGiven = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Discussion> discussions = new ArrayList<>();


}
