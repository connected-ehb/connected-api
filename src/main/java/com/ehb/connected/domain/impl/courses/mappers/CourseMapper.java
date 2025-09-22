package com.ehb.connected.domain.impl.courses.mappers;

import com.ehb.connected.domain.impl.assignments.mappers.AssignmentMapper;
import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CourseMapper {
    private final UserServiceImpl userService;
    private final AssignmentMapper assignmentMapper;

    public Course CourseCreateToEntity(CourseCreateDto courseCreateDto, Principal principal) {
        Course course = new Course();
        course.setName(courseCreateDto.getName());
        course.setUuid(courseCreateDto.getUuid());
        course.setStartAt(courseCreateDto.getStartAt());
        course.setEndAt(courseCreateDto.getEndAt());
        course.setAssignments(new ArrayList<>());
        course.setOwner(userService.getUserFromAnyPrincipal(principal));
        course.setCanvasId(courseCreateDto.getCanvasId());
        return course;
    }

    public CourseDetailsDto toCourseDetailsDto(Course course) {
        CourseDetailsDto courseDto = new CourseDetailsDto();
        courseDto.setId(course.getId());
        courseDto.setName(course.getName());
        courseDto.setUuid(course.getUuid());
        courseDto.setStartAt(course.getStartAt());
        courseDto.setEndAt(course.getEndAt());
        courseDto.setOwnerId(course.getOwner().getId());
        courseDto.setCanvasId(course.getCanvasId());
        if (course.getAssignments() != null) {
            courseDto.setAssignments(assignmentMapper.toAssignmentDetailsDtoList(course.getAssignments()));
        }
        return courseDto;
    }

    public List<CourseDetailsDto> toCourseDetailsDtoList(List<Course> courses) {
        List<CourseDetailsDto> courseDtos = new ArrayList<>();
        for (Course course : courses) {
            courseDtos.add(toCourseDetailsDto(course));
        }
        return courseDtos;
    }

    public CourseDetailsDto fromCanvasMapToCourseDetailsDto(Map<String, Object> canvasCourseMap) {
        CourseDetailsDto courseDto = new CourseDetailsDto();
        courseDto.setCanvasId(Long.parseLong(canvasCourseMap.get("id").toString()));
        courseDto.setName(canvasCourseMap.get("name").toString());
        if (canvasCourseMap.containsKey("start_at") && canvasCourseMap.get("start_at") != null) {
            courseDto.setStartAt(LocalDateTime.parse(canvasCourseMap.get("start_at").toString()));
        }
        if (canvasCourseMap.containsKey("end_at") && canvasCourseMap.get("end_at") != null) {
            courseDto.setEndAt(LocalDateTime.parse(canvasCourseMap.get("end_at").toString()));
        }
        return courseDto;
    }
}
