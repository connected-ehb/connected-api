package com.ehb.connected.domain.impl.users.services;

import com.ehb.connected.domain.impl.enrollments.entities.Enrollment;
import com.ehb.connected.domain.impl.enrollments.repositories.EnrollmentRepository;
import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.tags.entities.Tag;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.dto.AuthUserDetailsDto;
import com.ehb.connected.domain.impl.users.dto.EmailRequestDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import com.ehb.connected.exceptions.AuthenticationRequiredException;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserDetailsMapper userDetailsMapper;
    @Mock private TagMapper tagMapper;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private EmailService emailService;

    @InjectMocks private UserServiceImpl userService;

    @BeforeEach
    void init() {
        // avoids deprecated ReflectionTestUtils by exposing a setter in production class
        ReflectionTestUtils.setField(userService, "frontendUri", "https://frontend.example");
    }

    @Test
    void getAllStudentsByCourseIdReturnsMappedStudents() {
        Long courseId = 42L;
        Enrollment e1 = new Enrollment();
        e1.setCanvasUserId(111L);
        Enrollment e2 = new Enrollment();
        e2.setCanvasUserId(222L);

        User u1 = new User();
        u1.setId(1L);
        u1.setRole(Role.STUDENT);
        User u2 = new User();
        u2.setId(2L);
        u2.setRole(Role.STUDENT);

        UserDetailsDto d1 = new UserDetailsDto();
        d1.setId(1L);
        UserDetailsDto d2 = new UserDetailsDto();
        d2.setId(2L);

        when(enrollmentRepository.findByCourseId(courseId)).thenReturn(List.of(e1, e2));
        when(userRepository.findByCanvasUserIdInAndRole(List.of(111L, 222L), Role.STUDENT))
                .thenReturn(List.of(u1, u2));
        when(userDetailsMapper.toUserDetailsDto(u1)).thenReturn(d1);
        when(userDetailsMapper.toUserDetailsDto(u2)).thenReturn(d2);

        var result = userService.getAllStudentsByCourseId(courseId);

        assertThat(result).containsExactly(d1, d2);
        verify(userDetailsMapper).toUserDetailsDto(u1);
        verify(userDetailsMapper).toUserDetailsDto(u2);
    }

    @Test
    void getUserByIdWhenUserExistsReturnsUser() {
        User user = new User(); user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThat(userService.getUserById(10L)).isSameAs(user);
    }

    @Test
    void getUserByIdWhenMissingThrows() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createUserPersistsEntity() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        var result = userService.createUser(user);

        assertThat(result).isSameAs(user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserPersistsMappedFields() {
        Principal principal = () -> "user@example.com";
        User existing = new User(); existing.setEmail("user@example.com");

        TagDto tagDto = new TagDto(); tagDto.setId(5L); tagDto.setName("Java");
        Tag tagEntity = new Tag(); tagEntity.setId(5L); tagEntity.setName("Java");

        UserDetailsDto dto = new UserDetailsDto();
        dto.setAboutMe("new about");
        dto.setFieldOfStudy("new field");
        dto.setLinkedinUrl("new link");
        dto.setTags(List.of(tagDto));

        UserDetailsDto mappedResponse = new UserDetailsDto(); mappedResponse.setId(1L);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));
        when(tagMapper.toEntityList(dto.getTags())).thenReturn(List.of(tagEntity));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userDetailsMapper.toUserDetailsDto(any())).thenReturn(mappedResponse);

        var result = userService.updateUser(principal, dto);

        assertThat(result).isSameAs(mappedResponse);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getAboutMe()).isEqualTo("new about");
        assertThat(saved.getFieldOfStudy()).isEqualTo("new field");
        assertThat(saved.getLinkedinUrl()).isEqualTo("new link");
        assertThat(saved.getTags()).containsExactly(tagEntity);
    }

    @Test
    void deleteUserDelegatesToRepository() {
        userService.deleteUser(4L);
        verify(userRepository).deleteById(4L);
    }

    @Test
    void getUserByPrincipalWhenPrincipalNullThrows() {
        assertThatThrownBy(() -> userService.getUserByPrincipal(null))
                .isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void getUserByPrincipalCanvasIdResolvesUser() {
        Principal p = () -> "12345";
        User u = new User();
        when(userRepository.findByCanvasUserId(12345L)).thenReturn(Optional.of(u));

        assertThat(userService.getUserByPrincipal(p)).isSameAs(u);
    }

    @Test
    void getUserByPrincipalEmailResolvesUser() {
        Principal p = () -> "user@example.com";
        User u = new User();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(u));

        assertThat(userService.getUserByPrincipal(p)).isSameAs(u);
    }

    @Test
    void getCurrentUserWhenPrincipalNullReturnsNull() {
        assertThat(userService.getCurrentUser(null)).isNull();
    }

    @Test
    void getCurrentUserResolvesDto() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getName()).thenReturn("123");
        User user = new User();
        AuthUserDetailsDto dto = new AuthUserDetailsDto();

        when(userRepository.findByCanvasUserId(123L)).thenReturn(Optional.of(user));
        when(userDetailsMapper.toDtoWithPrincipal(user, principal)).thenReturn(dto);

        assertThat(userService.getCurrentUser(principal)).isSameAs(dto);
    }

    @Test
    void requestDeleteUserStampsTimestamp() {
        Principal p = () -> "user@example.com";
        User u = new User();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.requestDeleteUser(p);

        assertThat(u.getDeleteRequestedAt()).isNotNull();
        verify(userRepository).save(u);
    }

    @Test
    void createEmailVerificationTokenByCanvasIdWhenPrincipalNullThrows() {
        EmailRequestDto dto = new EmailRequestDto();
        dto.setEmail("user@student.ehb.be");

        assertThatThrownBy(() -> userService.createEmailVerificationTokenByCanvasId(null, dto))
                .isInstanceOf(AuthenticationRequiredException.class);

        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    void createEmailVerificationTokenByCanvasIdRejectsNonSchoolEmail() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getName()).thenReturn("456");

        User user = new User();
        when(userRepository.findByCanvasUserId(456L)).thenReturn(Optional.of(user));

        EmailRequestDto dto = new EmailRequestDto();
        dto.setEmail("user@example.com");

        assertThatThrownBy(() -> userService.createEmailVerificationTokenByCanvasId(principal, dto))
                .isInstanceOf(BaseRuntimeException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void createEmailVerificationTokenByCanvasIdThrowsWhenUserMissing() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getName()).thenReturn("111");
        when(userRepository.findByCanvasUserId(111L)).thenReturn(Optional.empty());

        EmailRequestDto dto = new EmailRequestDto();
        dto.setEmail("user@student.ehb.be");

        assertThatThrownBy(() -> userService.createEmailVerificationTokenByCanvasId(principal, dto))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void createEmailVerificationTokenByCanvasIdPersistsTokenAndSendsEmail() {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getName()).thenReturn("789");
        User user = new User();
        when(userRepository.findByCanvasUserId(789L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EmailRequestDto dto = new EmailRequestDto();
        dto.setEmail("user@student.ehb.be");

        userService.createEmailVerificationTokenByCanvasId(principal, dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@student.ehb.be");
        assertThat(saved.getEmailVerificationToken()).isNotBlank();
        assertThat(saved.getEmailVerificationTokenExpiry()).isAfter(LocalDateTime.now().minusSeconds(1));
        assertThat(saved.isEmailVerified()).isFalse();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendEmail(eq("user@student.ehb.be"), eq("Please verify your email"),
                eq("verify-email"), mapCaptor.capture());

        Map<String, Object> variables = mapCaptor.getValue();
        assertThat(variables).containsKey("url");
        assertThat((String) variables.get("url")).contains(saved.getEmailVerificationToken());
    }

    @Test
    void verifyEmailTokenWhenUnknownThrows() {
        when(userRepository.findByEmailVerificationToken("token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmailToken("token"))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("Invalid or expired token");
    }

    @Test
    void verifyEmailTokenWhenExpiredThrows() {
        User user = new User();
        user.setEmail("user@student.ehb.be");
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmailVerificationToken("token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.verifyEmailToken("token"))
                .isInstanceOf(BaseRuntimeException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void verifyEmailTokenAssignsStudentRole() {
        User user = new User();
        user.setEmail("user@student.ehb.be");
        user.setEmailVerificationToken("token");
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmailVerificationToken("token")).thenReturn(Optional.of(user));

        userService.verifyEmailToken("token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getEmailVerificationToken()).isNull();
        assertThat(saved.getEmailVerificationTokenExpiry()).isNull();
    }

    @Test
    void verifyEmailTokenAssignsTeacherRole() {
        User user = new User();
        user.setEmail("teacher@ehb.be");
        user.setEmailVerificationToken("token");
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmailVerificationToken("token")).thenReturn(Optional.of(user));

        userService.verifyEmailToken("token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getRole()).isEqualTo(Role.TEACHER);
        assertThat(saved.isEmailVerified()).isTrue();
    }

    @Test
    void verifyEmailTokenRejectsUnsupportedDomain() {
        User user = new User();
        user.setEmail("user@unsupported.com");
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmailVerificationToken("token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.verifyEmailToken("token"))
                .isInstanceOf(BaseRuntimeException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(userRepository, never()).save(any());
    }
}
