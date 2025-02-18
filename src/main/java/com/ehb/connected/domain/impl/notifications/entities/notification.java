package com.ehb.connected.domain.impl.notifications.entities;

import com.ehb.connected.domain.impl.users.entities.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class notification {
    @Id
    private Long id;

    @ManyToOne
    private User user;

    private String message;

    private boolean read;

    private LocalDateTime timestamp = LocalDateTime.now();

}
