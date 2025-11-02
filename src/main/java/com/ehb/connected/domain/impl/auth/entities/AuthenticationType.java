package com.ehb.connected.domain.impl.auth.entities;

/**
 * Enum representing the type of authentication used by a user.
 */
public enum AuthenticationType {
    /**
     * User authenticated via OAuth2 (Canvas LMS).
     */
    OAUTH2,

    /**
     * User authenticated via traditional form-based login (email/password).
     */
    FORM
}
