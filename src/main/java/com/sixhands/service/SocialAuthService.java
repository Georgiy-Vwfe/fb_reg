package com.sixhands.service;

import com.sixhands.domain.User;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;

@Service
public class SocialAuthService {
    private static final String AUTH_DETAILS_NAME_PARAM = "name";
    private static final String AUTH_DETAILS_EMAIL_PARAM = "email";

    public User extractUserFromAuthInfo(@NonNull Principal principal) {
        if (principal instanceof OAuth2Authentication) {
            return extractExternalUser(principal);
        }
        return null;
    }

    private User extractExternalUser(@NonNull Principal principal) {
        OAuth2Authentication oAuth = (OAuth2Authentication) principal;
        Map<String, String> details = (Map<String, String>) oAuth.getUserAuthentication().getDetails();

        String extIdStr;
        extIdStr = (String) oAuth.getUserAuthentication().getPrincipal();
        User user = new User();
        user.setUuid(11111L);
        user.setFirst_name(details.get(AUTH_DETAILS_NAME_PARAM));
        user.setEmail(AUTH_DETAILS_EMAIL_PARAM);
        return user;
    }

}
