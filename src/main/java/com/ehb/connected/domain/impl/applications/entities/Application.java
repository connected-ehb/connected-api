package com.ehb.connected.domain.impl.applications.entities;


import com.ehb.connected.domain.impl.projects.entities.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String motivationMd;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;
}
