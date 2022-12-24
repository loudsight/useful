package com.loudsight.useful.web.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

public class SecurityUtils {

    static public String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails)principal).getUsername();
        }
        if (principal instanceof DefaultOAuth2User) {
            return ((DefaultOAuth2User)principal).getName();
        }
        return principal.toString();
    }
}
