package com.localibrary.controller;

import com.localibrary.dto.BibliotecaParaLivroDTO;
import com.localibrary.dto.LivroDetalhesDTO;
import com.localibrary.dto.response.LivroResponseDTO;
import com.localibrary.service.LivroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {

    @Autowired
    private LivroService livroService;

    // RF-02: Permitir que usuários busquem livros por título
    @GetMapping
    public ResponseEntity<List<LivroResponseDTO>> buscarLivros(@RequestParam String titulo) {
        return ResponseEntity.ok(livroService.buscarLivrosPorTitulo(titulo));
    }

    // RF-03: Mostrar os livros populares
    @GetMapping("/populares")
    public ResponseEntity<List<LivroResponseDTO>> buscarPopulares() {
        return ResponseEntity.ok(livroService.buscarLivrosPopulares());
    }

    // RF-05: Exibir detalhes completos de um livro
    @GetMapping("/{id_livro}")
    public ResponseEntity<LivroDetalhesDTO> verDetalhesLivro(@PathVariable Long id_livro) {
        return ResponseEntity.ok(livroService.buscarDetalhesDoLivro(id_livro));
    }

    // RF-06: Listar bibliotecas que possuem determinado livro
// RF-06: Adicionamos parametros opcionais de lat/long
    @GetMapping("/{id_livro}/bibliotecas")
    public ResponseEntity<List<BibliotecaParaLivroDTO>> verBibliotecasDoLivro(
            @PathVariable Long id_livro,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon
    ) {
        return ResponseEntity.ok(livroService.buscarBibliotecasPorLivro(id_livro, lat, lon));
    }
}