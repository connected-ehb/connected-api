package com.ehb.connected.domain.impl.tags.services;

import com.ehb.connected.domain.impl.tags.entities.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagService {

    List<Tag> getAllTags();
    List<Tag> searchTags(String query);
    Tag createTag(Tag tag);
    Tag updateTag(Tag tag);
    void deleteTag(Long id);
    Tag getTagById(Long id);
}
