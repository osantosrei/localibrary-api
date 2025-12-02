package com.localibrary.controller;

import com.localibrary.dto.*;
import com.localibrary.dto.request.AddLivroRequestDTO;
import com.localibrary.dto.request.UpdateLivroRequestDTO;
import com.localibrary.dto.response.BibliotecaResponseDTO;
import com.localibrary.service.BibliotecaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // ============================================================
    // ROTAS PÚBLICAS
    // ============================================================

    @Operation(
            summary = "Mapa de Bibliotecas",
            description = "Lista bibliotecas ATIVAS para exibição no mapa. Suporta paginação. (RF-04)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos")
    })
    @GetMapping
    public ResponseEntity<Page<BibliotecaResponseDTO>> listarBibliotecas(
            @Parameter(description = "Página (0-based)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página") @RequestParam(required = false) Integer size,
            @Parameter(description = "Campo para ordenar (ex: nomeFantasia)") @RequestParam(required = false) String sortField,
            @Parameter(description = "Direção da ordenação: ASC ou DESC") @RequestParam(required = false) String sortDir
    ) {
        return ResponseEntity.ok(bibliotecaService.listarBibliotecasAtivas(page, size, sortField, sortDir));
    }

    @Operation(
            summary = "Detalhes da Biblioteca",
            description = "Exibe informações públicas (endereço, contato) de uma biblioteca. (RF-07)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biblioteca encontrada"),
            @ApiResponse(responseCode = "404", description = "Biblioteca não encontrada ou não está ativa")
    })
    @GetMapping("/{id_biblioteca}")
    public ResponseEntity<BibliotecaDetalhesDTO> verDetalhesBiblioteca(@PathVariable Long id_biblioteca) {
        return ResponseEntity.ok(bibliotecaService.buscarDetalhesBiblioteca(id_biblioteca));
    }

    // ============================================================
    // ROTAS PROTEGIDAS - GESTÃO DE PERFIL
    // ============================================================

    @Operation(
            summary = "Meu Perfil",
            description = "Retorna dados da biblioteca logada para edição. Requer token da própria biblioteca. (RF-13)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode acessar dados de outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "404", description = "Biblioteca não encontrada")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id_biblioteca}/profile")
    public ResponseEntity<BibliotecaDetalhesDTO> getMyProfile(@PathVariable Long id_biblioteca) {
        BibliotecaDetalhesDTO dto = bibliotecaService.getMyBibliotecaDetails(id_biblioteca);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Atualizar Perfil",
            description = "Atualiza dados cadastrais e endereço. O endereço é revalidado via API de geolocalização. (RF-14)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou endereço não encontrado"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode atualizar outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "404", description = "Biblioteca não encontrada"),
            @ApiResponse(responseCode = "503", description = "Serviço de geolocalização indisponível")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id_biblioteca}/profile")
    public ResponseEntity<BibliotecaDetalhesDTO> updateMyProfile(
            @PathVariable Long id_biblioteca,
            @Valid @RequestBody UpdateBibliotecaDTO dto
    ) {
        BibliotecaDetalhesDTO updatedDto = bibliotecaService.updateMyBiblioteca(id_biblioteca, dto);
        return ResponseEntity.ok(updatedDto);
    }

    // ============================================================
    // ROTAS PROTEGIDAS - GESTÃO DE ACERVO
    // ============================================================

    @Operation(
            summary = "Listar Meu Acervo",
            description = "Lista todos os livros da biblioteca logada com paginação. (RF-10)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode acessar acervo de outra biblioteca (RN-01)")
    })
    @SecurityRequirement(name = "bearerAuth")
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

    @Operation(
            summary = "✨ NOVO: Buscar Livro para Edição",
            description = "Retorna todas as informações de um livro do acervo para edição (incluindo IDs de gêneros e quantidade). (RF-NOVO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Livro encontrado",
                    content = @Content(schema = @Schema(implementation = LivroDetalhesDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode acessar livros de outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado no acervo desta biblioteca")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id_biblioteca}/livros/{id_livro}")
    public ResponseEntity<LivroDetalhesDTO> getLivroForEdit(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro
    ) {
        LivroDetalhesDTO livro = bibliotecaService.getLivroForEdit(id_biblioteca, id_livro);
        return ResponseEntity.ok(livro);
    }

    @Operation(
            summary = "Adicionar Livro ao Acervo",
            description = "Adiciona um livro ao acervo. Se o livro não existir no sistema, ele é criado automaticamente. (RF-11)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Livro adicionado com sucesso",
                    content = @Content(schema = @Schema(implementation = LivroAcervoDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou ISBN já existe no acervo"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode adicionar livros em outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "409", description = "Este livro já existe no seu acervo. Use PATCH para atualizar.")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id_biblioteca}/livros")
    public ResponseEntity<LivroAcervoDTO> addLivroToMyAcervo(
            @PathVariable Long id_biblioteca,
            @Valid @RequestBody AddLivroRequestDTO dto
    ) {
        LivroAcervoDTO newLivro = bibliotecaService.addLivroToMyAcervo(id_biblioteca, dto);
        return new ResponseEntity<>(newLivro, HttpStatus.CREATED);
    }

    @Operation(
            summary = "✨ NOVO: Atualizar Livro",
            description = "Atualiza as informações de um livro do acervo (título, autor, gêneros, quantidade, etc). (RF-NOVO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Livro atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LivroAcervoDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode atualizar livros de outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado no acervo desta biblioteca")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id_biblioteca}/livros/{id_livro}")
    public ResponseEntity<LivroDetalhesDTO> updateLivro(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro,
            @Valid @RequestBody UpdateLivroRequestDTO dto
    ) {
        LivroDetalhesDTO updatedLivro = bibliotecaService.updateLivroInLibrary(id_biblioteca, id_livro, dto);
        return ResponseEntity.ok(updatedLivro);
    }

    @Operation(
            summary = "Remover Livro do Acervo",
            description = "Remove um livro do acervo da biblioteca. (RF-12)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Livro removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode remover livros de outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado no acervo desta biblioteca")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id_biblioteca}/livros/{id_livro}")
    public ResponseEntity<Void> removeLivroFromMyAcervo(
            @PathVariable Long id_biblioteca,
            @PathVariable Long id_livro
    ) {
        bibliotecaService.removeLivroFromMyAcervo(id_biblioteca, id_livro);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "⚠️ DEPRECATED: Atualizar Estoque",
            description = "Altera apenas a quantidade de exemplares. Use PATCH /livros/{id} para atualizações completas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quantidade atualizada"),
            @ApiResponse(responseCode = "204", description = "Livro removido (quantidade = 0)"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Você não pode atualizar outra biblioteca (RN-01)"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado no acervo")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id_biblioteca}/livros/{id_livro}/quantidade")
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