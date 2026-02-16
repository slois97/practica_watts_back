package org.watts.transaction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.watts.transaction.dto.MovimientoResponse;
import org.watts.transaction.model.Movimiento;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {

    // Mapeos directos (id, tipo, cantidad...) se hacen solos si los nombres coinciden.
    // Solo especificamos los que son diferentes o anidados:

    @Mapping(source = "variante.sku", target = "sku")
    @Mapping(source = "variante.talla.id", target = "tallaId")
    @Mapping(source = "variante.talla.nombre", target = "talla")
    @Mapping(source = "variante.color.id", target = "colorId")
    @Mapping(source = "variante.color.nombre", target = "color")
    @Mapping(source = "variante.producto.nombre", target = "productoNombre")
    @Mapping(source = "variante.producto.caracteristicasTecnicas", target = "productoCaracteristicasTecnicas")
    @Mapping(source = "almacen.id", target = "almacenId")
    @Mapping(source = "almacen.descripcion", target = "almacenDescripcion")
    @Mapping(source = "precioCompraUnitario", target = "precioCompraUnitario")
    @Mapping(source = "precioVentaUnitario", target = "precioVentaUnitario")
    @Mapping(source = "precioCompraTotal", target = "precioCompraTotal")
    @Mapping(source = "precioVentaTotal", target = "precioVentaTotal")
    MovimientoResponse toResponse(Movimiento movimiento);
}