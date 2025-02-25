package com.ehb.connected.domain.impl.reviews.entities;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReviewStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
