package org.watts.inventory.mapper;

import org.mapstruct.Mapper;
import org.watts.inventory.dto.AlmacenResponse;
import org.watts.inventory.models.Almacen;

@Mapper(componentModel = "spring")
public interface AlmacenMapper {
    // Mapeos directos (id, tipo, cantidad...) se hacen solos si los nombres coinciden.
    // Solo especificamos los que son diferentes o anidados:
    AlmacenResponse toResponse(Almacen almacen);
}
