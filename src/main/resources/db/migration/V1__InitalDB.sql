CREATE TABLE application
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    motivation_md LONGTEXT              NULL,
    status        VARCHAR(50)           NULL,
    user_id       BIGINT                NULL,
    project_id    BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE assignment
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    canvas_id         BIGINT                NULL,
    default_team_size INT                   NOT NULL,
    `description`     LONGTEXT              NULL,
    name              VARCHAR(255)          NULL,
    course_id         BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE assignment_deadlines
(
    assignment_id BIGINT NOT NULL,
    deadlines_id  BIGINT NOT NULL
);

CREATE TABLE assignment_projects
(
    assignment_id BIGINT NOT NULL,
    projects_id   BIGINT NOT NULL
);

CREATE TABLE course
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    canvas_id BIGINT                NULL,
    end_at    datetime              NULL,
    name      VARCHAR(255)          NULL,
    start_at  datetime              NULL,
    uuid      VARCHAR(255)          NULL,
    owner_id  BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE deadline
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    `description` VARCHAR(255)          NULL,
    due_date      datetime              NULL,
    restriction   VARCHAR(50)           NULL,
    time_zone     VARCHAR(255)          NULL,
    title         VARCHAR(255)          NULL,
    assignment_id BIGINT                NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE discussion
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    comment    VARCHAR(255)          NULL,
    project_id BIGINT                NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE enrollment
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    canvas_user_id BIGINT                NULL,
    course_id      BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE feedback
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    comment    VARCHAR(255)          NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    project_id BIGINT                NULL,
    user_id    BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE invitation
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    code          VARCHAR(255)          NULL,
    created_at    datetime              NULL,
    expires_at    datetime              NULL,
    used          BIT(1)                NOT NULL,
    created_by_id BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE members
(
    project_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL
);

CREATE TABLE notification
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    destination_url VARCHAR(255)          NULL,
    is_read         BIT(1)                NULL,
    message         VARCHAR(255)          NULL,
    timestamp       datetime              NULL,
    user_id         BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE project
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    background_image      VARCHAR(255)          NULL,
    board_url             VARCHAR(255)          NULL,
    `description`         LONGTEXT              NULL,
    gid                   BLOB                  NULL,
    repository_url        VARCHAR(255)          NULL,
    short_description     VARCHAR(500)          NULL,
    status                VARCHAR(50)           NULL,
    team_size             INT                   NOT NULL,
    title                 VARCHAR(255)          NULL,
    assignment_id         BIGINT                NULL,
    created_by_user_id    BIGINT                NULL,
    product_owner_user_id BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE project_feedbacks
(
    project_id   BIGINT NOT NULL,
    feedbacks_id BIGINT NOT NULL
);

CREATE TABLE project_tag
(
    project_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL
);

CREATE TABLE review
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    status      VARCHAR(50)           NULL,
    project_id  BIGINT                NULL,
    reviewer_id BIGINT                NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE tag
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)          NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE user_tags
(
    user_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL
);

CREATE TABLE users
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    about_me          VARCHAR(255)          NULL,
    access_token      VARCHAR(255)          NULL,
    canvas_user_id    BIGINT                NULL,
    email             VARCHAR(255)          NULL,
    field_of_study    VARCHAR(255)          NULL,
    first_name        VARCHAR(255)          NULL,
    last_name         VARCHAR(255)          NULL,
    linkedin_url      VARCHAR(255)          NULL,
    password          VARCHAR(255)          NULL,
    profile_image_url VARCHAR(255)          NULL,
    refresh_token     VARCHAR(255)          NULL,
    `role`            VARCHAR(50)           NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

ALTER TABLE project_feedbacks
    ADD CONSTRAINT UK30nk9bydwtbkkjcnnmdycknc8 UNIQUE (feedbacks_id);

ALTER TABLE users
    ADD CONSTRAINT UK6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE assignment_deadlines
    ADD CONSTRAINT UKi8uomcplkjqf758x7nbpvmssw UNIQUE (deadlines_id);

ALTER TABLE assignment_projects
    ADD CONSTRAINT UKm7epi66cultgqiif9dkxpkmoa UNIQUE (projects_id);

ALTER TABLE user_tags
    ADD CONSTRAINT FK21732a8pkm7g7pt7xugpto6qg FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE NO ACTION;

CREATE INDEX FK21732a8pkm7g7pt7xugpto6qg ON user_tags (tag_id);

ALTER TABLE review
    ADD CONSTRAINT FK29sgaw0fsbkrgfd8gv15j9vvk FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FK29sgaw0fsbkrgfd8gv15j9vvk ON review (reviewer_id);

ALTER TABLE assignment_deadlines
    ADD CONSTRAINT FK3a7j3vbk288wasw4q06k3o4ri FOREIGN KEY (deadlines_id) REFERENCES deadline (id) ON DELETE NO ACTION;

ALTER TABLE project_feedbacks
    ADD CONSTRAINT FK3e7cad9mbj9au4597jryh1s48 FOREIGN KEY (feedbacks_id) REFERENCES feedback (id) ON DELETE NO ACTION;

