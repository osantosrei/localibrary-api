package com.localibrary.service;

import com.localibrary.dto.request.LoginRequestDTO;
import com.localibrary.dto.response.LoginResponseDTO;
import com.localibrary.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Cria o objeto de autenticação
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getSenha()
                )
        );

        // Seta a autenticação no contexto de segurança
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Gera o token JWT
        String token = jwtTokenService.generateToken(authentication);

        return new LoginResponseDTO(token, "Bearer", 86400L); // 24h em segundos
    }
}