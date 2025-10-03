package com.ehb.connected.domain.impl.users.Factories;

import com.ehb.connected.domain.impl.canvas.entities.CanvasAttributes;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {
    public User newCanvasUser(CanvasAttributes attrs) {
        User user = new User();
        user.setCanvasUserId(attrs.getId());
        user.setFirstName(attrs.getFirstName());
        user.setLastName(attrs.getLastName());
        user.setProfileImageUrl(attrs.getProfileImageUrl());

        user.setEmail(null);
        user.setRole(null);
        user.setEmailVerified(false);

        return user;
    }
}
