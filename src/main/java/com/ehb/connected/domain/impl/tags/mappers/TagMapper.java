package com.ehb.connected.domain.impl.tags.mappers;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TagMapper {

    public TagDto toDto(Tag tag) {
        TagDto tagDto = new TagDto();
        tagDto.setId(tag.getId());
        tagDto.setName(tag.getName());
        return tagDto;
    }

    public Tag toEntity(TagDto tagDto) {
        Tag tag = new Tag();
        tag.setId(tagDto.getId());
        tag.setName(tagDto.getName());
        return tag;
    }

    public List<TagDto> toDtoList(List<Tag> tags) {
        return tags.stream().map(this::toDto).toList();
    }

    public List<Tag> toEntityList(List<TagDto> tags) {
        return tags.stream().map(this::toEntity).toList();
    }
}