package org.watts.catalog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.catalog.dto.TallaRequest;
import org.watts.catalog.model.Talla;
import org.watts.catalog.service.TallaService;

import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/tallas") // URL base
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class TallaController {

    private final TallaService tallaService;

    public TallaController(TallaService tallaService) {
        this.tallaService = tallaService;
    }

    // Endpoint para obtener todas las tallas
    @GetMapping
    public ResponseEntity<List<Talla>> obtenerTallas() {
        return ResponseEntity.ok(tallaService.listarTallas());
    }

    // Endpoint para crear una talla
    @PostMapping
    @PreAuthorize("hasAuthority('TALLA_CREAR')")
    public ResponseEntity<Talla> crearTalla(@RequestBody TallaRequest request) {
        Talla nuevaTalla = tallaService.crearTalla(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTalla);
    }

    // Endpoint para editar una talla
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TALLA_EDITAR')")
    public ResponseEntity<Talla> actualizarTalla(@PathVariable Long id, @RequestBody TallaRequest request) {
        return ResponseEntity.ok(tallaService.actualizarTalla(id, request));
    }
}
