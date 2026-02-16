package org.watts.projects.dto;

import java.time.LocalDateTime;

public record ArchivoResponse(
        Long id,
        String nombreOriginal,
        String tipo,
        Long tamanyo,
        String subidoPor,
        LocalDateTime fechaSubida
) {
}
