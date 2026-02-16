package org.watts.transaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.watts.transaction.enums.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,

        // Nuevos campos para auditoría
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime fechaCreacion,

        String creadoPor,

        TipoMovimiento tipo,
        int cantidad,
        int stockResultante,
        String observaciones,

        // Datos para facilitar después en el frontend
        String sku,
        String productoNombre,
        String productoCaracteristicasTecnicas,
        String tallaId,
        String talla,
        String colorId,
        String color,
        String almacenId,
        String almacenDescripcion,

        Double precioCompraUnitario,
        Double precioVentaUnitario,
        Double precioCompraTotal,
        Double precioVentaTotal
) {
}
