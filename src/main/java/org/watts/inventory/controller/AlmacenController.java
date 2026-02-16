package org.watts.inventory.controller;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.inventory.dto.AlmacenRequest;
import org.watts.inventory.dto.AlmacenResponse;
import org.watts.inventory.service.AlmacenService;
import org.watts.shared.service.StorageService;

@RestController
@RequestMapping("/api/almacenes")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class AlmacenController {

    private final AlmacenService almacenService;
    private final StorageService storageService;

    public AlmacenController(AlmacenService almacenService,StorageService storageService) {
        this.almacenService = almacenService;
        this.storageService = storageService;
    }

    // Endpoint para crear un nuevo almacén
    @PostMapping
    @PreAuthorize("hasAuthority('ALMACEN_CREAR')")
    public ResponseEntity<AlmacenResponse> crearAlmacen(
            @Valid @RequestBody AlmacenRequest request
    ) {
        AlmacenResponse nuevoAlmacen = almacenService.crearAlmacen(request);
        return new ResponseEntity<>(nuevoAlmacen, HttpStatus.CREATED);
    }

    // Endpoint para listar todos los almacenes con paginación
    @GetMapping
    @PreAuthorize("hasAuthority('ALMACEN_LEER')")
    public ResponseEntity<Page<AlmacenResponse>> listarAlmacenes(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String codigoMatchMode,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String descripcionMatchMode,
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(almacenService.listarAlmacenes(pageable,codigo,codigoMatchMode,descripcion,descripcionMatchMode,activo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ALMACEN_LEER')")
    public ResponseEntity<AlmacenResponse> obtenerAlmacen(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(almacenService.obtenerPorId(id));
    }

    // Endpoint para actualizar los almacenes existentes
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ALMACEN_EDITAR')")
    public ResponseEntity<AlmacenResponse> actualizarAlmacenes(
            @PathVariable Long id,
            @Valid @RequestPart("almacen") AlmacenRequest request
    ) {
        AlmacenResponse almacenActualizado = almacenService.actualizarAlmacen(id, request);
        return ResponseEntity.ok(almacenActualizado);
    }

    // Endpoint para eliminar un almacen
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ALMACEN_ELIMINAR')")
    public ResponseEntity<Void> eliminarAlmacen(@PathVariable Long id) {
        almacenService.eliminarAlmacen(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para activar un almacén
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ALMACEN_CREAR')")
    public ResponseEntity<Void> activarAlmacen(@PathVariable Long id) {
        almacenService.activarAlmacen(id);
        return ResponseEntity.ok().build();
    }
}
