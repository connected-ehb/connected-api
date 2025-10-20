package com.ehb.connected.domain.impl.bugs.mappers;

import com.ehb.connected.domain.impl.bugs.dto.BugDetailsDto;
import com.ehb.connected.domain.impl.bugs.entities.Bug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BugMapper {

    public BugDetailsDto toBugDetailsDto(Bug bug) {
        if (bug == null) {
            return null;
        }

        BugDetailsDto dto = new BugDetailsDto();
        dto.setId(bug.getId());
        dto.setDescription(bug.getDescription());
        dto.setRoute(bug.getRoute());
        dto.setAppVersion(bug.getAppVersion());
        dto.setCreatedBy(bug.getCreatedBy());
        dto.setCreatedAt(bug.getCreatedAt());
        return dto;
    }

    public List<BugDetailsDto> toBugDetailsDtoList(List<Bug> bugs) {
        List<BugDetailsDto> bugDtos = new ArrayList<>();
        for (Bug bug : bugs) {
            bugDtos.add(toBugDetailsDto(bug));
        }
        return bugDtos;
    }
}
