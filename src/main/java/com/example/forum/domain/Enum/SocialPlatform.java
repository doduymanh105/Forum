package com.example.forum.domain.Enum;

import java.util.Locale;

public enum SocialPlatform {
    FACEBOOK("facebook.com"),
    GITHUB("github.com"),
    LINKEDIN("linkedin.com"),
    TWITTER("twitter.com","x.com"),
    THREADS("threads.com"),
    WEBSITE(""),
    INSTAGRAM("instagram.com"),
    YOUTUBE("youtube.com");

    private final String[] validDomains;

    SocialPlatform(String ... validDomains){
        this.validDomains=validDomains;
    }

    public boolean isValidUrl(String url){
        if(validDomains.length == 0 || validDomains[0].isEmpty()){
            return true;
        }

        String lowerCaseUrl = url.toLowerCase();
        for(String domain : validDomains){
            if(lowerCaseUrl.contains(domain)){
                return true;
            }
        }
        return false;
    }
}
