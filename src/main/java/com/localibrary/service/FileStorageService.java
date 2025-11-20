package com.localibrary.service;

import com.localibrary.enums.TipoUpload;
import com.localibrary.util.Constants;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    // Injeta o diretório raiz (definido no application.properties)
    public FileStorageService(@Value("${app.upload.dir:" + Constants.UPLOAD_DIR + "}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        initDirectories();
    }

    // Cria a estrutura de pastas ao iniciar: /uploads/capas, /uploads/autores, etc.
    private void initDirectories() {
        try {
            Files.createDirectories(rootLocation);
            for (TipoUpload tipo : TipoUpload.values()) {
                Files.createDirectories(rootLocation.resolve(tipo.getDiretorio()));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível criar os diretórios de upload.", ex);
        }
    }

    public String storeFile(MultipartFile file, TipoUpload tipo) {
        // Normaliza o nome do arquivo
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Validação de segurança básica
        if (originalFilename.contains("..")) {
            throw new RuntimeException("Nome de arquivo inválido: " + originalFilename);
        }

        // Gera nome único: uuid.jpg
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Define o caminho final: uploads/capas/uuid.jpg
        Path targetLocation = this.rootLocation.resolve(tipo.getDiretorio()).resolve(fileName);

        try {
            // --- A MÁGICA DO REDIMENSIONAMENTO ---
            // Redimensiona e salva
            Thumbnails.of(file.getInputStream())
                    .size(tipo.getLargura(), tipo.getAltura())
                    .outputQuality(0.9) // 90% de qualidade (bom equilíbrio)
                    .toFile(targetLocation.toFile());

            // Retorna o caminho relativo para salvar no banco (ex: capas/uuid.jpg)
            return tipo.getDiretorio() + "/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Falha ao armazenar arquivo " + fileName, ex);
        }
    }
}