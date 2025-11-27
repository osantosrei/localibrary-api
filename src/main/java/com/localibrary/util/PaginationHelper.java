package com.localibrary.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * ✅ NOVO: Helper para centralizar lógica de paginação
 * Resolve: Código duplicado em múltiplos services
 * Implementa: Princípio DRY (Don't Repeat Yourself)
 */
public abstract class PaginationHelper {

    /**
     * Cria um objeto Pageable com valores padrão se os parâmetros forem nulos/inválidos
     *
     * @param page Número da página (0-based)
     * @param size Tamanho da página
     * @param sortField Campo para ordenação
     * @param sortDir Direção da ordenação (ASC ou DESC)
     * @return Pageable configurado
     */
    public static Pageable createPageable(Integer page, Integer size, String sortField, String sortDir) {
        // Define valores padrão
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? Constants.DEFAULT_PAGE_SIZE : Math.min(size, Constants.MAX_PAGE_SIZE);
        String sf = (sortField == null || sortField.isBlank()) ? Constants.DEFAULT_SORT_FIELD : sortField;
        String sd = (sortDir == null || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC")))
                ? Constants.DEFAULT_SORT_DIRECTION
                : sortDir.toUpperCase();

        // Cria Sort
        Sort sort = Sort.by(Sort.Direction.fromString(sd), sf);

        // Retorna Pageable
        return PageRequest.of(p, s, sort);
    }

    /**
     * Cria um Pageable com ordenação padrão (ID ASC)
     *
     * @param page Número da página
     * @param size Tamanho da página
     * @return Pageable configurado
     */
    public static Pageable createPageable(Integer page, Integer size) {
        return createPageable(page, size, null, null);
    }

    /**
     * Cria um Pageable com valores padrão (página 0, tamanho padrão)
     *
     * @return Pageable configurado com valores padrão
     */
    public static Pageable createDefaultPageable() {
        return createPageable(0, Constants.DEFAULT_PAGE_SIZE, null, null);
    }

    // Construtor privado para evitar instanciação
    private PaginationHelper() {
        throw new IllegalStateException("Classe utilitária não deve ser instanciada");
    }
}
