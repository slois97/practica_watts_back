package org.watts.catalog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.watts.catalog.dto.ProductoResumen;
import org.watts.catalog.dto.VarianteResponse;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Talla;
import org.watts.catalog.model.Variante;

@Mapper(componentModel = "spring")
public interface VarianteMapper {
    // Mapeos directos (id, tipo, cantidad...) se hacen solos si los nombres coinciden.
    // Solo especificamos los que son diferentes o anidados:
    @Mapping(source = "talla", target = "talla")
    @Mapping(source = "color", target = "color")
    @Mapping(source = "producto", target = "producto")
    VarianteResponse toResponse(Variante variante);

    // Metodo auxiliar para convertir Producto -> ProductoResumen
    // MapStruct lo detecta y los usa cuando mapea el campo producto de arriba
    ProductoResumen toProductoResumen(Producto producto);

    // Al definir esto, MapStruct sabe qué hacer cuando encuentra "Talla -> String"
    default String mapTalla(Talla talla) {
        if (talla == null) return null;
        return talla.getNombre();
    }

    // Hacemos lo mismo para Color por seguridad
    default String mapColor(Color color) {
        if (color == null) return null;
        return color.getNombre();
    }
}
