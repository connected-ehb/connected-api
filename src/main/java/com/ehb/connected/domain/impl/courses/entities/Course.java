package com.ehb.connected.domain.impl.courses.entities;

import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long canvasId;

    //2022-06-24T13:54:26Z this format is returned by canvas
    private OffsetDateTime canvasCreatedAt;

    private String uuid;

    private String name;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();
}
