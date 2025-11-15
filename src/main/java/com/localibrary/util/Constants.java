package com.localibrary.util;

/**
 * Classe com constantes utilizadas em toda a aplicação.
 * Centraliza valores fixos para facilitar manutenção.
 */
public class Constants {

    // ============================================
    // CONSTANTES DE LOCALIZAÇÃO (RN-11, RN-13)
    // ============================================
    public static final String CIDADE_SAO_PAULO = "São Paulo";
    public static final Double DEFAULT_LATITUDE = -23.5505;
    public static final Double DEFAULT_LONGITUDE = -46.6333;

    // ============================================
    // CONSTANTES DE STATUS HTTP
    // ============================================
    public static final String MSG_SUCESSO = "Operação realizada com sucesso";
    public static final String MSG_ERRO_GENERICO = "Ocorreu um erro ao processar a solicitação";
    public static final String MSG_NAO_ENCONTRADO = "Recurso não encontrado";
    public static final String MSG_NAO_AUTORIZADO = "Acesso não autorizado";
    public static final String MSG_PROIBIDO = "Acesso proibido";
    public static final String MSG_CONFLITO = "Conflito de dados";
    public static final String MSG_DADOS_INVALIDOS = "Dados inválidos";

    // ============================================
    // CONSTANTES DE VALIDAÇÃO
    // ============================================
    public static final int MIN_SENHA_LENGTH = 6;
    public static final int MAX_SENHA_LENGTH = 255;
    public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String REGEX_CNPJ = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$";
    public static final String REGEX_CEP = "^\\d{5}-\\d{3}$";
    public static final String REGEX_TELEFONE = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$";
    public static final String REGEX_ISBN = "^\\d{13}$";

    // ============================================
    // MENSAGENS DE VALIDAÇÃO
    // ============================================
    public static final String MSG_EMAIL_INVALIDO = "Email inválido";
    public static final String MSG_CNPJ_INVALIDO = "CNPJ inválido (formato: 00.000.000/0000-00)";
    public static final String MSG_CEP_INVALIDO = "CEP inválido (formato: 00000-000)";
    public static final String MSG_TELEFONE_INVALIDO = "Telefone inválido";
    public static final String MSG_ISBN_INVALIDO = "ISBN inválido (deve ter 13 dígitos)";
    public static final String MSG_SENHA_CURTA = "Senha deve ter no mínimo " + MIN_SENHA_LENGTH + " caracteres";
    public static final String MSG_CAMPO_OBRIGATORIO = "Campo obrigatório";

    // ============================================
    // CONSTANTES DE NEGÓCIO
    // ============================================
    public static final int LIMITE_LIVROS_POPULARES = 10;
    public static final int LIMITE_LIVROS_SIMILARES = 5;
    public static final int QUANTIDADE_MINIMA_LIVRO = 1;

    // ============================================
    // CONSTANTES DE JWT (serão usadas na Sprint 2)
    // ============================================
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_CLAIM_ROLE = "role";
    public static final String JWT_CLAIM_ID = "id";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_TIPO = "tipo"; // BIBLIOTECA ou ADMIN

    // ============================================
    // CONSTANTES DE ROLES
    // ============================================
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MODERADOR = "ROLE_MODERADOR";
    public static final String ROLE_BIBLIOTECA = "ROLE_BIBLIOTECA";

    // ============================================
    // CONSTANTES DE ARQUIVO
    // ============================================
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png"};
    public static final String UPLOAD_DIR = "uploads/";

    // ============================================
    // CONSTANTES DE PAGINAÇÃO
    // ============================================
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    // Construtor privado para evitar instanciação
    private Constants() {
        throw new IllegalStateException("Classe utilitária não deve ser instanciada");
    }
}