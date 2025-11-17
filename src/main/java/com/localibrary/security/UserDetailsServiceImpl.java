package com.localibrary.security;

import com.localibrary.entity.Admin;
import com.localibrary.entity.CredencialBiblioteca;
import com.localibrary.repository.AdminRepository;
import com.localibrary.repository.CredencialBibliotecaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CredencialBibliotecaRepository credenciaisRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tenta encontrar como Admin
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            return new UserDetailsImpl(admin.get());
        }

        // 2. Tenta encontrar como Biblioteca
        Optional<CredencialBiblioteca> credenciais = credenciaisRepository.findByEmail(email);
        if (credenciais.isPresent()) {
            return new UserDetailsImpl(credenciais.get());
        }

        // 3. Não encontrou
        throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
    }
}