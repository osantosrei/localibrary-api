package com.localibrary.enums;

/**
 * Enum que representa o status de uma biblioteca no sistema.
 *
 * ATIVO: Biblioteca aprovada e visível nas buscas públicas
 * INATIVO: Biblioteca bloqueada/desativada temporariamente
 * PENDENTE: Biblioteca aguardando aprovação do admin/moderador (status inicial no cadastro)
 */
public enum StatusBiblioteca {
    ATIVO,
    INATIVO,
    PENDENTE
}