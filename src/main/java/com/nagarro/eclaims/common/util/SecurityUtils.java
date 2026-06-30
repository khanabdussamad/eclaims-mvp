package com.nagarro.eclaims.common.util;

import com.nagarro.eclaims.common.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityUtils {

    public static Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser()
                .map(CustomUserDetails::getUserId)
                .orElse(null);
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser()
                .map(CustomUserDetails::getUsername)
                .orElse(null);
    }

    public static boolean hasPermission(String permission) {
        return getCurrentUser()
                .map(user -> user.getPermissions().contains(permission))
                .orElse(false);
    }

    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.getRoles().contains(role))
                .orElse(false);
    }
}

