package com.projet.integration.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Stratégie de stockage de fichiers. Implémentation choisie via "app.storage.provider"
 * (local par défaut, cloudinary si configuré).
 */
public interface FileStorageService {

    String upload(MultipartFile file);
}
