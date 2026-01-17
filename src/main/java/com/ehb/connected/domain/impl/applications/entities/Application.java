package com.ehb.connected.domain.impl.applications.entities;


import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    @Column(nullable = false)
    private ApplicationStatusEnum status = ApplicationStatusEnum.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private ReasonEnum reason;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User applicant;

    public boolean hasStatus(ApplicationStatusEnum status) {
        return this.status.equals(status);
    }

    public boolean hasSameAssignment(Project other) {
        if (this.project == null || other == null) return false;
        return Objects.equals(
                this.project.getAssignment(),
                other.getAssignment()
        );
    }

    public void reject(ReasonEnum reason) {
        this.status = ApplicationStatusEnum.REJECTED;
        this.reason = reason;
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
