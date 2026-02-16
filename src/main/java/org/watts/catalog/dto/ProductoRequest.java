package org.watts.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductoRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        String nombre,
        @NotBlank(message = "El código base del producto es obligatorio")
        String codigoBase,
        String caracteristicasTecnicas,
        String imagenUrl
) {
}
