package com.ehb.connected.domain.impl.tags;
import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.tags.services.TagServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tags")
public class TagController {
    private final TagServiceImpl tagService;
    private final TagMapper tagMapper;

    private static final Logger logger = Logger.getLogger(TagController.class.getName());

    public TagController(TagServiceImpl tagServiceImpl, TagMapper tagMapper) {
        this.tagService = tagServiceImpl;
        this.tagMapper = tagMapper;
    }

    //implement the dto mapper so that the response is a list of TagDto objects instead of an endless nested json object
    @GetMapping("/search")
    public ResponseEntity<List<TagDto>> searchTags(@RequestParam String query) {
        List<Tag> tags = tagService.searchTags(query);
        // Convert tags to DTOs
        List<TagDto> tagDTOs = tags.stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tagDTOs);
    }
}