package com.nagarro.eclaims.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final boolean enabled;

    public CustomUserDetails(UUID userId, String email, String password,
                            Set<String> roles, Set<String> permissions, boolean enabled) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.roles = roles != null ? roles : new HashSet<>();
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add permission authorities (permissions are the main authorities)
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        // Also add role authorities for backward compatibility
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

