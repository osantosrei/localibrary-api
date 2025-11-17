package com.localibrary.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.localibrary.entity.Admin;
import com.localibrary.entity.CredencialBiblioteca;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String senha;
    private final Collection<? extends GrantedAuthority> authorities;

    // Construtor para Admin
    public UserDetailsImpl(Admin admin) {
        this.id = admin.getId();
        this.email = admin.getEmail();
        this.senha = admin.getSenha();
        // Adicionamos o prefixo "ROLE_" que o Spring Security espera
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRoleAdmin().name()));
    }

    // Construtor para Biblioteca
    public UserDetailsImpl(CredencialBiblioteca credenciais) {
        // IMPORTANTE: O ID que guardamos é o da BIBLIOTECA, não o da credencial
        this.id = credenciais.getBiblioteca().getId();
        this.email = credenciais.getEmail();
        this.senha = credenciais.getSenha();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_BIBLIOTECA"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    // Métodos de conta (pode implementar lógica de status PENDENTE/INATIVO aqui)
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; } // RN-02 e RN-06 serão tratados no service
}