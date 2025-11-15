package com.localibrary.exception;

/**
 * Exceção para regras de negócio violadas.
 * Retorna HTTP 400 (Bad Request) ou 409 (Conflict).
 *
 * Exemplos de uso:
 * - CNPJ já cadastrado (RN-XX)
 * - Email já em uso
 * - Biblioteca não pode ser aprovada (status inválido)
 * - Livro já está no acervo da biblioteca
 * - Quantidade inválida de livros
 */
public class BusinessException extends RuntimeException {

    private String code;

    /**
     * Construtor com mensagem simples
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e código de erro
     *
     * @param message Mensagem de erro
     * @param code Código de erro (ex: "CNPJ_DUPLICADO", "EMAIL_JA_CADASTRADO")
     */
    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
    }

    /**
     * Construtor com mensagem e causa
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }
}