ALTER TABLE invitation
    ADD CONSTRAINT FK3oigvd1xhxq52ynnxmuugf3mk FOREIGN KEY (created_by_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FK3oigvd1xhxq52ynnxmuugf3mk ON invitation (created_by_id);

ALTER TABLE project_tag
    ADD CONSTRAINT FK519h89u5tkrcmyquqgr5lh3y2 FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE NO ACTION;

CREATE INDEX FK519h89u5tkrcmyquqgr5lh3y2 ON project_tag (tag_id);

ALTER TABLE assignment_projects
    ADD CONSTRAINT FK6iharmk2jj8rrgtialrrkwauc FOREIGN KEY (projects_id) REFERENCES project (id) ON DELETE NO ACTION;

ALTER TABLE discussion
    ADD CONSTRAINT FK6m45sjpuse6o09iwiv2qdj3xg FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FK6m45sjpuse6o09iwiv2qdj3xg ON discussion (project_id);

ALTER TABLE members
    ADD CONSTRAINT FK8k1i9mdvowfp46l90alfy37ke FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FK8k1i9mdvowfp46l90alfy37ke ON members (project_id);

ALTER TABLE application
    ADD CONSTRAINT FKawte0mbtubellxed1dvpoxhdj FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKawte0mbtubellxed1dvpoxhdj ON application (user_id);

ALTER TABLE enrollment
    ADD CONSTRAINT FKbhhcqkw1px6yljqg92m0sh2gt FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE NO ACTION;

CREATE INDEX FKbhhcqkw1px6yljqg92m0sh2gt ON enrollment (course_id);

ALTER TABLE review
    ADD CONSTRAINT FKcv4hm14o255rb1jfsk9nmp0kp FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FKcv4hm14o255rb1jfsk9nmp0kp ON review (project_id);

ALTER TABLE feedback
    ADD CONSTRAINT FKd3k0p62wu7lsw335o1xul28ah FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FKd3k0p62wu7lsw335o1xul28ah ON feedback (project_id);

ALTER TABLE user_tags
    ADD CONSTRAINT FKdylhtw3qjb2nj40xp50b0p495 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKdylhtw3qjb2nj40xp50b0p495 ON user_tags (user_id);

ALTER TABLE assignment_deadlines
    ADD CONSTRAINT FKe5cf1b2im777yg0mnokxxqr2r FOREIGN KEY (assignment_id) REFERENCES assignment (id) ON DELETE NO ACTION;

CREATE INDEX FKe5cf1b2im777yg0mnokxxqr2r ON assignment_deadlines (assignment_id);

ALTER TABLE project
    ADD CONSTRAINT FKfjchir3jae9wykcdv3llr2iu4 FOREIGN KEY (assignment_id) REFERENCES assignment (id) ON DELETE NO ACTION;

CREATE INDEX FKfjchir3jae9wykcdv3llr2iu4 ON project (assignment_id);

ALTER TABLE project
    ADD CONSTRAINT FKg5ag6umks2v0n0ekgcrl7fp7u FOREIGN KEY (created_by_user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKg5ag6umks2v0n0ekgcrl7fp7u ON project (created_by_user_id);

ALTER TABLE course
    ADD CONSTRAINT FKhjy2hwlpkjh9qpdfoi1npnbmn FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKhjy2hwlpkjh9qpdfoi1npnbmn ON course (owner_id);

ALTER TABLE project_tag
    ADD CONSTRAINT FKk3ccabfs72wkx2008pn7tij9b FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FKk3ccabfs72wkx2008pn7tij9b ON project_tag (project_id);

ALTER TABLE project
    ADD CONSTRAINT FKknlkxv817uxp18e1g89e5htr9 FOREIGN KEY (product_owner_user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKknlkxv817uxp18e1g89e5htr9 ON project (product_owner_user_id);

ALTER TABLE project_feedbacks
    ADD CONSTRAINT FKn174xweqle72enaqs5tm03kk4 FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FKn174xweqle72enaqs5tm03kk4 ON project_feedbacks (project_id);

ALTER TABLE notification
    ADD CONSTRAINT FKnk4ftb5am9ubmkv1661h15ds9 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKnk4ftb5am9ubmkv1661h15ds9 ON notification (user_id);

ALTER TABLE assignment_projects
    ADD CONSTRAINT FKnloxk146iq8kydw2vgnjh04oo FOREIGN KEY (assignment_id) REFERENCES assignment (id) ON DELETE NO ACTION;

CREATE INDEX FKnloxk146iq8kydw2vgnjh04oo ON assignment_projects (assignment_id);

ALTER TABLE deadline
    ADD CONSTRAINT FKnw6pwjavqx2fbwe7dy8odvrqd FOREIGN KEY (assignment_id) REFERENCES assignment (id) ON DELETE NO ACTION;

CREATE INDEX FKnw6pwjavqx2fbwe7dy8odvrqd ON deadline (assignment_id);

ALTER TABLE members
    ADD CONSTRAINT FKpj3n6wh5muoeakc485whgs3x5 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKpj3n6wh5muoeakc485whgs3x5 ON members (user_id);

ALTER TABLE feedback
    ADD CONSTRAINT FKpwwmhguqianghvi1wohmtsm8l FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

CREATE INDEX FKpwwmhguqianghvi1wohmtsm8l ON feedback (user_id);

ALTER TABLE assignment
    ADD CONSTRAINT FKrop26uwnbkstbtfha3ormxp85 FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE NO ACTION;

CREATE INDEX FKrop26uwnbkstbtfha3ormxp85 ON assignment (course_id);

ALTER TABLE application
    ADD CONSTRAINT FKrxh04lcvhpj4owpuk43oa0njh FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION;

CREATE INDEX FKrxh04lcvhpj4owpuk43oa0njh ON application (project_id);