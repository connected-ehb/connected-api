package com.ehb.connected.domain.impl.notifications.helpers;
import org.springframework.stereotype.Component;

@Component
public  class UrlHelper {

    public static String Sluggify(String input) {
        // Replace spaces with hyphens
        String slug = input.replace(" ", "-");
        //to lowercase
        slug = slug.toLowerCase();
        // Replace multiple consecutive hyphens with a single hyphen
        slug = slug.replaceAll("-+", "-");
        // Trim trailing hyphens
        slug = slug.replaceAll("-$", "");
        return slug;
    }

    public static String UrlBuilder(String... parts){
        StringBuilder url = new StringBuilder();
        for (String part : parts) {
           if(!url.isEmpty()){
                url.append("/");
           }
            url.append(Sluggify(part));
        }
        return url.toString();
    }

}
