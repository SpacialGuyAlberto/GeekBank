package com.geekbank.bank.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
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
        return username;
    }

    // Implementa los otros métodos de UserDetails según sea necesario

    @Override
    public boolean isAccountNonExpired() {
        return true; // Aquí puedes implementar la lógica de expiración de la cuenta si es necesario
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Aquí puedes implementar la lógica de bloqueo de la cuenta si es necesario
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Aquí puedes implementar la lógica de expiración de credenciales si es necesario
    }

    @Override
    public boolean isEnabled() {
        return true; // Aquí puedes implementar la lógica para habilitar/deshabilitar la cuenta si es necesario
    }
}
