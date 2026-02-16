package org.watts.projects.dto;

import jakarta.validation.constraints.NotBlank;

public record ProyectoRequest(
        @NotBlank(message = "El nombre del proyecto es obligatorio")
        String nombre,
        String descripcion
) {
}
