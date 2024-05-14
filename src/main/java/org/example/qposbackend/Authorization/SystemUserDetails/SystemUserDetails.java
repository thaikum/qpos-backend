package org.example.qposbackend.Authorization.SystemUserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.qposbackend.Authorization.User.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class SystemUserDetails implements UserDetails {
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole().getPrivileges().stream().map(privilege -> new SimpleGrantedAuthority(privilege.getPrivilege())).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        if (user.getPasswords() != null && !user.getPasswords().isEmpty()) {
            return (user.getPasswords().get(user.getPasswords().size() - 1)).getPassword();
        } else {
            return null;
        }
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        return user.getEnabled();
    }

}
