package org.watts.catalog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

@JsonPropertyOrder({"id", "codigoBase", "nombre", "caracteristicasTecnicas", "activo"})
public record ProductoResponse(
        Long id,
        String nombre,
        String codigoBase,
        String caracteristicasTecnicas,
        String imagenUrl,
        boolean activo,

        // Nuevos campos para auditoria
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime fechaCreacion,

        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime fechaModificacion,

        String creadoPor,
        String modificadoPor
) {
}
