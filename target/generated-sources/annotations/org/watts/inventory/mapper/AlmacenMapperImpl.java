package org.watts.inventory.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import org.watts.inventory.dto.AlmacenResponse;
import org.watts.inventory.models.Almacen;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-16T00:39:35+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class AlmacenMapperImpl implements AlmacenMapper {

    @Override
    public AlmacenResponse toResponse(Almacen almacen) {
        if ( almacen == null ) {
            return null;
        }

        AlmacenResponse almacenResponse = new AlmacenResponse();

        almacenResponse.setId( almacen.getId() );
        almacenResponse.setCodigo( almacen.getCodigo() );
        almacenResponse.setDescripcion( almacen.getDescripcion() );
        almacenResponse.setUbicacionMaps( almacen.getUbicacionMaps() );
        almacenResponse.setActivo( almacen.isActivo() );

        return almacenResponse;
    }
}
