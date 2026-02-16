package org.watts.transaction.service;

import jakarta.transaction.Transactional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;
import org.watts.inventory.dto.InventarioResponse;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.models.Inventario;
import org.watts.inventory.service.InventarioService;
import org.watts.security.user.repository.UsuarioRepository;
import org.watts.shared.service.EmailService;
import org.watts.shared.service.ReportService;
import org.watts.shared.utils.SpecificationUtils;
import org.watts.transaction.dto.MovimientoResponse;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.model.Movimiento;
import org.watts.transaction.repository.MovimientoRepository;
import org.watts.transaction.mapper.MovimientoMapper;
import jakarta.persistence.criteria.Predicate;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final InventarioService inventarioService;
    private final MovimientoMapper movimientoMapper;
    private final ReportService reportService;
    private final EmailService emailService;

    public MovimientoService(MovimientoRepository movimientoRepository,
                             InventarioService inventarioService,
                             MovimientoMapper movimientoMapper,
                             ReportService reportService,
                             EmailService emailService
    ) {
        this.movimientoRepository = movimientoRepository;
        this.inventarioService = inventarioService;
        this.movimientoMapper = movimientoMapper;
        this.reportService = reportService;
        this.emailService = emailService;
    }

    // Metodo para guardar un movimiento
    @Transactional
    public InventarioResponse procesarMovimiento(
            Variante variante,
            Almacen almacen,
            int cantidad,
            TipoMovimiento tipo,
            String observaciones,
            Double precioCompraManual,
            Double precioVentaManual
            ) {
        // Pedimos a InventarioService que actualice el stock
        Inventario inventarioActualizado = inventarioService.updateStock(variante, almacen, cantidad, tipo);

        // Creamos y guardamos el registro del movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setVariante(variante);
        movimiento.setAlmacen(almacen);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockResultante(inventarioActualizado.getStock());
        movimiento.setObservaciones(observaciones);
        // Lógica precios
        Double costeFinal;
        Double ventaFinal;
        if (precioCompraManual != null) {
            // Si se introduce un precio manual, usamos ese
            movimiento.setPrecioCompraUnitario(precioCompraManual);
        } else {
            // Si no, usamos el de la variante
            movimiento.setPrecioCompraUnitario(variante.getPrecioCompra());
        }
        if (precioVentaManual != null) {
            // Si se introduce un precio manual, usamos ese
            movimiento.setPrecioVentaUnitario(precioVentaManual);
        } else {
            // Si no, usamos el de la variante
            movimiento.setPrecioVentaUnitario(variante.getPrecioVenta());
        }
        // Precios totales
        if (movimiento.getPrecioCompraUnitario() != null) {
            movimiento.setPrecioCompraTotal(movimiento.getPrecioCompraUnitario() * cantidad);
        } else {
            movimiento.setPrecioCompraTotal(0.0);
        }
        if (movimiento.getPrecioVentaUnitario() != null) {
            movimiento.setPrecioVentaTotal(movimiento.getPrecioVentaUnitario() * cantidad);
        } else {
            movimiento.setPrecioVentaTotal(0.0);
        }

        movimientoRepository.save(movimiento);

        String asunto = "Nuevo movimiento registrado";

        String contenido = """
            <h3>Nuevo movimiento de inventario</h3>
            <p><b>Variante:</b> %s</p>
            <p><b>Almacén:</b> %s</p>
            <p><b>Tipo:</b> %s</p>
            <p><b>Cantidad:</b> %d</p>
        """.formatted(
                variante.getSku(),
                almacen.getDescripcion(),
                tipo,
                cantidad
        );

        emailService.enviarNotificacionMovimiento(asunto, contenido);

        return inventarioService.mapearADTO(inventarioActualizado);
    }

    // Metodo para ver el historial de un variante
    public Page<MovimientoResponse> verHistorialVariante(Long varianteId, Pageable pageable) {
        return movimientoRepository.findByVarianteId(varianteId, pageable)
                .map(movimientoMapper::toResponse);
    }

    // Metodo para ver todos los movimientos con paginación
    public Page<MovimientoResponse> verTodosLosMovimientos(
            Pageable pageable,
            String varianteNombre,
            String varianteMatchMode,
            String almacenNombre,
            String almacenMatchMode,
            TipoMovimiento tipo,
            String observaciones,
            String observacionesMatchMode,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String creadoPor,
            String creadoPorMatchMode
    ) {
        Specification<Movimiento> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtros directos (texto)
            SpecificationUtils.addPredicate(predicates, cb, root.get("observaciones"), observaciones, observacionesMatchMode);
            SpecificationUtils.addPredicate(predicates, cb, root.get("creadoPor"), creadoPor, creadoPorMatchMode);

            // Filtro enum
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            // Filtro fecha
            SpecificationUtils.addDateRangePredicate(predicates, cb, root.get("fechaCreacion"), fechaInicio, fechaFin);

            // Filtros por relaciones

            // Filtro variante
            if (varianteNombre != null && !varianteNombre.isEmpty()) {
                Join<Movimiento, Variante> varianteJoin = root.join("variante", JoinType.LEFT);
                Join<Variante, Producto> productoJoin = varianteJoin.join("producto", JoinType.LEFT);
                SpecificationUtils.addPredicate(predicates, cb, productoJoin.get("nombre"), varianteNombre, varianteMatchMode);
            }

            // Filtro almacén
            if (almacenNombre != null && !almacenNombre.isEmpty()) {
                Join<Movimiento, Almacen> almacenJoin = root.join("almacen", JoinType.LEFT);
                SpecificationUtils.addPredicate(predicates, cb, almacenJoin.get("descripcion"), almacenNombre, almacenMatchMode);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return movimientoRepository.findAll(spec, pageable)
                .map(movimientoMapper::toResponse);
    }

    // Metodo para exportar usando el servicio ReportService
    public void exportar(Specification<Movimiento> spec, String formato, OutputStream outputStream) {
        List<Movimiento> lista = movimientoRepository.findAll(spec);

        if ("csv".equalsIgnoreCase(formato)) {
            reportService.generarCsvMovimientos(lista, outputStream);
        } else {
            reportService.generarPdfMovimientos(lista, outputStream);
        }
    }
}
