package org.watts.inventory.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.watts.catalog.model.Variante;
import org.watts.inventory.dto.InventarioResponse;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.models.Inventario;
import org.watts.inventory.repository.AlmacenRepository;
import org.watts.inventory.repository.InventarioRepository;
import org.watts.shared.utils.SpecificationUtils;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.model.Movimiento;
import org.watts.transaction.repository.MovimientoRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioService {
    private final InventarioRepository inventarioRepository;
    private final AlmacenRepository almacenRepository;
    private final MovimientoRepository movimientoRepository;


    public InventarioService(InventarioRepository inventarioRepository, AlmacenRepository almacenRepository, MovimientoRepository movimientoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.almacenRepository = almacenRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // Metodo para agregar o quitar stock desde movimientos
    @Transactional
    public Inventario updateStock(Variante variante, Almacen almacen, int cantidad, TipoMovimiento tipo) {

        Inventario inventario = inventarioRepository.findByVarianteAndAlmacen(variante, almacen)
                .orElse(new Inventario());

        if (inventario.getId() == null) {
            inventario.setVariante(variante);
            inventario.setAlmacen(almacen);
            inventario.setStock(0);
        }

        int stockActual = inventario.getStock();
        int nuevoStock;

        switch (tipo) {
            case VENTA, SALIDA_DEFECTO, SALIDA_REGALO -> {
                if (stockActual < cantidad) {
                    throw new RuntimeException("Stock insuficiente. Tienes: " + stockActual + ", intentas sacar: " + cantidad);
                }
                nuevoStock = stockActual - cantidad;
            }
            case COMPRA, ENTRADA_FABRICACION, ENTRADA_DEVOLUCION -> {
                nuevoStock = stockActual + cantidad;
            }
            default -> nuevoStock = stockActual;
        }

        inventario.setStock(nuevoStock);
        return inventarioRepository.save(inventario);
    }

    // Metodo auxiliar para convertir a DTO y evitar loop infinito
    public InventarioResponse mapearADTO(Inventario inventario) {
        InventarioResponse dto = new InventarioResponse();
        dto.setId(inventario.getId());
        dto.setStock(inventario.getStock());

        if (inventario.getVariante() != null) {
            dto.setSku(inventario.getVariante().getSku());
            if (inventario.getVariante().getProducto() != null) {
                dto.setProductoNombre(inventario.getVariante().getProducto().getNombre());
            }
            if (inventario.getVariante().getTalla() != null) {
                dto.setTalla(inventario.getVariante().getTalla().getNombre());
            }
            if (inventario.getVariante().getColor() != null) {
                dto.setColor(inventario.getVariante().getColor().getNombre());
            }
        }

        if (inventario.getAlmacen() != null) {
            dto.setAlmacenNombre(inventario.getAlmacen().getDescripcion());
        }

        return dto;
    }

    public int stockTotal(Variante variante) {
        return inventarioRepository.findByVariante(variante)
                .stream().mapToInt(Inventario::getStock).sum();
    }

    // Metodo para listar el stock por almacén con paginación y filtros
    public Page<InventarioResponse> listarStockPorAlmacen(
            Long almacenId,
            Pageable pageable,
            String producto,
            String productoMatchMode,
            String sku,
            String skuMatchMode,
            String talla,
            String tallaMatchMode,
            String color,
            String colorMatchMode
    ) {
        Specification<Inventario> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro fijo por Almacén
            predicates.add(cb.equal(root.get("almacen").get("id"), almacenId));

            // 2. Filtro Producto (Join: Inventario -> Variante -> Producto -> nombre)
            if (producto != null) {
                // Navegamos por la relación: variante.producto.nombre
                SpecificationUtils.addPredicate(
                        predicates,
                        cb,
                        root.get("variante").get("producto").get("nombre"),
                        producto,
                        productoMatchMode
                );
            }

            // 3. Filtro SKU (Join: Inventario -> Variante -> sku)
            if (sku != null) {
                // Navegamos por la relación: variante.sku
                SpecificationUtils.addPredicate(
                        predicates,
                        cb,
                        root.get("variante").get("sku"),
                        sku,
                        skuMatchMode
                );
            }

            // 4. Filtro Talla (Join: Inventario -> Variante -> Talla -> nombre)
            if (talla != null) {
                SpecificationUtils.addPredicate(predicates, cb, root.get("variante").get("talla").get("nombre"), talla, tallaMatchMode);
            }

            // 5. Filtro Color (Join: Inventario -> Variante -> Color -> nombre)
            if (color != null) {
                SpecificationUtils.addPredicate(predicates, cb, root.get("variante").get("color").get("nombre"), color, colorMatchMode);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return inventarioRepository.findAll(spec, pageable)
                .map(this::mapearADTO);
    }


}
