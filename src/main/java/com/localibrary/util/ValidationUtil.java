package com.localibrary.util;

import java.util.regex.Pattern;

/**
 * Classe utilitária para validações customizadas.
 * Complementa as validações do Bean Validation.
 */
public class ValidationUtil {

    /**
     * Valida formato de email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_EMAIL, email);
    }

    /**
     * Valida formato de CNPJ (com pontuação)
     */
    public static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_CNPJ, cnpj);
    }

    /**
     * Valida formato de CEP
     */
    public static boolean isValidCEP(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_CEP, cep);
    }

    /**
     * Valida formato de telefone
     */
    public static boolean isValidTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_TELEFONE, telefone);
    }

    /**
     * Valida formato de ISBN (13 dígitos)
     */
    public static boolean isValidISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(Constants.REGEX_ISBN, isbn);
    }

    /**
     * Valida comprimento de senha
     */
    public static boolean isValidSenha(String senha) {
        if (senha == null) {
            return false;
        }
        return senha.length() >= Constants.MIN_SENHA_LENGTH &&
                senha.length() <= Constants.MAX_SENHA_LENGTH;
    }

    /**
     * Remove pontuação do CNPJ (deixa apenas números)
     */
    public static String sanitizeCNPJ(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        return cnpj.replaceAll("[^0-9]", "");
    }

    /**
     * Remove pontuação do CEP
     */
    public static String sanitizeCEP(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replaceAll("[^0-9]", "");
    }

    /**
     * Remove pontuação do telefone
     */
    public static String sanitizeTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        return telefone.replaceAll("[^0-9]", "");
    }

    /**
     * Formata CNPJ (adiciona pontuação)
     * Input: 12345678000199
     * Output: 12.345.678/0001-99
     */
    public static String formatCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return String.format("%s.%s.%s/%s-%s",
                cnpj.substring(0, 2),
                cnpj.substring(2, 5),
                cnpj.substring(5, 8),
                cnpj.substring(8, 12),
                cnpj.substring(12, 14)
        );
    }

    /**
     * Formata CEP (adiciona hífen)
     * Input: 01310100
     * Output: 01310-100
     */
    public static String formatCEP(String cep) {
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return String.format("%s-%s",
                cep.substring(0, 5),
                cep.substring(5, 8)
        );
    }

    /**
     * Verifica se string está vazia ou nula
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Verifica se string não está vazia
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Valida se coordenadas geográficas são válidas
     */
    public static boolean isValidCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    /**
     * Valida se ano de publicação é válido (entre 1000 e ano atual + 1)
     */
    public static boolean isValidAnoPublicacao(Integer ano) {
        if (ano == null) {
            return false;
        }
        int anoAtual = java.time.Year.now().getValue();
        return ano >= 1000 && ano <= (anoAtual + 1);
    }

    // Construtor privado
    private ValidationUtil() {
        throw new IllegalStateException("Classe utilitária não deve ser instanciada");
    }
}