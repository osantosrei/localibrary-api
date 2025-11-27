package com.localibrary.controller;

import com.localibrary.dto.WelcomeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "0. Sistema", description = "Informações gerais e saúde da API")
public class HealthCheckController {

    /**
     * ✅ CORREÇÃO RF-01: Página inicial agora retorna informações introdutórias completas
     * Antes: Retornava apenas texto simples
     * Agora: Retorna JSON estruturado com links e documentação
     */
    @Operation(
            summary = "Página Inicial",
            description = "Exibe informações introdutórias do sistema e acesso às funcionalidades principais (RF-01)"
    )
    @GetMapping
    public ResponseEntity<WelcomeDTO> home() {
        Map<String, String> quickLinks = new LinkedHashMap<>();
        quickLinks.put("Buscar Livros", "/livros?titulo=java");
        quickLinks.put("Livros Populares", "/livros/populares");
        quickLinks.put("Mapa de Bibliotecas", "/bibliotecas");
        quickLinks.put("Cadastrar Biblioteca", "/auth/cadastro");
        quickLinks.put("Login", "/auth/login");

        WelcomeDTO welcome = WelcomeDTO.builder()
                .message("Bem-vindo à Localibrary API! 📚")
                .description("Sistema para localização e gestão de bibliotecas em São Paulo. " +
                        "Encontre livros disponíveis nas bibliotecas mais próximas de você.")
                .version("1.0.0")
                .quickLinks(quickLinks)
                .documentation("/swagger-ui/index.html")
                .contact(WelcomeDTO.ContactInfo.builder()
                        .email("dev@localibrary.com")
                        .github("https://github.com/osantosrei/localibrary-api")
                        .build())
                .build();

        return ResponseEntity.ok(welcome);
    }

    /**
     * Endpoint de health check simplificado
     */
    @Operation(summary = "Health Check", description = "Verifica se a API está respondendo")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Localibrary API",
                "version", "1.0.0"
        ));
    }
}
