package com.localibrary.controller;

import com.localibrary.dto.BibliotecaParaLivroDTO;
import com.localibrary.dto.LivroDetalhesDTO;
import com.localibrary.dto.response.LivroResponseDTO;
import com.localibrary.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
@Tag(name = "4. Livros", description = "Busca e descoberta de livros (Público)")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @Operation(summary = "Buscar por Título", description = "Pesquisa livros por parte do título (busca textual).")
    @GetMapping
    public ResponseEntity<Page<LivroResponseDTO>> buscarLivros(
            @Parameter(description = "Termo de busca") @RequestParam String titulo,
            @Parameter(description = "Página (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false) Integer size,
            @Parameter(description = "Campo para ordenar (ex: titulo)") @RequestParam(required = false) String sortField,
            @Parameter(description = "Direção da ordenação: ASC ou DESC") @RequestParam(required = false) String sortDir
    ) {
        Page<LivroResponseDTO> result = livroService.buscarLivrosPorTitulo(titulo, page, size, sortField, sortDir);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Livros Populares", description = "Retorna os Top 10 livros com maior disponibilidade no sistema.")
    @GetMapping("/populares")
    public ResponseEntity<List<LivroResponseDTO>> buscarPopulares() {
        return ResponseEntity.ok(livroService.buscarLivrosPopulares());
    }

    @Operation(summary = "Detalhes do Livro", description = "Exibe ficha técnica e recomendações de livros similares.")
    @GetMapping("/{id_livro}")
    public ResponseEntity<LivroDetalhesDTO> verDetalhesLivro(@PathVariable Long id_livro) {
        return ResponseEntity.ok(livroService.buscarDetalhesDoLivro(id_livro));
    }

    @Operation(summary = "Onde Encontrar", description = "Lista bibliotecas que possuem o livro. Se informar lat/lon, ordena por proximidade.")
    @GetMapping("/{id_livro}/bibliotecas")
    public ResponseEntity<List<BibliotecaParaLivroDTO>> verBibliotecasDoLivro(
            @PathVariable Long id_livro,
            @Parameter(description = "Latitude do usuário") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude do usuário") @RequestParam(required = false) Double lon
    ) {
        return ResponseEntity.ok(livroService.buscarBibliotecasPorLivro(id_livro, lat, lon));
    }
}