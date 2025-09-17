package lt.nsg.scroogebank.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public enum UserRoles {
    user(UserRole.ROLE_USER), operator(UserRole.ROLE_OPERATOR);
    private final Collection<GrantedAuthority> roles;

    UserRoles(String role) {
        this.roles = Collections.singleton(new SimpleGrantedAuthority(role));
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }
}
