package org.watts.catalog.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

// DTO creado para devolver los datos de las Variantes y evitar loop infinito en Producto
@JsonPropertyOrder({ "id", "sku", "producto", "talla", "color", "imagenUrl", "precioVenta", "precioCompra", "fechaCreacion", "fechaModificacion", "creadoPor", "modificadoPor" })  // Para ordenar los datos del JSON
public record VarianteResponse(
        Long id,
        String sku,
        Double precioCompra,
        Double precioVenta,
        ProductoResumen producto, // Usamos el DTO simple
        String talla,
        String color,
        String imagenUrl,
        Boolean activo,

        // Nuevos campos para auditoría
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        String fechaCreacion,

        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        String fechaModificacion,

        String creadoPor,
        String modificadoPor
) {
}
