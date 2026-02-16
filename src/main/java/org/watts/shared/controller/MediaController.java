package org.watts.shared.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.watts.shared.service.StorageService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/uploads")
@CrossOrigin(origins = "*")
public class MediaController {

    private final StorageService storageService;

    public MediaController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        // Vamos al FTP a por el archivo
        Resource file = storageService.loadAsResource(filename);

        // Determinamos el tipo de archivo (MIME Type)
        String contentType = "application/octet-stream"; // Por defecto
        String lowerName = filename.toLowerCase();

        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            contentType = "image/png";
        } else if (lowerName.endsWith(".pdf")) {
            contentType = "application/pdf";
        }

        // Devolvemos el archivo simulando que somos un servidor estático
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // IMPORTANTE: Guardamos en caché la imagen en el navegador 1 hora
                // Esto reduce drásticamente las conexiones al FTP.
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(file);
    }
}