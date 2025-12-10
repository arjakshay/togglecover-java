package com.togglecover.insurance.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        // In your implementation, you might want to store user ID in JWT claims
        // For now, extracting from username (assuming username is email or contains ID)
        String username = getCurrentUsername();
        if (username != null) {
            // Extract user ID from JWT claims or decode from username
            // This is a simplified example
            try {
                // Assuming username is in format "user_{id}" or contains ID
                if (username.startsWith("user_")) {
                    return Long.parseLong(username.substring(5));
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}