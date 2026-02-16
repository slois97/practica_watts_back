package org.watts.projects.dto;

import org.watts.projects.model.RolProyecto;

import java.time.LocalDateTime;

public record ProyectoResponse(
        Long id,
        String nombre,
        String descripcion,
        RolProyecto miRol,
        LocalDateTime fechaCreacion,
        int cantidadMiembros
) {
}
