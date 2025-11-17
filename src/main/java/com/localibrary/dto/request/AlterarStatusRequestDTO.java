package com.localibrary.dto.request;

import com.localibrary.enums.StatusBiblioteca;
import com.localibrary.util.Constants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para alteração de status de biblioteca.
 *
 * RF-18: Bloquear/desbloquear biblioteca aprovada
 * RF-20: Aprovar/reprovar biblioteca pendente
 * RN-03: Apenas ADMIN/MODERADOR pode alterar status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlterarStatusRequestDTO {

    @NotNull(message = Constants.MSG_CAMPO_OBRIGATORIO)
    private StatusBiblioteca novoStatus;

    /**
     * Motivo da alteração (opcional para ATIVO, obrigatório para INATIVO)
     */
    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    private String motivo;
}