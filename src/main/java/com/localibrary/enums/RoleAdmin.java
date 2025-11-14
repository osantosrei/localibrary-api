package com.localibrary.enums;

/**
 * Enum que representa os papéis/funções dos administradores no sistema.
 *
 * ADMIN: Acesso total ao sistema, pode gerenciar moderadores
 * MODERADOR: Acesso para aprovar/reprovar bibliotecas, mas não pode gerenciar outros admins
 */
public enum RoleAdmin {
    ADMIN,
    MODERADOR
}