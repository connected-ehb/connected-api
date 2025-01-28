package com.ehb.connected.domain.impl.assignments.entities;


import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.projects.entities.Project;
import jakarta.persistence.*;
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
    private Long Id;

   private String title;
   private LocalDateTime date;
   private String description;
   private int default_team_size;

   @ManyToOne
    @JoinColumn(name = "course_id", nullable = true)
    private Course course;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private List<Deadline> deadlines = new ArrayList<>();
}
