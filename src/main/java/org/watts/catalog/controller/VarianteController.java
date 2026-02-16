package org.watts.catalog.controller;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.watts.catalog.dto.VarianteRequest;
import org.watts.catalog.dto.VarianteResponse;
import org.watts.catalog.service.VarianteService;
import org.watts.shared.service.StorageService;


@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/variantes") // URL base
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class VarianteController {

    private final VarianteService varianteService;
    private final StorageService storageService;

    public VarianteController(VarianteService varianteService, StorageService storageService) {
        this.varianteService = varianteService;
        this.storageService = storageService;
    }

    // Endpoint para crear una variante
    @PostMapping
    @PreAuthorize("hasAuthority('VARIANTE_CREAR')")
    public ResponseEntity<VarianteResponse> crearVariante(
            @Valid @RequestPart("variante") VarianteRequest request, // Añadimos @Valid para asegurar los datos
            @RequestPart(value = "imagen", required = false) MultipartFile imagen
    ) {
        String nombreImagen = null;
        if (imagen != null && !imagen.isEmpty()) {
            nombreImagen = storageService.store(imagen);
        }
        // Llamamos al servicio para crear la variante
        VarianteResponse varianteNueva = varianteService.crearVariante(request, nombreImagen);
        // Devolvemos la variante creada
        return new ResponseEntity<>(varianteNueva, HttpStatus.CREATED);
    }

    // Endpoint actualizado con page para obtener todas las variantes con paginación
    @GetMapping
    @PreAuthorize("hasAuthority('VARIANTE_LEER')")
    public ResponseEntity<Page<VarianteResponse>> listarVariantes(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String skuMatchMode,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String tallaMatchMode,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String colorMatchMode
    ) {
        return ResponseEntity.ok(varianteService.listarVariantes(
                productoId,
                pageable,
                sku,
                skuMatchMode,
                talla,
                tallaMatchMode,
                color,
                colorMatchMode
        ));
    }

    // Endpoint para exportar variantes (PDF/CSV)
    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('VARIANTE_LEER')")
    public void exportar(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String skuMatchMode,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String tallaMatchMode,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String colorMatchMode,
            @RequestParam(defaultValue = "pdf") String formato,
            HttpServletResponse response
    ) throws java.io.IOException {

        // 1. Reconstruir la Specification (Misma lógica que en listar)
        org.springframework.data.jpa.domain.Specification<org.watts.catalog.model.Variante> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Filtro Producto Padre
            if (productoId != null) {
                predicates.add(cb.equal(root.get("producto").get("id"), productoId));
            }

            // Filtros Dinámicos
            org.watts.shared.utils.SpecificationUtils.addPredicate(predicates, cb, root.get("sku"), sku, skuMatchMode);

            if (talla != null && !talla.isEmpty()) {
                org.watts.shared.utils.SpecificationUtils.addPredicate(predicates, cb, root.get("talla").get("nombre"), talla, tallaMatchMode);
            }

            if (color != null && !color.isEmpty()) {
                org.watts.shared.utils.SpecificationUtils.addPredicate(predicates, cb, root.get("color").get("nombre"), color, colorMatchMode);
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // 2. Configurar cabeceras
        String filename = "variantes_" + System.currentTimeMillis() + "." + formato;
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // 3. Llamar al servicio
        if ("csv".equalsIgnoreCase(formato)) {
            response.setContentType("text/csv");
            varianteService.exportar(spec, formato, response.getOutputStream());
        } else {
            response.setContentType("application/pdf");
            varianteService.exportar(spec, formato, response.getOutputStream());
        }
    }

    // Endpoint para actualizar los precios de una variante existente
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VARIANTE_EDITAR')")
    public ResponseEntity<VarianteResponse> actualizarVariante(
            @PathVariable Long id,
            @Valid @RequestPart("variante") VarianteRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen
    ) {
        String nombreImagen = null;
        if (imagen != null && !imagen.isEmpty()) {
            nombreImagen = storageService.store(imagen);
        }
        VarianteResponse varianteActualizada = varianteService.actualizarVariante(id, request, nombreImagen);
        return ResponseEntity.ok(varianteActualizada);
    }

    // Endpoint para eliminar una variante
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VARIANTE_ELIMINAR')")
    public ResponseEntity<Void> eliminarVariante(@PathVariable Long id) {
        varianteService.eliminarVariante(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint para activar una variante
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('VARIANTE_CREAR')")
    public ResponseEntity<Void> activarVariante(@PathVariable Long id) {
        varianteService.activarVariante(id);
        return ResponseEntity.ok().build();
    }
}
