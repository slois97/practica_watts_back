package org.watts.catalog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.catalog.dto.ColorRequest;
import org.watts.catalog.model.Color;
import org.watts.catalog.service.ColorService;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/colores") // URL base
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class ColorController {

    private final ColorService colorService;

    public ColorController(ColorService colorService) {
        this.colorService = colorService;
    }

    // Endpoint para obtener todos los colores
    @GetMapping
    public ResponseEntity<List<Color>> obtenerColores() {
        return ResponseEntity.ok(colorService.listarColores());
    }

    // Endpoint para crear un nuevo color
    @PostMapping
    @PreAuthorize("hasAuthority('COLOR_CREAR')")
    public ResponseEntity<Color> crearColor(@RequestBody ColorRequest request) {
        Color nuevoColor = colorService.crearColor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoColor);
    }

    // Endpoint para editar un color
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COLOR_EDITAR')")
    public ResponseEntity<Color> actualizarColor(@PathVariable Long id, @RequestBody ColorRequest request) {
        return ResponseEntity.ok(colorService.actualizarColor(id, request));
    }
}