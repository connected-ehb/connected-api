package com.ehb.connected.domain.impl.users.services;

import com.ehb.connected.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@example.com");
    }

    @Test
    void sendEmailBuildsTemplateAndDispatchesMessage() throws Exception {
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>body</html>");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("user@example.com", "Subject", "template", Map.of("key", "value"));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("template"), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertThat(context.getVariable("key")).isEqualTo("value");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getFrom()[0].toString()).isEqualTo("noreply@example.com");
        assertThat(sentMessage.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Subject");

        Object content = sentMessage.getContent();
        assertThat(content).isInstanceOf(MimeMultipart.class);
    }

    @Test
    void sendEmailWrapsMessagingExceptions() throws MessagingException {
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>body</html>");
        MimeMessage failingMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(failingMessage);
        doThrow(new MessagingException("boom"))
                .when(failingMessage).setSubject(any(String.class));

        assertThatThrownBy(() -> emailService.sendEmail("user@example.com", "Subject", "template", Map.of()))
                .isInstanceOf(EmailSendingException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
