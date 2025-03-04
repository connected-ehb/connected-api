package com.ehb.connected.config;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository; // New dependency for assignments
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedUsers();
        seedCourses();
        seedAssignments(); // Seeding assignments
        seedEnrollments();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            System.out.println("Users already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/userData.json")) {
            List<User> users = objectMapper.readValue(is, new TypeReference<List<User>>() {});
            userRepository.saveAll(users);
            System.out.println("Seeded " + users.size() + " users!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed users", e);
        }
    }

    private void seedCourses() {
        if (courseRepository.count() > 0) {
            System.out.println("Courses already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/courseData.json")) {
            List<Map<String, Object>> courseMaps = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> courseMap : courseMaps) {
                Course course = new Course();
                course.setCanvasId(Long.valueOf(courseMap.get("canvasId").toString()));
                course.setName(courseMap.get("name").toString());
                // For owner: look up using "ownerCanvasUserId" if available.
                if (courseMap.containsKey("ownerCanvasUserId")) {
                    Long ownerCanvasId = Long.valueOf(courseMap.get("ownerCanvasUserId").toString());
                    Optional<User> ownerOpt = userRepository.findByCanvasUserId(ownerCanvasId);
                    ownerOpt.ifPresent(course::setOwner);
                }
                courseRepository.save(course);
            }
            System.out.println("Courses seeded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed courses", e);
        }
    }

    private void seedAssignments() {
        if (assignmentRepository.count() > 0) {
            System.out.println("Assignments already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/assignmentData.json")) {
            List<Map<String, Object>> assignmentsData = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> data : assignmentsData) {
                Long courseCanvasId = Long.valueOf(data.get("CourseCanvasId").toString());
                Optional<Course> courseOpt = courseRepository.findByCanvasId(courseCanvasId);
                if (!courseOpt.isPresent()) {
                    System.err.println("Course with canvasId " + courseCanvasId + " not found. Skipping assignment: " + data.get("name"));
                    continue;
                }
                Assignment assignment = new Assignment();
                assignment.setName(data.get("name").toString());
                assignment.setDescription(data.get("description").toString());
                assignment.setDefaultTeamSize(Integer.parseInt(data.get("defaultTeamSize").toString()));
                assignment.setCourse(courseOpt.get());
                assignmentRepository.save(assignment);
            }
            System.out.println("Assignments seeded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed assignments", e);
        }
    }

    private void seedEnrollments() {
        if (enrollmentRepository.count() > 0) {
            System.out.println("Enrollments already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/enrollmentData.json")) {
            List<Map<String, Object>> enrollmentsData = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> enrollmentData : enrollmentsData) {
                Long canvasUserId = Long.valueOf(enrollmentData.get("canvasUserId").toString());
                Long canvasCourseId = Long.valueOf(enrollmentData.get("canvasCourseId").toString());
                Optional<Course> courseOpt = courseRepository.findByCanvasId(canvasCourseId);
                if (courseOpt.isPresent()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setCanvasUserId(canvasUserId);
                    enrollment.setCourse(courseOpt.get());
                    enrollmentRepository.save(enrollment);
                }
            }
            System.out.println("Enrollments seeded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed enrollments", e);
        }
    }
}
