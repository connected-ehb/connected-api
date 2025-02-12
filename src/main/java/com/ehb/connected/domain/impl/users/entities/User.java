package com.ehb.connected.domain.impl.users.entities;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long canvasUserId;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String fieldOfStudy;
    private String profileImageUrl;
    private String linkedinUrl;
    private String aboutMe;

    private String accessToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();




    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Project> createdProjects = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<Project> projects = new ArrayList<>();

    @Transient // OAuth2 attributes are not stored in the database
    private Map<String, Object> attributes;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @ManyToMany
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}