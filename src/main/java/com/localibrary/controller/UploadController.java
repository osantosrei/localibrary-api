package com.localibrary.controller;

import com.localibrary.enums.TipoUpload;
import com.localibrary.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "5. Uploads", description = "Serviço de armazenamento de imagens")
@SecurityRequirement(name = "bearerAuth") // Requer autenticação
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Enviar Imagem", description = "Faz upload, redimensiona e retorna a URL da imagem.")
    @PostMapping(value = "/{tipo}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @Parameter(description = "Tipo: CAPA, AUTOR ou BIBLIOTECA") @PathVariable TipoUpload tipo,
            @Parameter(description = "Arquivo de imagem", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        String relativePath = fileStorageService.storeFile(file, tipo);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(relativePath)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "url", fileDownloadUri,
                "path", relativePath
        ));
    }
}