package com.ehb.connected.domain.impl.canvas.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CanvasPermission {
    private Boolean limitParentAppWebAccess;
    private Boolean canUpdateAvatar;
    private Boolean canUpdateName;
}
