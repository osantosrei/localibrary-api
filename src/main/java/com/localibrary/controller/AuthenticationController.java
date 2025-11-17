package com.localibrary.controller;

import com.localibrary.dto.request.LoginRequestDTO;
import com.localibrary.dto.response.LoginResponseDTO;
import com.localibrary.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(
            @Valid @RequestBody LoginRequestDTO loginRequest
    ) {
        LoginResponseDTO response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    // O endpoint /auth/cadastro (RF-08) será implementado na Sprint 2
}