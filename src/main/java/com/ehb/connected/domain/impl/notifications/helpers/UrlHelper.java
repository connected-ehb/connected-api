package com.ehb.connected.domain.impl.notifications.helpers;
import org.springframework.stereotype.Component;

@Component
public  class UrlHelper {

    public static String sluggify(String input) {
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

    public static String buildCourseAssignmentUrl(String courseName, String assignmentName, String... parts){
        return urlBuilder(concatenateArrays(new String[]{"course", courseName, "assignment", assignmentName}, parts));
    }

    private static String[] concatenateArrays(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static String urlBuilder(String... parts){
        StringBuilder url = new StringBuilder();
        for (String part : parts) {
           if(!url.isEmpty()){
                url.append("/");
           }
            url.append(sluggify(part));
        }
        return url.toString();
    }

}
