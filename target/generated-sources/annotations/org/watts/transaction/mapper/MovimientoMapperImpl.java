package org.watts.transaction.mapper;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Talla;
import org.watts.catalog.model.Variante;
import org.watts.inventory.models.Almacen;
import org.watts.transaction.dto.MovimientoResponse;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.model.Movimiento;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-16T00:39:35+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class MovimientoMapperImpl implements MovimientoMapper {

    @Override
    public MovimientoResponse toResponse(Movimiento movimiento) {
        if ( movimiento == null ) {
            return null;
        }

        String sku = null;
        String tallaId = null;
        String talla = null;
        String colorId = null;
        String color = null;
        String productoNombre = null;
        String productoCaracteristicasTecnicas = null;
        String almacenId = null;
        String almacenDescripcion = null;
        Double precioCompraUnitario = null;
        Double precioVentaUnitario = null;
        Double precioCompraTotal = null;
        Double precioVentaTotal = null;
        Long id = null;
        LocalDateTime fechaCreacion = null;
        String creadoPor = null;
        TipoMovimiento tipo = null;
        int cantidad = 0;
        int stockResultante = 0;
        String observaciones = null;

        sku = movimientoVarianteSku( movimiento );
        Long id1 = movimientoVarianteTallaId( movimiento );
        if ( id1 != null ) {
            tallaId = String.valueOf( id1 );
        }
        talla = movimientoVarianteTallaNombre( movimiento );
        Long id2 = movimientoVarianteColorId( movimiento );
        if ( id2 != null ) {
            colorId = String.valueOf( id2 );
        }
        color = movimientoVarianteColorNombre( movimiento );
        productoNombre = movimientoVarianteProductoNombre( movimiento );
        productoCaracteristicasTecnicas = movimientoVarianteProductoCaracteristicasTecnicas( movimiento );
        Long id3 = movimientoAlmacenId( movimiento );
        if ( id3 != null ) {
            almacenId = String.valueOf( id3 );
        }
        almacenDescripcion = movimientoAlmacenDescripcion( movimiento );
        precioCompraUnitario = movimiento.getPrecioCompraUnitario();
        precioVentaUnitario = movimiento.getPrecioVentaUnitario();
        precioCompraTotal = movimiento.getPrecioCompraTotal();
        precioVentaTotal = movimiento.getPrecioVentaTotal();
        id = movimiento.getId();
        fechaCreacion = movimiento.getFechaCreacion();
        creadoPor = movimiento.getCreadoPor();
        tipo = movimiento.getTipo();
        cantidad = movimiento.getCantidad();
        stockResultante = movimiento.getStockResultante();
        observaciones = movimiento.getObservaciones();

        MovimientoResponse movimientoResponse = new MovimientoResponse( id, fechaCreacion, creadoPor, tipo, cantidad, stockResultante, observaciones, sku, productoNombre, productoCaracteristicasTecnicas, tallaId, talla, colorId, color, almacenId, almacenDescripcion, precioCompraUnitario, precioVentaUnitario, precioCompraTotal, precioVentaTotal );

        return movimientoResponse;
    }

    private String movimientoVarianteSku(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        return variante.getSku();
    }

    private Long movimientoVarianteTallaId(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        Talla talla = variante.getTalla();
        if ( talla == null ) {
            return null;
        }
        return talla.getId();
    }

    private String movimientoVarianteTallaNombre(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        Talla talla = variante.getTalla();
        if ( talla == null ) {
            return null;
        }
        return talla.getNombre();
    }

    private Long movimientoVarianteColorId(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        Color color = variante.getColor();
        if ( color == null ) {
            return null;
        }
        return color.getId();
    }

    private String movimientoVarianteColorNombre(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        Color color = variante.getColor();
        if ( color == null ) {
            return null;
        }
        return color.getNombre();
    }

    private String movimientoVarianteProductoNombre(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        Producto producto = variante.getProducto();
        if ( producto == null ) {
            return null;
        }
        return producto.getNombre();
    }

    private String movimientoVarianteProductoCaracteristicasTecnicas(Movimiento movimiento) {
        Variante variante = movimiento.getVariante();
        if ( variante == null ) {
            return null;
        }
        Producto producto = variante.getProducto();
        if ( producto == null ) {
            return null;
        }
        return producto.getCaracteristicasTecnicas();
    }

    private Long movimientoAlmacenId(Movimiento movimiento) {
        Almacen almacen = movimiento.getAlmacen();
        if ( almacen == null ) {
            return null;
        }
        return almacen.getId();
    }

    private String movimientoAlmacenDescripcion(Movimiento movimiento) {
        Almacen almacen = movimiento.getAlmacen();
        if ( almacen == null ) {
            return null;
        }
        return almacen.getDescripcion();
    }
}
