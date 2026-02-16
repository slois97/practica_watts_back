package org.watts.transaction.controller;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;
import org.watts.catalog.repository.VarianteRepository;
import org.watts.inventory.dto.InventarioResponse;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.repository.AlmacenRepository;
import org.watts.shared.exception.ResourceNotFoundException;
import org.watts.shared.utils.SpecificationUtils;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.model.Movimiento;
import org.watts.transaction.service.MovimientoService;
import org.watts.transaction.dto.MovimientoRequest;
import org.watts.transaction.dto.MovimientoResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final VarianteRepository varianteRepository;
    private final AlmacenRepository almacenRepository;

    public MovimientoController(MovimientoService movimientoService, VarianteRepository varianteRepository, AlmacenRepository almacenRepository) {
        this.movimientoService = movimientoService;
        this.varianteRepository = varianteRepository;
        this.almacenRepository = almacenRepository;
    }

    // Endpoint para crear un movimiento
    @PostMapping
    @PreAuthorize("hasAuthority('MOVIMIENTO_CREAR')")
    public ResponseEntity<InventarioResponse> crearMovimiento(@Valid @RequestBody MovimientoRequest request) {

        InventarioResponse inventarioActualizado = movimientoService.procesarMovimiento(
                varianteRepository.findBySku(request.varianteSku())
                        .orElseThrow(() -> new ResourceNotFoundException("Variante", request.varianteSku())),
                almacenRepository.findById(request.almacenId())
                        .orElseThrow(() -> new ResourceNotFoundException("Almacen", request.almacenId())),
                request.cantidad(),
                request.tipo(),
                request.observaciones(),
                request.precioCompraUnitario(),
                request.precioVentaUnitario()
        );

        return new ResponseEntity<>(inventarioActualizado, HttpStatus.CREATED);
    }

    // Endpoing para ver todos los movimientos con paginación
    @GetMapping
    @PreAuthorize("hasAuthority('MOVIMIENTO_LEER')")
    public ResponseEntity<Page<MovimientoResponse>> listarMovimientos(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String varianteNombre,
            @RequestParam(required = false) String varianteMatchMode,
            @RequestParam(required = false) String almacenNombre,
            @RequestParam(required = false) String almacenMatchMode,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) String observacionesMatchMode,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String creadoPor,
            @RequestParam(required = false) String creadoPorMatchMode
    ) {
        return ResponseEntity.ok(movimientoService.verTodosLosMovimientos(
                pageable,
                varianteNombre,
                varianteMatchMode,
                almacenNombre,
                almacenMatchMode,
                tipo,
                observaciones,
                observacionesMatchMode,
                fechaInicio,
                fechaFin,
                creadoPor,
                creadoPorMatchMode
        ));
    }

    // Endpoint para exportar movimientos
    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('MOVIMIENTO_LEER')")
    public void exportar(
            @RequestParam(required = false) String varianteNombre,
            @RequestParam(required = false) String varianteMatchMode,
            @RequestParam(required = false) String almacenNombre,
            @RequestParam(required = false) String almacenMatchMode,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) String observacionesMatchMode,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) String creadoPor,
            @RequestParam(required = false) String creadoPorMatchMode,
            @RequestParam(defaultValue = "pdf") String formato,
            HttpServletResponse response
    ) throws java.io.IOException {

        // 1. Reconstruir Specification
        Specification<Movimiento> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            // Filtros directos
            SpecificationUtils.addPredicate(predicates, cb, root.get("observaciones"), observaciones, observacionesMatchMode);
            SpecificationUtils.addPredicate(predicates, cb, root.get("creadoPor"), creadoPor, creadoPorMatchMode);

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            SpecificationUtils.addDateRangePredicate(predicates, cb, root.get("fechaCreacion"), fechaInicio, fechaFin);

            // Filtros por relaciones (Joins)
            if (varianteNombre != null && !varianteNombre.isEmpty()) {
                Join<Movimiento, Variante> varianteJoin = root.join("variante", JoinType.LEFT);
                Join<Variante, Producto> productoJoin = varianteJoin.join("producto", JoinType.LEFT);
                SpecificationUtils.addPredicate(predicates, cb, productoJoin.get("nombre"), varianteNombre, varianteMatchMode);
            }

            if (almacenNombre != null && !almacenNombre.isEmpty()) {
                Join<org.watts.transaction.model.Movimiento, Almacen> almacenJoin = root.join("almacen", JoinType.LEFT);
                SpecificationUtils.addPredicate(predicates, cb, almacenJoin.get("descripcion"), almacenNombre, almacenMatchMode);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Configurar respuesta
        String filename = "movimientos_" + System.currentTimeMillis() + "." + formato;
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        if ("csv".equalsIgnoreCase(formato)) {
            response.setContentType("text/csv");
            movimientoService.exportar(spec, formato, response.getOutputStream());
        } else {
            response.setContentType("application/pdf");
            movimientoService.exportar(spec, formato, response.getOutputStream());
        }
    }

    // Endpoint para ver el historial de un variante con paginación
    @GetMapping("/historial/{varianteId}")
    @PreAuthorize("hasAuthority('MOVIMIENTO_LEER')")
    public ResponseEntity<Page<MovimientoResponse>> verHistorial(
            @PathVariable Long varianteId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(movimientoService.verHistorialVariante(varianteId, pageable));
    }
}
