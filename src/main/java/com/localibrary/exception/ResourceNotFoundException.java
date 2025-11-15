package com.localibrary.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado no banco de dados.
 * Retorna HTTP 404 (Not Found).
 *
 * Exemplos de uso:
 * - Biblioteca não encontrada
 * - Livro não encontrado
 * - Admin não encontrado
 */
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    /**
     * Construtor com mensagem simples
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor com detalhes do recurso não encontrado
     *
     * @param resourceName Nome do recurso (ex: "Biblioteca", "Livro")
     * @param fieldName Nome do campo usado na busca (ex: "id", "isbn")
     * @param fieldValue Valor do campo usado na busca
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s não encontrado(a) com %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}