package org.watts.catalog.mapper;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import org.watts.catalog.dto.ProductoResponse;
import org.watts.catalog.model.Producto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-16T00:39:35+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class ProductoMapperImpl implements ProductoMapper {

    @Override
    public ProductoResponse toResponse(Producto producto) {
        if ( producto == null ) {
            return null;
        }

        Long id = null;
        String nombre = null;
        String codigoBase = null;
        String caracteristicasTecnicas = null;
        String imagenUrl = null;
        boolean activo = false;
        LocalDateTime fechaCreacion = null;
        LocalDateTime fechaModificacion = null;
        String creadoPor = null;
        String modificadoPor = null;

        id = producto.getId();
        nombre = producto.getNombre();
        codigoBase = producto.getCodigoBase();
        caracteristicasTecnicas = producto.getCaracteristicasTecnicas();
        imagenUrl = producto.getImagenUrl();
        activo = producto.isActivo();
        fechaCreacion = producto.getFechaCreacion();
        fechaModificacion = producto.getFechaModificacion();
        creadoPor = producto.getCreadoPor();
        modificadoPor = producto.getModificadoPor();

        ProductoResponse productoResponse = new ProductoResponse( id, nombre, codigoBase, caracteristicasTecnicas, imagenUrl, activo, fechaCreacion, fechaModificacion, creadoPor, modificadoPor );

        return productoResponse;
    }
}
