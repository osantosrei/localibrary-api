package com.localibrary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // Injeta o caminho da pasta e cria o diretório se não existir
    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normaliza o nome do arquivo
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Gera um nome único para evitar conflitos (ex: uuid_foto.jpg)
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // Validação de segurança simples
            if (fileName.contains("..")) {
                throw new RuntimeException("Nome de arquivo inválido " + fileName);
            }

            // Copia o arquivo para o destino (substituindo se existir)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível armazenar o arquivo " + fileName, ex);
        }
    }
}