package com.localibrary.controller;

import com.localibrary.enums.TipoUpload;
import com.localibrary.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Endpoint para upload de imagens.
     * Exemplo: POST /uploads/CAPA (form-data: file=imagem.jpg)
     */
    @PostMapping("/{tipo}")
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable TipoUpload tipo, // O Spring converte a String da URL para o Enum automaticamente!
            @RequestParam("file") MultipartFile file
    ) {
        // Salva e redimensiona
        String relativePath = fileStorageService.storeFile(file, tipo);

        // Gera a URL completa para o Front-end
        // Ex: http://localhost:8080/uploads/capas/uuid-123.jpg
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