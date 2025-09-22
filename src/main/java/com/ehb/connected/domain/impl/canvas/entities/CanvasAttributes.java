package com.ehb.connected.domain.impl.canvas.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasAttributes {
    private Long id;
    private String name;
    private ZonedDateTime createdAt;
    private String sortableName;
    private String shortName;
    private String avatarUrl;
    private String lastName;
    private String firstName;
    private String locale;
    private String effectiveLocale;
    private CanvasPermission permissions;

    /**
     * Creates a CanvasAttributes object from the OAuth2 user attributes map
     * @param attributes The attributes map from OAuth2User
     * @return CanvasAttributes object
     */
    public static CanvasAttributes fromOAuth2Attributes(Map<String, Object> attributes) {
        CanvasAttributes canvasAttributes = new CanvasAttributes();
        
        // Handle the id (it comes as Integer from Canvas)
        Object idObj = attributes.get("id");
        if (idObj instanceof Integer) {
            canvasAttributes.setId(((Integer) idObj).longValue());
        } else if (idObj instanceof Long) {
            canvasAttributes.setId((Long) idObj);
        }
        
        canvasAttributes.setName((String) attributes.get("name"));
        canvasAttributes.setSortableName((String) attributes.get("sortable_name"));
        canvasAttributes.setShortName((String) attributes.get("short_name"));
        canvasAttributes.setAvatarUrl((String) attributes.get("avatar_url"));
        canvasAttributes.setLastName((String) attributes.get("last_name"));
        canvasAttributes.setFirstName((String) attributes.get("first_name"));
        canvasAttributes.setLocale((String) attributes.get("locale"));
        canvasAttributes.setEffectiveLocale((String) attributes.get("effective_locale"));
        
        // Handle created_at timestamp
        Object createdAtObj = attributes.get("created_at");
        if (createdAtObj instanceof String) {
            try {
                canvasAttributes.setCreatedAt(ZonedDateTime.parse((String) createdAtObj));
            } catch (Exception e) {
                // Log warning but don't fail the authentication
                System.err.println("Could not parse created_at: " + createdAtObj);
            }
        }
        
        // Handle permissions
        Object permissionsObj = attributes.get("permissions");
        if (permissionsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> permissionsMap = (Map<String, Object>) permissionsObj;
            CanvasPermission permissions = new CanvasPermission();
            permissions.setCanUpdateName((Boolean) permissionsMap.get("can_update_name"));
            permissions.setCanUpdateAvatar((Boolean) permissionsMap.get("can_update_avatar"));
            permissions.setLimitParentAppWebAccess((Boolean) permissionsMap.get("limit_parent_app_web_access"));
            canvasAttributes.setPermissions(permissions);
        }
        
        return canvasAttributes;
    }
}
