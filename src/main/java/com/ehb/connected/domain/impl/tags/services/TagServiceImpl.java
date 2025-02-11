package com.ehb.connected.domain.impl.tags.services;

import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return null;
    }

    @Override
    public List<Tag> searchTags(String query) {
        log.warn("Searching for tags with query: {}", query);
        log.warn("found tags: {}", tagRepository.findByNameContainingIgnoreCase(query));
        return tagRepository.findByNameContainingIgnoreCase(query);

    }

    @Override
    public Tag createTag(Tag tag) {
        return null;
    }

    @Override
    public Tag updateTag(Tag tag) {
        return null;
    }

    @Override
    public void deleteTag(Long id) {

    }

    @Override
    public Tag getTagById(Long id) {
        return null;
    }
}
