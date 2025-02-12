package com.ehb.connected.domain.impl.assignments.entities;


import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long canvasAssignmentId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    private int defaultTeamSize;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<Deadline> deadlines = new ArrayList<>();
}
