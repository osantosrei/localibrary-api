package com.localibrary.exception;

/**
 * Exceção lançada quando há falha de autenticação ou token inválido.
 * Retorna HTTP 401 (Unauthorized).
 *
 * Exemplos de uso:
 * - Token JWT inválido ou expirado (RN-08)
 * - Credenciais inválidas no login
 * - Token não fornecido em rota protegida
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Construtor com mensagem simples
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}