package org.watts.security.user.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.security.user.model.Rol;
import org.watts.security.user.repository.RolRepository;
import org.watts.shared.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasAuthority('USUARIO_GLOBAL')")
public class RolController {

    private final RolRepository rolRepository;

    public RolController(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    // Endpoint para listar todos los roles con paginación
    @GetMapping
    public ResponseEntity<Page<Rol>> listarRoles(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(rolRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtenerRol(@PathVariable Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: ", id));
        return ResponseEntity.ok(rol);
    }

    @PostMapping
    public ResponseEntity<Rol> crearRol(@RequestBody Rol rol) {
        // Aseguramos que es nuevo
        rol.setId(null);
        return ResponseEntity.ok(rolRepository.save(rol));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizarRol(@PathVariable Long id, @RequestBody Rol rolDetalles) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: ", id));

        rol.setNombre(rolDetalles.getNombre());

        // Actualizamos los permisos
        // Asumimos que rolDetalles.getPermisos() trae la lista seleccionada en el front
        if (rolDetalles.getPermisos() != null) {
            rol.setPermisos(rolDetalles.getPermisos());
        }

        return ResponseEntity.ok(rolRepository.save(rol));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        if (!rolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol no encontrado con id: ", id);
        }
        rolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}