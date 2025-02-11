package com.ehb.connected.domain.impl.tags.mappers;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }
        return new TagDto(tag.getId(), tag.getName());
    }
}