package com.localibrary.controller;

import com.localibrary.dto.request.CadastroBibliotecaRequestDTO;
import com.localibrary.dto.request.LoginRequestDTO;
import com.localibrary.dto.response.MensagemResponseDTO;
import com.localibrary.dto.response.TokenResponseDTO;
import com.localibrary.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticação.
 * Endpoints públicos para login e cadastro.
 *
 * RF-08: Cadastro de biblioteca
 * RF-09: Login de biblioteca
 * RF-15: Login de admin/moderador
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Endpoint de login.
     * Aceita login de bibliotecas, admins e moderadores.
     *
     * RF-09: Login de biblioteca
     * RF-15: Login de admin/moderador
     * RN-07: Rota pública
     * RN-08: Retorna JWT válido
     *
     * @param request DTO com email e senha
     * @return TokenResponseDTO com token JWT e dados do usuário
     *
     * Exemplo de request:
     * POST /auth/login
     * {
     *   "email": "biblioteca@exemplo.com",
     *   "senha": "senha123"
     * }
     *
     * Exemplo de response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tipo": "Bearer",
     *   "id": 1,
     *   "email": "biblioteca@exemplo.com",
     *   "tipoUsuario": "BIBLIOTECA",
     *   "role": "ROLE_BIBLIOTECA",
     *   "nome": "Biblioteca Municipal de São Paulo"
     * }
     *
     * Erros possíveis:
     * - 400: Dados inválidos (validação)
     * - 401: Credenciais inválidas ou usuário inativo
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {

        logger.info("Tentativa de login: {}", request.getEmail());

        TokenResponseDTO response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de cadastro de biblioteca.
     * Cria nova biblioteca com status PENDENTE (aguardando aprovação).
     *
     * RF-08: Cadastro de biblioteca
     * RN-07: Rota pública
     * RN-12: Valida endereço único
     * RN-17: Valida endereço via geolocalização (Sprint 3)
     *
     * @param request DTO com dados completos da biblioteca
     * @return MensagemResponseDTO confirmando cadastro
     *
     * Exemplo de request:
     * POST /auth/cadastro
     * {
     *   "nomeFantasia": "Biblioteca Municipal",
     *   "razaoSocial": "Biblioteca Municipal de São Paulo LTDA",
     *   "cnpj": "12.345.678/0001-99",
     *   "telefone": "(11) 98765-4321",
     *   "categoria": "PUBLICA",
     *   "site": "https://biblioteca.sp.gov.br",
     *   "email": "contato@biblioteca.sp.gov.br",
     *   "senha": "senha123",
     *   "endereco": {
     *     "cep": "01310-100",
     *     "logradouro": "Avenida Paulista",
     *     "numero": "1000",
     *     "complemento": "Sala 10",
     *     "bairro": "Bela Vista",
     *     "cidade": "São Paulo",
     *     "estado": "SP"
     *   }
     * }
     *
     * Exemplo de response (201 Created):
     * {
     *   "mensagem": "Cadastro realizado com sucesso! Sua biblioteca está aguardando aprovação do administrador.",
     *   "timestamp": "2025-01-15T10:30:00"
     * }
     *
     * Erros possíveis:
     * - 400: Dados inválidos, cidade não é São Paulo
     * - 409: CNPJ ou email já cadastrado
     * - 500: Erro na validação de endereço (geolocalização)
     */
    @PostMapping("/cadastro")
    public ResponseEntity<MensagemResponseDTO> cadastrarBiblioteca(
            @Valid @RequestBody CadastroBibliotecaRequestDTO request) {

        logger.info("Tentativa de cadastro: {} (CNPJ: {})",
                request.getNomeFantasia(), request.getCnpj());

        authService.cadastrarBiblioteca(request);

        MensagemResponseDTO response = new MensagemResponseDTO(
                "Cadastro realizado com sucesso! " +
                        "Sua biblioteca está aguardando aprovação do administrador."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de teste (opcional).
     * Verifica se a API está rodando.
     *
     * @return Mensagem de boas-vindas
     */
    @GetMapping("/health")
    public ResponseEntity<MensagemResponseDTO> health() {
        return ResponseEntity.ok(
                new MensagemResponseDTO("Localibrary API - Authentication Service is running!")
        );
    }
}