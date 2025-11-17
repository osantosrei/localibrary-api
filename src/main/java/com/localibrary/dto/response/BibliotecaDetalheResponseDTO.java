package com.localibrary.dto.response;

import com.localibrary.entity.Biblioteca;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de resposta com detalhes completos de uma biblioteca.
 * Inclui lista de livros disponíveis.
 *
 * RF-07: Detalhes da biblioteca
 * RF-10: Listar livros da biblioteca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BibliotecaDetalheResponseDTO extends BibliotecaResponseDTO {

    private List<LivroComQuantidadeDTO> livros;

    /**
     * Construtor a partir da entidade Biblioteca
     */
    public BibliotecaDetalheResponseDTO(Biblioteca biblioteca) {
        super(biblioteca);

        if (biblioteca.getLivros() != null && !biblioteca.getLivros().isEmpty()) {
            this.livros = biblioteca.getLivros().stream()
                    .map(bl -> new LivroComQuantidadeDTO(
                            new LivroResponseDTO(bl.getLivroBase()),
                            bl.getQuantidade()
                    ))
                    .collect(Collectors.toList());
        } else {
            this.livros = new ArrayList<>();
        }
    }

    /**
     * DTO interno para representar livro com quantidade
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivroComQuantidadeDTO {
        private LivroResponseDTO livro;
        private Integer quantidade;
    }
}