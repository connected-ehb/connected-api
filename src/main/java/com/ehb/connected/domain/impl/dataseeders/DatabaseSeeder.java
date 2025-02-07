package com.ehb.connected.domain.impl.dataseeders;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.repositories.DeadlineRepository;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.feedbacks.repositories.FeedbackRepository;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.repositories.TagRepository;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final ApplicationRepository applicationRepository;
    private final FeedbackRepository feedbackRepository;
    private final TagRepository tagRepository;
    private final DeadlineRepository deadlineRepository;


   /* @EventListener
    public void seed(ContextRefreshedEvent event) {
        try{
            seedUsers();
            seedCourses();
            seedAssignments();
            seedTags();
            seedProjects();
            seedDeadlines();
            seedApplications();
            seedFeedbacks();
            logger.info("Database seeding completed.");
        } catch (Exception e) {
            logger.error("Error while seeding database: ", e);
        }
    }*/


    //TODO fix user seeding
    private void seedUsers() {

    }
    private void seedCourses() {
        if (courseRepository.count() == 0) {
            LocalDateTime startDate = LocalDateTime.of(2024, 9, 18, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2025, 9, 1, 0, 0);
            Course course1 = new Course(1L,null, "Final Work", startDate ,endDate , null);
            Course course2 = new Course(2L,null, "IT Project", startDate , endDate , null);

            courseRepository.saveAllAndFlush(List.of(course1, course2));
            logger.info("Courses Seeded");
        }
    }

    private void seedAssignments() {
        if (assignmentRepository.count() == 0) {
            List<Course> courses = courseRepository.findAll();
            if (courses.isEmpty()) return;

            Assignment assignment1 = new Assignment(null, "Final Work Assignment", LocalDateTime.now(), "Final project for students", 5, courses.get(0), new ArrayList<>(), new ArrayList<>());
            Assignment assignment2 = new Assignment(null, "IT Project Assignment", LocalDateTime.now(), "Group project for IT students", 3, courses.get(1), new ArrayList<>(), new ArrayList<>());

            assignmentRepository.saveAll(List.of(assignment1, assignment2));
            logger.info("Assignments Seeded");
        }
    }

    private void seedTags() {
        if (tagRepository.count() == 0) {
            Tag tag1 = new Tag(null, "Java", new ArrayList<>(), new ArrayList<>());
            Tag tag2 = new Tag(null, "Spring Boot", new ArrayList<>(), new ArrayList<>());
            Tag tag3 = new Tag(null, "AI", new ArrayList<>(), new ArrayList<>());

            tagRepository.saveAll(List.of(tag1, tag2, tag3));
            logger.info("Tags Seeded");
        }
    }

    private void seedProjects() {
        if (projectRepository.count() == 0) {
            User owner = userRepository.findByEmail("john.doe@student.ehb.be")
                    .orElseThrow(() -> new RuntimeException("User John Doe not found!"));
            User member = userRepository.findByEmail("thomas.detrein@student.ehb.be")
                    .orElseThrow(() -> new RuntimeException("User Thomas not found!"));

            logger.info("Using owner {} with ID: {}", owner.getEmail(), owner.getId());
            logger.info("Using member {} with ID: {}", member.getEmail(), member.getId());
            List<Assignment> assignments = assignmentRepository.findAll();
            List<Tag> tags = tagRepository.findAll();

            if (assignments.isEmpty() || tags.isEmpty()) {
                logger.warn("Skipping projects seeding: dependencies are missing.");
                return;
            }

            Assignment assignment = assignments.get(0);
            Project project1 = new Project(null, "Task Management App", "An **app** to manage tasks efficiently.",
                    ProjectStatusEnum.PENDING, "https://github.com/example/taskapp", "https://trello.com/taskapp",
                    null, assignment, tags, owner, new ArrayList<>(), List.of(owner, member));

            Project project2 = new Project(null, "Farm Management System", "A **system to manage resources** on the farm.",
                    ProjectStatusEnum.APPROVED, "https://github.com/example/farmapp", "https://trello.com/farmapp",
                    null, assignment, List.of(tags.get(0)), owner, new ArrayList<>(), List.of(owner));

            projectRepository.saveAllAndFlush(List.of(project1, project2));
            logger.info("Projects Seeded");
        }
    }

    private void seedDeadlines() {
        if (deadlineRepository.count() == 0) {
            List<Assignment> assignments = assignmentRepository.findAll();
            if (assignments.isEmpty()) return;

            Deadline deadline1 = new Deadline(null, "Prototype Submission", LocalDateTime.now().plusDays(10), "Submit the first prototype", "Hard Deadline", assignments.get(0));
            Deadline deadline2 = new Deadline(null, "Final Submission", LocalDateTime.now().plusDays(30), "Submit the final version", "Final", assignments.get(0));

            deadlineRepository.saveAll(List.of(deadline1, deadline2));
            logger.info("Deadlines Seeded");
        }
    }

    private void seedApplications() {
        if (applicationRepository.count() == 0) {
            List<Project> projects = projectRepository.findAll();
            if (projects.isEmpty()) return;

            Application app1 = new Application(null, "I am very passionate about this project!", projects.get(0));
            Application app2 = new Application(null, "I have experience in Java and would love to join.", projects.get(0));
            Application app3 = new Application(null, "I am an AI enthusiast and want to contribute!", projects.get(1));

            applicationRepository.save(app1);
            applicationRepository.save(app2);
            applicationRepository.save(app3);

            applicationRepository.flush();

            logger.info("Applications Seeded");
        }
    }

    private void seedFeedbacks() {
        if (feedbackRepository.count() == 0) {
            List<Project> projects = projectRepository.findAll();
            if (projects.isEmpty()) return;

            Feedback feedback1 = new Feedback(null, "Great project idea!", projects.get(0));
            Feedback feedback2 = new Feedback(null, "Consider improving the UI.", projects.get(0));
            Feedback feedback3 = new Feedback(null, "The project structure is well-organized.", projects.get(1));

            feedbackRepository.saveAllAndFlush(List.of(feedback1, feedback2, feedback3));
            logger.info("Feedbacks Seeded");
        }
    }
}
