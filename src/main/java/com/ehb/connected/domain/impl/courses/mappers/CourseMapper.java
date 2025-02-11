package com.ehb.connected.domain.impl.courses.mappers;

import com.ehb.connected.domain.impl.courses.dto.CourseCreateDto;
import com.ehb.connected.domain.impl.courses.dto.CourseDetailsDto;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseMapper {
    private final UserServiceImpl userService;

    public Course CourseCreateToEntity(CourseCreateDto courseCreateDto, Principal principal) {
        Course course = new Course();
        course.setName(courseCreateDto.getName());
        course.setUuid(courseCreateDto.getUuid());
        course.setStartAt(courseCreateDto.getStartAt());
        course.setEndAt(courseCreateDto.getEndAt());
        course.setAssignments(new ArrayList<>());
        course.setOwner(userService.getUserByEmail(principal.getName()));
        course.setCanvasCourseId(courseCreateDto.getCanvasCourseId());
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
        courseDto.setCanvasCourseId(course.getCanvasCourseId());
        return courseDto;
    }

    public List<CourseDetailsDto> toCourseDetailsDtoList(List<Course> courses) {
        List<CourseDetailsDto> courseDtos = new ArrayList<>();
        for (Course course : courses) {
            courseDtos.add(toCourseDetailsDto(course));
        }
        return courseDtos;
    }
}
