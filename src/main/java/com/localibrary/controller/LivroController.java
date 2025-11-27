package com.localibrary.controller;

import com.localibrary.dto.ApiErrorDTO;
import com.localibrary.dto.BibliotecaParaLivroDTO;
import com.localibrary.dto.LivroDetalhesDTO;
import com.localibrary.dto.response.LivroResponseDTO;
import com.localibrary.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Buscar por Título",
            description = "Pesquisa livros por parte do título. Busca parcial e case-insensitive. Suporta paginação. (RF-02)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Busca realizada com sucesso. Retorna lista vazia se nenhum livro for encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de resposta",
                                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "titulo": "Clean Code",
                              "autor": "Robert C. Martin",
                              "capa": "http://localhost:8080/uploads/capas/clean-code.jpg"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20
                          },
                          "totalElements": 1,
                          "totalPages": 1
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de busca ou paginação inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<LivroResponseDTO>> buscarLivros(
            @Parameter(description = "Termo de busca (mínimo 2 caracteres)", required = true, example = "clean code")
            @RequestParam String titulo,
            @Parameter(description = "Página (0-based)", example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Campo para ordenar", example = "titulo")
            @RequestParam(required = false) String sortField,
            @Parameter(description = "Direção da ordenação: ASC ou DESC", example = "ASC")
            @RequestParam(required = false) String sortDir
    ) {
        Page<LivroResponseDTO> result = livroService.buscarLivrosPorTitulo(titulo, page, size, sortField, sortDir);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Livros Populares",
            description = "Retorna os Top 10 livros com maior disponibilidade no sistema (baseado no número de bibliotecas que os possuem). (RF-03)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de livros populares retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(
                                    name = "Exemplo",
                                    value = """
                        [
                          {
                            "id": 1,
                            "titulo": "Dom Casmurro",
                            "autor": "Machado de Assis",
                            "capa": "http://localhost:8080/uploads/capas/dom-casmurro.jpg"
                          },
                          {
                            "id": 2,
                            "titulo": "1984",
                            "autor": "George Orwell",
                            "capa": "http://localhost:8080/uploads/capas/1984.jpg"
                          }
                        ]
                        """
                            )
                    )
            )
    })
    @GetMapping("/populares")
    public ResponseEntity<List<LivroResponseDTO>> buscarPopulares() {
        return ResponseEntity.ok(livroService.buscarLivrosPopulares());
    }

    @Operation(
            summary = "Detalhes do Livro",
            description = "Exibe ficha técnica completa: título, autor, ISBN, resumo, gêneros literários e lista de livros similares recomendados. (RF-05)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalhes do livro retornados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LivroDetalhesDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemplo completo",
                                    value = """
                        {
                          "id": 1,
                          "titulo": "Clean Code",
                          "autor": "Robert C. Martin",
                          "isbn": "9780132350884",
                          "anoPublicacao": 2008,
                          "editora": "Prentice Hall",
                          "capa": "http://localhost:8080/uploads/capas/clean-code.jpg",
                          "resumo": "Um livro sobre boas práticas de programação...",
                          "fotoAutor": "http://localhost:8080/uploads/autores/uncle-bob.jpg",
                          "generos": ["Tecnologia", "Programação"],
                          "livrosSimilares": [
                            {
                              "id": 2,
                              "titulo": "The Pragmatic Programmer",
                              "autor": "Andrew Hunt",
                              "capa": "..."
                            }
                          ]
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))
            )
    })
    @GetMapping("/{id_livro}")
    public ResponseEntity<LivroDetalhesDTO> verDetalhesLivro(@PathVariable Long id_livro) {
        return ResponseEntity.ok(livroService.buscarDetalhesDoLivro(id_livro));
    }

    @Operation(
            summary = "Onde Encontrar Este Livro",
            description = """
            Lista todas as bibliotecas ATIVAS que possuem este livro em acervo.
            
            **Ordenação por Proximidade (Opcional):**
            - Se informar `lat` e `lon`, a lista será ordenada da biblioteca mais próxima para a mais distante.
            - Se NÃO informar coordenadas, retorna lista sem ordenação específica.
            
            **Requisito:** RN-10 (Ordenação por proximidade ao usuário)
            **Endpoint:** RF-06
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de bibliotecas retornada com sucesso. Pode ser vazia se nenhuma biblioteca tiver o livro.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(
                                    name = "Exemplo com 2 bibliotecas",
                                    value = """
                        [
                          {
                            "id": 1,
                            "nomeFantasia": "Biblioteca Municipal",
                            "telefone": "(11) 98765-4321",
                            "endereco": {
                              "cep": "01310-100",
                              "logradouro": "Av. Paulista",
                              "numero": "1578",
                              "bairro": "Bela Vista",
                              "cidade": "São Paulo",
                              "estado": "SP"
                            },
                            "latitude": -23.5614,
                            "longitude": -46.6560
                          },
                          {
                            "id": 2,
                            "nomeFantasia": "Biblioteca Central",
                            "telefone": "(11) 91234-5678",
                            "endereco": {...},
                            "latitude": -23.5505,
                            "longitude": -46.6333
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Livro não encontrado no sistema",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))
            )
    })
    @GetMapping("/{id_livro}/bibliotecas")
    public ResponseEntity<List<BibliotecaParaLivroDTO>> verBibliotecasDoLivro(
            @PathVariable Long id_livro,
            @Parameter(description = "Latitude do usuário (para ordenação por proximidade)", example = "-23.5505")
            @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude do usuário (para ordenação por proximidade)", example = "-46.6333")
            @RequestParam(required = false) Double lon
    ) {
        return ResponseEntity.ok(livroService.buscarBibliotecasPorLivro(id_livro, lat, lon));
    }
}