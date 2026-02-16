package org.watts.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Esta clase es un Data Transfer Object, el traductor entre el mundo exterior y la base de datos. Básicamente como un formulario
public record VarianteRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,
        @NotNull(message = "El ID de la talla es obligatorio")
        Long tallaId,
        @NotNull(message = "El ID del color es obligatorio")
        Long colorId,
        @Min(value = 0, message = "El precio de compra no puede ser negativo")
        Double precioCompra,
        @Min(value = 0, message = "El precio de venta no puede ser negativo")
        Double precioVenta,
        String imagenUrl
) {
}
