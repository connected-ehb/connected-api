package com.ehb.connected.domain.impl.feedbacks.entities;


import com.ehb.connected.domain.impl.users.entities.User;
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
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
}
