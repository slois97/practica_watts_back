package org.watts.security.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.watts.security.user.model.Permiso;
import org.watts.security.user.repository.PermisoRepository;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permisos")
@PreAuthorize("hasAuthority('USUARIO_GLOBAL')")
public class PermisoController {

    private final PermisoRepository permisoRepository;

    public PermisoController(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Permiso>> listarTodos() {
        return ResponseEntity.ok(permisoRepository.findAll());
    }
}