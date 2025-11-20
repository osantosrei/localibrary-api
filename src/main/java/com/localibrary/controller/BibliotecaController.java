package com.localibrary.controller;

import com.localibrary.dto.*;
import com.localibrary.dto.request.AddLivroRequestDTO;
import com.localibrary.dto.response.BibliotecaResponseDTO;
import com.localibrary.service.BibliotecaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bibliotecas")
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    // RF-04: Exibir um mapa com todas as bibliotecas ATIVAS
    @GetMapping
    public ResponseEntity<List<BibliotecaResponseDTO>> listarBibliotecas() {
        return ResponseEntity.ok(bibliotecaService.listarBibliotecasAtivas());
    }

    // RF-07: Exibir detalhes completos de uma biblioteca
    @GetMapping("/{id_biblioteca}")
    public ResponseEntity<BibliotecaDetalhesDTO> verDetalhesBiblioteca(@PathVariable Long id_biblioteca) {
        return ResponseEntity.ok(bibliotecaService.buscarDetalhesBiblioteca(id_biblioteca));
    }

    /**
     * RF-13: Pega dados da biblioteca logada para o formulário de edição
     */
    @GetMapping("/{id_biblioteca}/profile") // Rota dedicada e protegida
    public ResponseEntity<BibliotecaDetalhesDTO> getMyProfile(@PathVariable Long id_biblioteca) {
        // O service vai checar a permissão (RN-01)
        BibliotecaDetalhesDTO dto = bibliotecaService.getMyBibliotecaDetails(id_biblioteca);
        return ResponseEntity.ok(dto);
    }

    /**
     * RF-14: Envia a atualização do perfil da biblioteca logada
     */
    @PutMapping("/{id_biblioteca}") // Rota protegida
    public ResponseEntity<BibliotecaDetalhesDTO> updateMyProfile(
            @PathVariable Long id_biblioteca,
            @Valid @RequestBody UpdateBibliotecaDTO dto
    ) {
        // O service vai checar a permissão (RN-01)
        BibliotecaDetalhesDTO updatedDto = bibliotecaService.updateMyBiblioteca(id_biblioteca, dto);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * RF-10: Listar todos os livros do acervo da biblioteca
     */
    @GetMapping("/{id_biblioteca}/livros") // Rota protegida
    public ResponseEntity<List<LivroAcervoDTO>> getMyLivros(@PathVariable Long id_biblioteca) {
        // O service vai checar a permissão (RN-01)
        List<LivroAcervoDTO> livros = bibliotecaService.listMyLivros(id_biblioteca);
        return ResponseEntity.ok(livros);
    }

    /**
     * RF-11: Adicionar um novo livro ao acervo
     */
    @PostMapping("/{id_biblioteca}/livros") // Rota protegida
    public ResponseEntity<LivroAcervoDTO> addLivroToMyAcervo(
            @PathVariable Long id_biblioteca,
            @Valid @RequestBody AddLivroRequestDTO dto
    ) {
        // O service vai checar a permissão (RN-01)
        LivroAcervoDTO newLivro = bibliotecaService.addLivroToMyAcervo(id_biblioteca, dto);
        return new ResponseEntity<>(newLivro, HttpStatus.CREATED);
    }

    /**
     * RF-12: Remover um livro do acervo
     */
    @DeleteMapping("/{id_biblioteca}/livros/{id_livro}") // Rota protegida
    public ResponseEntity<Void> removeLivroFromMyAcervo(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro
    ) {
        // O service vai checar a permissão (RN-01)
        bibliotecaService.removeLivroFromMyAcervo(id_biblioteca, id_livro);
        return ResponseEntity.noContent().build();
    }

    /**
     * Opcional: Atualizar quantidade de um livro
     */
    @PatchMapping("/{id_biblioteca}/livros/{id_livro}") // Rota protegida
    public ResponseEntity<LivroAcervoDTO> updateQuantidade(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro,
            @Valid @RequestBody UpdateQuantidadeDTO dto
    ) {
        // O service vai checar a permissão (RN-01)
        LivroAcervoDTO updatedLivro = bibliotecaService.updateQuantidadeLivro(id_biblioteca, id_livro, dto);

        if (updatedLivro == null) { // Caso a quantidade seja 0
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(updatedLivro);
    }
}