package com.ehb.connected.config;

import com.ehb.connected.domain.impl.announcements.entities.Announcement;
import com.ehb.connected.domain.impl.announcements.repositories.AnnouncementRepository;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.courses.repositories.CourseRepository;
import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final AnnouncementRepository announcementRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
//        seedUsers();
//        seedCourses();
//        seedAssignments(); // Seeding assignments
//        seedProjects();    // Seeding projects
//        seedEnrollments();
//        seedAnnouncements();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            System.out.println("Users already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/mockData/userData.json")) {
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
        try (InputStream is = getClass().getResourceAsStream("/mockData/courseData.json")) {
            List<Map<String, Object>> courseMaps = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> courseMap : courseMaps) {
                Course course = new Course();
                course.setCanvasId(Long.valueOf(courseMap.get("canvasId").toString()));
                course.setName(courseMap.get("name").toString());
                // Look up owner using "ownerCanvasUserId" if available.
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
        try (InputStream is = getClass().getResourceAsStream("/mockData/assignmentData.json")) {
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
                assignment.setCanvasId(Long.valueOf(data.get("canvasId").toString()));
                assignment.setDescription(data.get("description").toString());
                assignment.setDefaultTeamSize(Integer.parseInt(data.get("defaultTeamSize").toString()));
                assignment.setCourse(courseOpt.get());
                // Optionally, set canvasId for the assignment if provided.
                assignmentRepository.save(assignment);
            }
            System.out.println("Assignments seeded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed assignments", e);
        }
    }

    private void seedProjects() {
        if (projectRepository.count() > 0) {
            System.out.println("Projects already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/mockData/projectData.json")) {
            List<Map<String, Object>> projectsData = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> data : projectsData) {
                Project project = new Project();
                project.setGid(UUID.randomUUID());

                // Title
                if (data.containsKey("title")) {
                    project.setTitle(data.get("title").toString());
                } else {
                    project.setTitle("");
                }

                // Description (use empty string if not provided)
                project.setDescription(data.containsKey("description") ? data.get("description").toString() : "");

                // Short Description
                if (data.containsKey("shortDescription")) {
                    project.setShortDescription(data.get("shortDescription").toString());
                } else {
                    project.setShortDescription("");
                }

                // Status
                if (data.containsKey("status")) {
                    project.setStatus(ProjectStatusEnum.valueOf(data.get("status").toString()));
                }

                // Repository URL
                if (data.containsKey("repositoryUrl")) {
                    project.setRepositoryUrl(data.get("repositoryUrl").toString());
                } else {
                    project.setRepositoryUrl("");
                }

                // Board URL
                if (data.containsKey("boardUrl")) {
                    project.setBoardUrl(data.get("boardUrl").toString());
                } else {
                    project.setBoardUrl("");
                }

                // Team Size
                if (data.containsKey("teamSize")) {
                    project.setTeamSize(Integer.parseInt(data.get("teamSize").toString()));
                } else {
                    project.setTeamSize(0);
                }

                // Lookup Assignment using assignmentCanvasId
                if (data.containsKey("assignmentCanvasId")) {
                    Long assignmentCanvasId = Long.valueOf(data.get("assignmentCanvasId").toString());
                    Optional<Assignment> assignmentOpt = assignmentRepository.findByCanvasId(assignmentCanvasId);
                    if (assignmentOpt.isEmpty()) {
                        System.err.println("Assignment with canvasId " + assignmentCanvasId + " not found. Skipping project: " + data.get("title"));
                        continue;
                    }
                    project.setAssignment(assignmentOpt.get());
                }

                // Lookup createdBy user using createdByCanvasId
                if (data.containsKey("createdByCanvasId")) {
                    Long createdByCanvasId = Long.valueOf(data.get("createdByCanvasId").toString());
                    Optional<User> createdByOpt = userRepository.findByCanvasUserId(createdByCanvasId);
                    createdByOpt.ifPresent(project::setCreatedBy);
                }

                // Lookup productOwner user using productOwnerCanvasId
                if (data.containsKey("productOwnerCanvasId")) {
                    Long productOwnerCanvasId = Long.valueOf(data.get("productOwnerCanvasId").toString());
                    Optional<User> productOwnerOpt = userRepository.findByCanvasUserId(productOwnerCanvasId);
                    productOwnerOpt.ifPresent(project::setProductOwner);
                }

                // Set members from membersCanvasIds array
                if (data.containsKey("membersCanvasIds")) {
                    List<?> membersIds = (List<?>) data.get("membersCanvasIds");
                    List<User> members = new ArrayList<>();
                    for (Object memberIdObj : membersIds) {
                        Long memberCanvasId = Long.valueOf(memberIdObj.toString());
                        Optional<User> memberOpt = userRepository.findByCanvasUserId(memberCanvasId);
                        memberOpt.ifPresent(members::add);
                    }
                    project.setMembers(members);
                }

                // Save the project
                projectRepository.save(project);
            }
            System.out.println("Projects seeded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed projects", e);
        }
    }


    private void seedEnrollments() {
        if (enrollmentRepository.count() > 0) {
            System.out.println("Enrollments already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/mockData/enrollmentData.json")) {
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

    private void seedAnnouncements() {
        if (announcementRepository.count() > 0) {
            System.out.println("Announcements already seeded, skipping.");
            return;
        }
        try (InputStream is = getClass().getResourceAsStream("/mockData/announcementData.json")) {
            List<Map<String, Object>> announcementsData = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> data : announcementsData) {
                Announcement announcement = new Announcement();
                announcement.setTitle(data.get("title").toString());
                announcement.setMessage(data.get("message").toString());

                // Lookup Assignment using assignmentCanvasId
                if (data.containsKey("assignmentCanvasId")) {
                    Long assignmentCanvasId = Long.valueOf(data.get("assignmentCanvasId").toString());
                    Optional<Assignment> assignmentOpt = assignmentRepository.findByCanvasId(assignmentCanvasId);
                    if (assignmentOpt.isPresent()) {
                        announcement.setAssignment(assignmentOpt.get());
                    } else {
                        System.err.println("Assignment with canvasId " + assignmentCanvasId + " not found. Skipping announcement: " + data.get("title"));
                        continue;
                    }
                }

                // Lookup User using canvasUserId for createdBy
                if (data.containsKey("canvasUserId")) {
                    Long canvasUserId = Long.valueOf(data.get("canvasUserId").toString());
                    Optional<User> userOpt = userRepository.findByCanvasUserId(canvasUserId);
                    if (userOpt.isPresent()) {
                        announcement.setCreatedBy(userOpt.get());
                    } else {
                        System.err.println("User with canvasUserId " + canvasUserId + " not found. Skipping announcement: " + data.get("title"));
                        continue;
                    }
                }

                announcementRepository.save(announcement);
            }
            System.out.println("Announcements seeded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed announcements", e);
        }
    }
}
