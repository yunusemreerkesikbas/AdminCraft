package com.backend.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.domain.port.CurrentUserPort;

@Component
public class CurrentUserPortImpl implements CurrentUserPort {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    @Override
    public boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (ROLE_SUPER_ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
