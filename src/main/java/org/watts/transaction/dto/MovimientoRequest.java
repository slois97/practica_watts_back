package org.watts.transaction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.watts.transaction.enums.TipoMovimiento;

public record MovimientoRequest(
        @NotNull(message = "La variante es obligatoria")
        String varianteSku,
        @NotNull(message = "El almacén es obligatorio")
        Long almacenId,
        @Min(value = 1, message = "La cantidad debe ser mayor que 0")
        int cantidad,
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipo,
        String observaciones,
        @Min(value = 0, message = "El precio de compra no puede ser negativo")
        Double precioCompraUnitario, // Si es null, se usará el de la variante
        @Min(value = 0, message = "El precio de venta no puede ser negativo")
        Double precioVentaUnitario // Si es null, se usará el de la variante
) {
}
