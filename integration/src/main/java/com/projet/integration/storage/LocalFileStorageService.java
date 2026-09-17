package com.projet.integration.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stratégie par défaut : écrit sur le disque local. Suffisant pour développer,
 * mais à ne pas utiliser en prod sur Render (disque éphémère, fichiers perdus au redéploiement).
 */
@Component
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    public LocalFileStorageService(@Value("${app.storage.local.base-dir:uploads}") String baseDir) {
        this.baseDir = Path.of(baseDir);
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new FileStorageException("Impossible de créer le répertoire de stockage local", e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            Files.copy(file.getInputStream(), baseDir.resolve(filename));
        } catch (IOException e) {
            throw new FileStorageException("Échec de l'écriture du fichier local", e);
        }
        return "/files/" + filename;
    }
}
