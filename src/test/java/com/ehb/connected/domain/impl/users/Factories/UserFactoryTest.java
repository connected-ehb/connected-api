package com.ehb.connected.domain.impl.users.Factories;

import com.ehb.connected.domain.impl.canvas.entities.CanvasAttributes;
import com.ehb.connected.domain.impl.users.entities.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserFactoryTest {

    private final UserFactory factory = new UserFactory();

    @Test
    void newCanvasUserCopiesCanvasAttributes() {
        CanvasAttributes attrs = new CanvasAttributes();
        attrs.setId(55L);
        attrs.setFirstName("Jane");
        attrs.setLastName("Doe");
        attrs.setProfileImageUrl("https://canvas");

        User user = factory.newCanvasUser(attrs);

        assertThat(user.getCanvasUserId()).isEqualTo(55L);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://canvas");
        assertThat(user.getEmail()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.isEmailVerified()).isFalse();
    }
}
