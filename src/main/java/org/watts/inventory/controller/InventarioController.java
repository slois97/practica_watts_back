package org.watts.inventory.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.catalog.repository.VarianteRepository;
import org.watts.inventory.dto.InventarioResponse;
import org.watts.inventory.models.Inventario;
import org.watts.inventory.repository.AlmacenRepository;
import org.watts.inventory.service.InventarioService;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final VarianteRepository varianteRepository;
    private final AlmacenRepository almacenRepository;
    private final InventarioService inventarioService;

    public InventarioController(VarianteRepository varianteRepository, AlmacenRepository almacenRepository,InventarioService inventarioService ){
        this.varianteRepository = varianteRepository;
        this.almacenRepository = almacenRepository;
        this.inventarioService = inventarioService;
    }

    @GetMapping("/total/{varianteId}")
    @PreAuthorize("hasAuthority('ALMACEN_LEER')")
    public int stockTotal(@PathVariable Long varianteId) {
        return inventarioService.stockTotal(varianteRepository.findById(varianteId).orElseThrow());
    }

    // Endpoint para listar el stock por almacén con paginación
    @GetMapping("/stock/{almacenId}")
    @PreAuthorize("hasAuthority('ALMACEN_LEER')")
    public ResponseEntity<Page<InventarioResponse>> listarStockPorAlmacen(
            @PathVariable Long almacenId,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String producto,
            @RequestParam(required = false) String productoMatchMode,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String skuMatchMode,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String tallaMatchMode,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String colorMatchMode
    ) {
        return ResponseEntity.ok(inventarioService.listarStockPorAlmacen(
                almacenId, pageable, producto, productoMatchMode, sku, skuMatchMode, talla, tallaMatchMode, color, colorMatchMode
        ));
    }
}
