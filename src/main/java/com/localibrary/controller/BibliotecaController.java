package com.localibrary.controller;

import com.localibrary.dto.*;
import com.localibrary.dto.request.AddLivroRequestDTO;
import com.localibrary.dto.response.BibliotecaResponseDTO;
import com.localibrary.service.BibliotecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bibliotecas")
@Tag(name = "3. Bibliotecas", description = "Busca pública e gestão de perfil/acervo")
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    // --- PÚBLICO ---

    /**
     * ✅ CORREÇÃO RF-04: Mapa de Bibliotecas agora com PAGINAÇÃO
     * Antes: Retornava lista completa (ineficiente para muitas bibliotecas)
     * Agora: Retorna Page<BibliotecaResponseDTO> com suporte a paginação
     */
    @Operation(summary = "Mapa de Bibliotecas", description = "Lista bibliotecas ATIVAS para exibição no mapa. Suporta paginação (RF-04).")
    @GetMapping
    public ResponseEntity<Page<BibliotecaResponseDTO>> listarBibliotecas(
            @Parameter(description = "Página (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false) Integer size,
            @Parameter(description = "Campo para ordenar (ex: nomeFantasia)") @RequestParam(required = false) String sortField,
            @Parameter(description = "Direção da ordenação: ASC ou DESC") @RequestParam(required = false) String sortDir
    ) {
        return ResponseEntity.ok(bibliotecaService.listarBibliotecasAtivas(page, size, sortField, sortDir));
    }

    @Operation(summary = "Detalhes da Biblioteca", description = "Exibe informações públicas (endereço, contato) de uma biblioteca.")
    @GetMapping("/{id_biblioteca}")
    public ResponseEntity<BibliotecaDetalhesDTO> verDetalhesBiblioteca(@PathVariable Long id_biblioteca) {
        return ResponseEntity.ok(bibliotecaService.buscarDetalhesBiblioteca(id_biblioteca));
    }

    // --- PROTEGIDO (Requer Token da Própria Biblioteca) ---

    @Operation(summary = "Meu Perfil", description = "Retorna dados para edição. Requer token da própria biblioteca.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id_biblioteca}/profile")
    public ResponseEntity<BibliotecaDetalhesDTO> getMyProfile(@PathVariable Long id_biblioteca) {
        BibliotecaDetalhesDTO dto = bibliotecaService.getMyBibliotecaDetails(id_biblioteca);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Atualizar Perfil", description = "Atualiza dados cadastrais e endereço.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id_biblioteca}")
    public ResponseEntity<BibliotecaDetalhesDTO> updateMyProfile(
            @PathVariable Long id_biblioteca,
            @Valid @RequestBody UpdateBibliotecaDTO dto
    ) {
        BibliotecaDetalhesDTO updatedDto = bibliotecaService.updateMyBiblioteca(id_biblioteca, dto);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Listar Meu Acervo", description = "Lista os livros da biblioteca logada.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id_biblioteca}/livros")
    public ResponseEntity<Page<LivroAcervoDTO>> getMyLivros(
            @PathVariable Long id_biblioteca,
            @Parameter(description = "Página (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false) Integer size,
            @Parameter(description = "Campo para ordenar (ex: titulo)") @RequestParam(required = false) String sortField,
            @Parameter(description = "Direção da ordenação: ASC ou DESC") @RequestParam(required = false) String sortDir
    ) {
        Page<LivroAcervoDTO> livros = bibliotecaService.listMyLivros(id_biblioteca, page, size, sortField, sortDir);
        return ResponseEntity.ok(livros);
    }

    @Operation(summary = "Adicionar Livro", description = "Adiciona um livro ao acervo. Se o livro não existir no sistema, ele é criado.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id_biblioteca}/livros")
    public ResponseEntity<LivroAcervoDTO> addLivroToMyAcervo(
            @PathVariable Long id_biblioteca,
            @Valid @RequestBody AddLivroRequestDTO dto
    ) {
        LivroAcervoDTO newLivro = bibliotecaService.addLivroToMyAcervo(id_biblioteca, dto);
        return new ResponseEntity<>(newLivro, HttpStatus.CREATED);
    }

    @Operation(summary = "Remover Livro", description = "Remove um livro do acervo.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id_biblioteca}/livros/{id_livro}")
    public ResponseEntity<Void> removeLivroFromMyAcervo(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro
    ) {
        bibliotecaService.removeLivroFromMyAcervo(id_biblioteca, id_livro);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Estoque", description = "Altera a quantidade de exemplares de um livro.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id_biblioteca}/livros/{id_livro}")
    public ResponseEntity<LivroAcervoDTO> updateQuantidade(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro,
            @Valid @RequestBody UpdateQuantidadeDTO dto
    ) {
        LivroAcervoDTO updatedLivro = bibliotecaService.updateQuantidadeLivro(id_biblioteca, id_livro, dto);
        if (updatedLivro == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(updatedLivro);
    }
}
