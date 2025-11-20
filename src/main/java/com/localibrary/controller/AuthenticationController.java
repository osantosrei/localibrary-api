package com.localibrary.controller;

import com.localibrary.dto.BibliotecaRegistrationDTO;
import com.localibrary.dto.request.LoginRequestDTO;
import com.localibrary.dto.response.LoginResponseDTO;
import com.localibrary.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(
            @Valid @RequestBody LoginRequestDTO loginRequest
    ) {
        LoginResponseDTO response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    // Endpoint de Cadastro (RF-08)
    @PostMapping("/cadastro")
    public ResponseEntity<Void> registerBiblioteca(
            @Valid @RequestBody BibliotecaRegistrationDTO registrationDTO
    ) {
        authenticationService.registerBiblioteca(registrationDTO);
        // Retorna 201 Created (padrão para criação de recurso)
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}