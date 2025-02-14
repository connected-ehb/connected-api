package com.ehb.connected.domain.impl.tags.services;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagService {

    List<TagDto> searchTagsByQuery(String query);
    TagDto createTag(TagDto tag);
}
