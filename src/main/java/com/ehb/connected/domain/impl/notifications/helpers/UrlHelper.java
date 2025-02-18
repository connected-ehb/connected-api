package com.ehb.connected.domain.impl.notifications.helpers;
import org.springframework.stereotype.Component;

@Component
public  class UrlHelper {

    public static String Sluggify(String input){
        return input.toLowerCase().replace(" ", "-");
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
