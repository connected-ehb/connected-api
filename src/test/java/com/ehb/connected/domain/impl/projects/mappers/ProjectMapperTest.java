package com.ehb.connected.domain.impl.projects.mappers;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.courses.entities.Course;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.dto.ResearcherProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.service.ProjectUserService;
import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMapperTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserDetailsMapper userDetailsMapper;

    @Mock
    private ProjectUserService projectUserService;

    @InjectMocks
    private ProjectMapper projectMapper;

    private Tag tag;
    private TagDto tagDto;
    private User creator;
    private UserDetailsDto creatorDto;
    private User owner;
    private UserDetailsDto ownerDto;

    @BeforeEach
    void initFixtures() {
        tag = new Tag();
        tag.setId(1L);
        tag.setName("Java");
        tagDto = new TagDto();
        tagDto.setId(1L);
        tagDto.setName("Java");

        creator = new User();
        creator.setId(2L);
        creatorDto = new UserDetailsDto();
        creatorDto.setId(2L);

        owner = new User();
        owner.setId(3L);
        ownerDto = new UserDetailsDto();
        ownerDto.setId(3L);
    }

    @Test
    void toDetailsDtoMapsCompleteProject() {
        Project project = new Project();
        project.setId(10L);
        project.setGid(UUID.randomUUID());
        project.setTitle("Capstone");
        project.setDescription("Desc");
        project.setShortDescription("Short");
        project.setStatus(ProjectStatusEnum.PENDING);
        project.setRepositoryUrl("repo");
        project.setBoardUrl("board");
        project.setBackgroundImage("bg");
        project.setTeamSize(4);
        project.setCreatedBy(creator);
        project.setProductOwner(owner);
        project.setMembers(new ArrayList<>(List.of(owner)));
        project.setTags(new ArrayList<>(List.of(tag)));

        Assignment assignment = new Assignment();
        assignment.setId(77L);
        project.setAssignment(assignment);

        when(tagMapper.toDto(tag)).thenReturn(tagDto);
        when(userDetailsMapper.toUserDetailsDto(creator)).thenReturn(creatorDto);
        when(userDetailsMapper.toUserDetailsDto(owner)).thenReturn(ownerDto);

        ProjectDetailsDto dto = projectMapper.toDetailsDto(project);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo("Capstone");
        assertThat(dto.getAssignmentId()).isEqualTo(77L);
        assertThat(dto.getTags()).containsExactly(tagDto);
        assertThat(dto.getCreatedBy()).isEqualTo(creatorDto);
        assertThat(dto.getProductOwner()).isEqualTo(ownerDto);
        assertThat(dto.getMembers()).containsExactly(ownerDto);

        verify(tagMapper).toDto(tag);
        verify(userDetailsMapper, atLeastOnce()).toUserDetailsDto(creator);
        verify(userDetailsMapper, atLeastOnce()).toUserDetailsDto(owner);
    }

    @Test
    void toDetailsDtoListMapsAllProjects() {
        Project project1 = new Project();
        project1.setId(1L);
        Project project2 = new Project();
        project2.setId(2L);

        List<ProjectDetailsDto> dtos = projectMapper.toDetailsDtoList(List.of(project1, project2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toEntityCopiesDtoValues() {
        ProjectCreateDto dto = new ProjectCreateDto();
        dto.setTitle("Title");
        dto.setDescription("Desc");
        dto.setShortDescription("Short");
        dto.setRepositoryUrl("git");
        dto.setBoardUrl("board");
        dto.setTeamSize(5);
        dto.setBackgroundImage("bg");
        dto.setTags(List.of(tagDto));

        when(tagMapper.toEntityList(dto.getTags())).thenReturn(List.of(tag));

        Project entity = projectMapper.toEntity(dto);

        assertThat(entity.getTitle()).isEqualTo("Title");
        assertThat(entity.getDescription()).isEqualTo("Desc");
        assertThat(entity.getShortDescription()).isEqualTo("Short");
        assertThat(entity.getRepositoryUrl()).isEqualTo("git");
        assertThat(entity.getBoardUrl()).isEqualTo("board");
        assertThat(entity.getTeamSize()).isEqualTo(5);
        assertThat(entity.getBackgroundImage()).isEqualTo("bg");
        assertThat(entity.getTags()).containsExactly(tag);

        verify(tagMapper).toEntityList(dto.getTags());
    }

    @Test
    void updateEntityFromDtoWhenEditableOverwritesMutableFields() {
        ProjectUpdateDto dto = new ProjectUpdateDto();
        dto.setTitle("New Title");
        dto.setDescription("New Desc");
        dto.setShortDescription("New Short");
        dto.setRepositoryUrl("newRepo");
        dto.setBoardUrl("newBoard");
        dto.setBackgroundImage("newBg");
        dto.setTeamSize(6);
        dto.setTags(List.of(tagDto));

        Project entity = new Project();
        entity.setStatus(ProjectStatusEnum.PENDING);
        entity.setTitle("Old Title");
        entity.setDescription("Old Desc");
        entity.setShortDescription("Old Short");
        entity.setRepositoryUrl("oldRepo");
        entity.setBoardUrl("oldBoard");
        entity.setBackgroundImage("oldBg");
        entity.setTeamSize(4);
        entity.setTags(new ArrayList<>(List.of(new Tag())));

        when(tagMapper.toEntityList(dto.getTags())).thenReturn(List.of(tag));

        projectMapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getTitle()).isEqualTo("New Title");
        assertThat(entity.getDescription()).isEqualTo("New Desc");
        assertThat(entity.getShortDescription()).isEqualTo("New Short");
        assertThat(entity.getRepositoryUrl()).isEqualTo("newRepo");
        assertThat(entity.getBoardUrl()).isEqualTo("newBoard");
        assertThat(entity.getBackgroundImage()).isEqualTo("newBg");
        assertThat(entity.getTeamSize()).isEqualTo(6);
        assertThat(entity.getTags()).containsExactly(tag);
    }

    @Test
    void updateEntityFromDtoWhenNotEditableSkipsCoreFieldsButUpdatesAssets() {
        ProjectUpdateDto dto = new ProjectUpdateDto();
        dto.setTitle("Blocked Title");
        dto.setDescription("Blocked Desc");
        dto.setShortDescription("Blocked Short");
        dto.setRepositoryUrl("newRepo");
        dto.setBoardUrl("newBoard");
        dto.setBackgroundImage("newBg");
        dto.setTeamSize(9);
        dto.setTags(List.of(tagDto));

        Project entity = new Project();
        entity.setStatus(ProjectStatusEnum.APPROVED);
        entity.setTitle("Original Title");
        entity.setDescription("Original Desc");
        entity.setShortDescription("Original Short");
        entity.setRepositoryUrl("oldRepo");
        entity.setBoardUrl("oldBoard");
        entity.setBackgroundImage("oldBg");
        entity.setTeamSize(4);
        entity.setTags(new ArrayList<>(List.of(new Tag())));

        when(tagMapper.toEntityList(dto.getTags())).thenReturn(List.of(tag));

        projectMapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getTitle()).isEqualTo("Original Title");
        assertThat(entity.getDescription()).isEqualTo("Original Desc");
        assertThat(entity.getShortDescription()).isEqualTo("Original Short");
        assertThat(entity.getTeamSize()).isEqualTo(4);
        assertThat(entity.getRepositoryUrl()).isEqualTo("newRepo");
        assertThat(entity.getBoardUrl()).isEqualTo("newBoard");
        assertThat(entity.getBackgroundImage()).isEqualTo("newBg");
        assertThat(entity.getTags()).containsExactly(tag);
    }

    @Test
    void toResearcherDetailsDtoAddsCourseAndAssignmentNames() {
        Project project = new Project();
        project.setId(5L);
        project.setTags(new ArrayList<>());
        project.setMembers(new ArrayList<>());

        Assignment assignment = new Assignment();
        assignment.setName("AI Project");
        Course course = new Course();
        course.setName("Machine Learning");
        assignment.setCourse(course);
        project.setAssignment(assignment);

        ResearcherProjectDetailsDto dto = projectMapper.toResearcherDetailsDto(project);

        assertThat(dto.getCourseName()).isEqualTo("Machine Learning");
        assertThat(dto.getAssignmentName()).isEqualTo("AI Project");
        assertThat(dto.getId()).isEqualTo(5L);
    }
}






