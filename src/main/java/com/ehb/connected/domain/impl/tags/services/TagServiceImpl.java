package com.ehb.connected.domain.impl.tags.services;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.tags.repositories.TagRepository;
import com.ehb.connected.exceptions.EntityAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public List<TagDto> searchTagsByQuery(String query) {
        return tagRepository.findByNameContainingIgnoreCase(query).stream().map(tagMapper::toDto).toList();
    }

    @Override
    public TagDto createTag(TagDto tag) {
        // check if name already exists
        if (tagRepository.existsByNameIgnoreCase(tag.getName().trim())) {
            throw new EntityAlreadyExistsException("Tag with name: '" + tag.getName() + "' already exists");
        }
        return tagMapper.toDto(tagRepository.save(tagMapper.toEntity(tag)));
    }
}
