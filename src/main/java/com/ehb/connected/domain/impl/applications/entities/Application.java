package com.ehb.connected.domain.impl.applications.entities;


import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String motivationMd;

    @Enumerated(EnumType.STRING)
    private ApplicationStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User applicant;

    public boolean hasStatus(ApplicationStatusEnum status) {
        return this.status.equals(status);
    }

    public boolean hasSameAssignment(Project project) {
        return this.project.getAssignment().equals(project.getAssignment());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
