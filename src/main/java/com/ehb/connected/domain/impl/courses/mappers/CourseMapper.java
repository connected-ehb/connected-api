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
        course.setStart_at(courseCreateDto.getStart_at());
        course.setEnd_at(courseCreateDto.getEnd_at());
        course.setAssignments(new ArrayList<>());
        course.setOwner(userService.getUserByEmail(principal.getName()));
        return course;
    }

    public CourseDetailsDto toCourseDetailsDto(Course course, Principal principal) {
        CourseDetailsDto courseDto = new CourseDetailsDto();
        courseDto.setName(course.getName());
        courseDto.setUuid(course.getUuid());
        courseDto.setStart_at(course.getStart_at());
        courseDto.setEnd_at(course.getEnd_at());
        courseDto.setOwner_id(course.getOwner().getId());
        return courseDto;
    }

    public List<CourseDetailsDto> toCourseDetailsDtoList(List<Course> courses, Principal principal) {
        List<CourseDetailsDto> courseDtos = new ArrayList<>();
        for (Course course : courses) {
            courseDtos.add(toCourseDetailsDto(course, principal));
        }
        return courseDtos;
    }
}
