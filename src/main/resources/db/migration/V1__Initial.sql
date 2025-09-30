-- Flyway V1 initial schema for MySQL
-- Generated from existing JPA entities with FKs

-- Users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    canvas_user_id BIGINT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    field_of_study VARCHAR(255),
    profile_image_url VARCHAR(512),
    linkedin_url VARCHAR(512),
    about_me VARCHAR(255),
    access_token VARCHAR(255),
    refresh_token VARCHAR(255),
    email_verification_token VARCHAR(255),
    email_verification_token_expiry DATETIME,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50),
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    delete_requested_at DATETIME,
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: courses
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    canvas_id BIGINT,
    uuid VARCHAR(255),
    name VARCHAR(255),
    canvas_created_at DATETIME(6),
    start_at DATETIME(6),
    end_at DATETIME(6),
    owner_id BIGINT,
    CONSTRAINT fk_courses_owner FOREIGN KEY (owner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: assignments
CREATE TABLE assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    canvas_id BIGINT,
    name VARCHAR(255),
    description TEXT,
    default_team_size INT NOT NULL DEFAULT 1,
    course_id BIGINT,
    CONSTRAINT fk_assignments_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: tags
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Join table: user_tags (User ↔ Tag)
CREATE TABLE user_tags (
    user_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, tag_id),
    CONSTRAINT fk_user_tags_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: projects
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gid BINARY(16),
    title VARCHAR(255),
    description TEXT,
    short_description VARCHAR(500),
    status ENUM('APPROVED','NEEDS_REVISION','PENDING','PUBLISHED','REJECTED','REVISED') NOT NULL DEFAULT 'PENDING',
    repository_url VARCHAR(512),
    board_url VARCHAR(512),
    background_image VARCHAR(512),
    team_size INT NOT NULL DEFAULT 1,
    assignment_id BIGINT,
    created_by_user_id BIGINT,
    product_owner_user_id BIGINT,
    CONSTRAINT fk_projects_assignment
        FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_projects_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_projects_product_owner
        FOREIGN KEY (product_owner_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE project_tag (
    project_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, tag_id),
    CONSTRAINT fk_project_tag_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_tag_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE project_user (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_user_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_user_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    motivation_md TEXT,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    project_id BIGINT,
    user_id BIGINT,
    CONSTRAINT fk_applications_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(1000),
    message TEXT,
    assignment_id BIGINT,
    user_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_announcements_assignment
        FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_announcements_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE deadlines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255),
    due_date DATETIME(6),
    description TEXT,
    restriction ENUM('PROJECT_CREATION','APPLICATION_SUBMISSION','NONE') NOT NULL DEFAULT 'NONE',
    time_zone VARCHAR(255),
    assignment_id BIGINT NOT NULL,
    CONSTRAINT fk_deadlines_assignment
        FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment VARCHAR(255),
    user_id BIGINT,
    project_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_feedbacks_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedbacks_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    status ENUM('THUMBS_UP','THUMBS_DOWN') NOT NULL DEFAULT 'THUMBS_DOWN',
    reviewer_id BIGINT,
    project_id BIGINT,
    CONSTRAINT fk_reviews_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_reviews_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uk_review_project_reviewer UNIQUE (project_id, reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(255),
    created_by_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at DATETIME(6),
    used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_invitations_user
        FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    message VARCHAR(512),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    destination_url VARCHAR(512),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    canvas_user_id BIGINT,
    course_id BIGINT,
    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bugs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    description TEXT NOT NULL,
    route VARCHAR(512),
    app_version VARCHAR(64),
    user_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_bugs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_bugs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


