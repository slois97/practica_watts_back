package org.watts.catalog.mapper;

import org.mapstruct.Mapper;
import org.watts.catalog.dto.ProductoResponse;
import org.watts.catalog.model.Producto;

@Mapper(componentModel = "spring")
public interface ProductoMapper {
    // Mapeos directos (id, tipo, cantidad...) se hacen solos si los nombres coinciden.
    // Solo especificamos los que son diferentes o anidados:
    ProductoResponse toResponse(Producto producto);
}
