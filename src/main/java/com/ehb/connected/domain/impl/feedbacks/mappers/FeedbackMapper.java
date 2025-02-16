package com.ehb.connected.domain.impl.feedbacks.mappers;

import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedbackMapper {

    private final UserDetailsMapper userMapper;
    private final ProjectMapper projectMapper;

    public FeedbackDto toDto(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        FeedbackDto dto = new FeedbackDto();
        dto.setId(feedback.getId());
        dto.setComment(feedback.getComment());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());

        if (feedback.getUser() != null) {
            dto.setUser(userMapper.toUserDetailsDto(feedback.getUser()));
        }

        if (feedback.getProject() != null) {
            dto.setProject(projectMapper.toDetailsDto(feedback.getProject()));
        }

        return dto;
    }

    public List<FeedbackDto> toDtoList(List<Feedback> feedbacks) {
        return feedbacks.stream()
                .map(this::toDto)
                .toList();
    }
}
