package com.ehb.connected.domain.impl.applications.mappers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationMapperTest {

    @Mock private UserDetailsMapper userDetailsMapper;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks private ApplicationMapper applicationMapper;

    @Test
    void toDtoMapsAllFields() {
        Project project = new Project();
        User user = new User();
        Application application = new Application(99L, "Motivation", ApplicationStatusEnum.PENDING, project, user);

        ProjectDetailsDto projectDto = new ProjectDetailsDto();
        projectDto.setId(44L);
        UserDetailsDto userDto = new UserDetailsDto();
        userDto.setId(55L);

        when(projectMapper.toDetailsDto(project)).thenReturn(projectDto);
        when(userDetailsMapper.toUserDetailsDto(user)).thenReturn(userDto);

        ApplicationDetailsDto dto = applicationMapper.toDto(application);

        assertThat(dto.getId()).isEqualTo(99L);
        assertThat(dto.getMotivationMd()).isEqualTo("Motivation");
        assertThat(dto.getStatus()).isEqualTo(ApplicationStatusEnum.PENDING);
        assertThat(dto.getProject()).isSameAs(projectDto);
        assertThat(dto.getApplicant()).isSameAs(userDto);
        verify(projectMapper).toDetailsDto(project);
        verify(userDetailsMapper).toUserDetailsDto(user);
    }

    @Test
    void toDtoListMapsCollection() {
        Project project = new Project();
        User user = new User();
        Application application = new Application(99L, "Motivation", ApplicationStatusEnum.PENDING, project, user);

        ProjectDetailsDto projectDto = new ProjectDetailsDto();
        UserDetailsDto userDto = new UserDetailsDto();
        when(projectMapper.toDetailsDto(project)).thenReturn(projectDto);
        when(userDetailsMapper.toUserDetailsDto(user)).thenReturn(userDto);

        List<ApplicationDetailsDto> dtos = applicationMapper.toDtoList(List.of(application));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getId()).isEqualTo(99L);
        verify(projectMapper).toDetailsDto(project);
        verify(userDetailsMapper).toUserDetailsDto(user);
    }
}
