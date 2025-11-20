package com.localibrary.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

import static com.localibrary.util.Constants.MSG_ISBN_INVALIDO;
import static com.localibrary.util.Constants.REGEX_ISBN;

@Data
public class AddLivroRequestDTO {

    @Pattern(regexp = REGEX_ISBN, message = MSG_ISBN_INVALIDO)
    private String isbn;

    @NotBlank
    private String titulo;

    @NotBlank
    private String autor;

    private String editora;

    private Integer anoPublicacao;

    private String capa;

    private String resumo;

    // Parte 2: Gêneros (tbl_livro_genero)
    @NotEmpty // Deve ter pelo menos um gênero
    private Set<Long> generosIds; // Lista de IDs de gêneros (de tbl_genero)

    // Parte 3: Inventário (tbl_biblioteca_livro)
    @NotNull
    @Min(1)
    private int quantidade;
}