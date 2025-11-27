package com.localibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ✅ NOVO: DTO para RF-01 (Página inicial com informações introdutórias)
 * Substitui o texto simples por um JSON estruturado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeDTO {

    /**
     * Mensagem de boas-vindas
     */
    private String message;

    /**
     * Descrição do sistema
     */
    private String description;

    /**
     * Versão da API
     */
    private String version;

    /**
     * Links para os principais recursos
     */
    private Map<String, String> quickLinks;

    /**
     * Link para documentação Swagger
     */
    private String documentation;

    /**
     * Informações de contato
     */
    private ContactInfo contact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private String email;
        private String github;
    }
}
