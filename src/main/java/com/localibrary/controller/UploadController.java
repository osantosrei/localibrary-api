package com.localibrary.controller;

import com.localibrary.dto.ApiErrorDTO;
import com.localibrary.enums.TipoUpload;
import com.localibrary.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/uploads")
@Tag(name = "5. Uploads", description = "Serviço de armazenamento e processamento de imagens")
@SecurityRequirement(name = "bearerAuth")
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(
            summary = "Enviar Imagem",
            description = """
            Faz upload de uma imagem, redimensiona automaticamente e retorna a URL pública.
            
            **Tipos suportados:**
            - **CAPA**: Capa de livro (600x800px)
            - **AUTOR**: Foto do autor (400x400px)
            - **BIBLIOTECA**: Foto da biblioteca (800x600px)
            
            **Formatos aceitos:** JPG, JPEG, PNG
            **Tamanho máximo:** 5MB
            **Processamento:** A imagem é redimensionada automaticamente mantendo a proporção.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Upload realizado com sucesso. Retorna URL pública da imagem.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de resposta",
                                    value = """
                        {
                          "url": "http://localhost:8080/uploads/capas/abc123-clean-code.jpg",
                          "path": "capas/abc123-clean-code.jpg"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arquivo inválido (formato não suportado, tamanho excedido ou arquivo corrompido)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de erro",
                                    value = """
                        {
                          "status": 400,
                          "message": "Formato de arquivo não suportado. Use JPG, JPEG ou PNG.",
                          "timestamp": "2025-11-26T10:30:00",
                          "errors": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "Arquivo muito grande (máximo 5MB)",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro ao processar ou salvar o arquivo",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class))
            )
    })
    @PostMapping(value = "/{tipo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @Parameter(
                    description = "Tipo de imagem: CAPA (livro), AUTOR (foto do autor) ou BIBLIOTECA (foto da biblioteca)",
                    required = true,
                    example = "CAPA"
            )
            @PathVariable TipoUpload tipo,

            @Parameter(
                    description = "Arquivo de imagem (JPG, JPEG ou PNG, máximo 5MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file
    ) {
        String relativePath = fileStorageService.storeFile(file, tipo);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(relativePath)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "url", fileDownloadUri,
                "path", relativePath,
                "tipo", tipo.name(),
                "tamanho", String.format("%dx%dpx", tipo.getLargura(), tipo.getAltura())
        ));
    }
}