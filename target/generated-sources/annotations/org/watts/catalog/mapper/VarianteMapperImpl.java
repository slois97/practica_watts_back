package org.watts.catalog.mapper;

import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import org.watts.catalog.dto.ProductoResumen;
import org.watts.catalog.dto.VarianteResponse;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-16T00:39:35+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class VarianteMapperImpl implements VarianteMapper {

    @Override
    public VarianteResponse toResponse(Variante variante) {
        if ( variante == null ) {
            return null;
        }

        String talla = null;
        String color = null;
        ProductoResumen producto = null;
        Long id = null;
        String sku = null;
        Double precioCompra = null;
        Double precioVenta = null;
        String imagenUrl = null;
        Boolean activo = null;
        String fechaCreacion = null;
        String fechaModificacion = null;
        String creadoPor = null;
        String modificadoPor = null;

        talla = mapTalla( variante.getTalla() );
        color = mapColor( variante.getColor() );
        producto = toProductoResumen( variante.getProducto() );
        id = variante.getId();
        sku = variante.getSku();
        precioCompra = variante.getPrecioCompra();
        precioVenta = variante.getPrecioVenta();
        imagenUrl = variante.getImagenUrl();
        activo = variante.isActivo();
        if ( variante.getFechaCreacion() != null ) {
            fechaCreacion = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( variante.getFechaCreacion() );
        }
        if ( variante.getFechaModificacion() != null ) {
            fechaModificacion = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( variante.getFechaModificacion() );
        }
        creadoPor = variante.getCreadoPor();
        modificadoPor = variante.getModificadoPor();

        VarianteResponse varianteResponse = new VarianteResponse( id, sku, precioCompra, precioVenta, producto, talla, color, imagenUrl, activo, fechaCreacion, fechaModificacion, creadoPor, modificadoPor );

        return varianteResponse;
    }

    @Override
    public ProductoResumen toProductoResumen(Producto producto) {
        if ( producto == null ) {
            return null;
        }

        Long id = null;
        String nombre = null;
        String codigoBase = null;

        id = producto.getId();
        nombre = producto.getNombre();
        codigoBase = producto.getCodigoBase();

        ProductoResumen productoResumen = new ProductoResumen( id, nombre, codigoBase );

        return productoResumen;
    }
}
