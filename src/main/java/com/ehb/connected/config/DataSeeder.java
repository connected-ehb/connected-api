package com.ehb.connected.config;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedUsers();
        seedCourses();
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
            // Since your Course JSON contains an extra field "ownerCanvasUserId",
            // we first read it as a list of maps.
            List<Map<String, Object>> courseMaps = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> courseMap : courseMaps) {
                Course course = new Course();
                // Map the properties from JSON to the Course entity.
                course.setCanvasId(Long.valueOf(courseMap.get("canvasId").toString()));
                course.setName(courseMap.get("name").toString());
                // Optionally, set other properties such as startAt, endAt, etc.

                // For owner: use the provided "ownerCanvasUserId" to look up a User.
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
                // Find the course by canvasId
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
