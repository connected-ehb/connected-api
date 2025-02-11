package com.ehb.connected.domain.impl.courses.entities;

import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.ehb.connected.domain.impl.assignments.entities.Assignment; // Correct import

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private String name;

    private LocalDateTime start_at;

    private LocalDateTime end_at;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
