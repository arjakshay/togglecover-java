package com.togglecover.insurance.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    public static Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object userIdAttr = request.getAttribute("userId");
                if (userIdAttr instanceof Long) {
                    return (Long) userIdAttr;
                } else if (userIdAttr instanceof Integer) {
                    return ((Integer) userIdAttr).longValue();
                } else if (userIdAttr instanceof String) {
                    try {
                        return Long.parseLong((String) userIdAttr);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            // Log if needed
        }
        return null;
    }

    public static String getCurrentUsername() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object usernameAttr = request.getAttribute("username");
                if (usernameAttr instanceof String) {
                    return (String) usernameAttr;
                }
            }
        } catch (Exception e) {
            // Log if needed
        }
        return null;
    }
}