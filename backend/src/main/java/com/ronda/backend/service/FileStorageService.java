package com.ronda.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para guardar archivos subidos al filesystem local.
 * Las fotos se guardan en uploads/profiles/ con nombre UUID para evitar colisiones.
 */
@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir.resolve("profiles"));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
    }

    /**
     * Guarda la foto de perfil y devuelve el path relativo (ej: profiles/uuid.jpg).
     */
    public String saveProfileImage(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;
        Path targetPath = uploadDir.resolve("profiles").resolve(fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "profiles/" + fileName;
    }

    /**
     * Borra un archivo de perfil anterior (para no acumular fotos viejas).
     */
    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return;
        try {
            Path filePath = uploadDir.resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // No es critico si falla el borrado
        }
    }
}
