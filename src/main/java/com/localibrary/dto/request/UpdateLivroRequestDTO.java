// ==========================================
// 2. UpdateLivroRequestDTO.java
// DTO para PATCH /bibliotecas/{id}/livros/{id_livro}
// ==========================================
package com.localibrary.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

import static com.localibrary.util.Constants.MSG_ISBN_INVALIDO;
import static com.localibrary.util.Constants.REGEX_ISBN;

/**
 * ? NOVO: DTO para atualizar livro completo
 * Permite atualizar todos os campos editáveis
 */
@Data
public class UpdateLivroRequestDTO {

    @Pattern(regexp = REGEX_ISBN, message = MSG_ISBN_INVALIDO)
    private String isbn;

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    private String autor;

    private String editora;

    @Min(value = 1000, message = "Ano de publicação inválido")
    private Integer anoPublicacao;

    private String capa;

    private String resumo;

    private String fotoAutor;

    @NotEmpty(message = "Pelo menos um gênero deve ser selecionado")
    private Set<Long> generosIds;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    private Integer quantidade;
}