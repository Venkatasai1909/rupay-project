package com.rupay.forex.service;

import com.rupay.forex.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoUserDetails implements UserDetails {
    private Long id;
    private String name;
    private String password;
    private List<GrantedAuthority> authorities;
    public UserInfoUserDetails(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.password = user.getPassword();
        this.authorities = user.getRoles().
                           stream().map(role -> new SimpleGrantedAuthority("ROLE_"+role)).
                            collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
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
        return true;
    }
}