package com.ehb.connected.domain.impl.users.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements OAuth2User, UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long canvasUserId;

    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    private String fieldOfStudy;
    private String profileImageUrl;
    private String linkedinUrl;
    private String aboutMe;

    private String accessToken;
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Project> createdProjects = new ArrayList<>();

    @OneToMany(mappedBy = "productOwner", cascade = CascadeType.ALL)
    private List<Project> productOwnedProjects = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<Project> projects = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    private LocalDateTime deleteRequestedAt;

    @Transient
    private Map<String, Object> attributes;

    // OAuth2User methods
    @Override
    public String getName() {
        return email;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return new HashSet<>();
        }

        Set<GrantedAuthority> authorities = new HashSet<>(role.getAuthorities());
        return authorities;
    }

    // UserDetails methods
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}