package com.ehb.connected.domain.impl.projects.entities;

public enum ProjectStatusEnum {
    PENDING,        // Initial state
    NEEDS_REVISION, // Teacher requested changes
    REVISED,        // Student resubmitted with changes
    APPROVED,       // Teacher approved
    PUBLISHED,      // Visible to others
    REJECTED        // Final negative outcome
}
