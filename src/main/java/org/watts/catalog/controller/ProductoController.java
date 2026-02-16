package org.watts.catalog.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.persistence.criteria.Predicate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.watts.catalog.dto.ProductoRequest;
import org.watts.catalog.dto.ProductoResponse;
import org.watts.catalog.model.Producto;
import org.watts.catalog.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.watts.shared.service.StorageService;
import org.watts.shared.utils.SpecificationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/productos") // URL base
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen
public class ProductoController {

    private final ProductoService productoService;
    private final StorageService storageService;

    public ProductoController(ProductoService productoService, StorageService storageService) {
        this.productoService = productoService;
        this.storageService = storageService;
    }

    // Endpoint para crear un producto
    // Recibe un JSON, genera el código base y lo guarda en la BDD
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCTO_CREAR')")
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestPart("producto") ProductoRequest request, // Añadimos @Valid para asegurar los datos
            @RequestPart(value = "imagen", required = false) MultipartFile imagen
    ) {
        String nombreImagen = null;
        if (imagen != null && !imagen.isEmpty()) {
            nombreImagen = storageService.store(imagen);
        }
        ProductoResponse nuevoProducto = productoService.crearProducto(request, nombreImagen);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // Endpoint para obtener todos los productos Arreglado: Ahora es paginado para evitar problemas con muchos productos
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
    public ResponseEntity<Page<ProductoResponse>> obtenerProductos(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String codigoBase,
            @RequestParam(required = false) String codigoMatchMode,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nombreMatchMode,
            @RequestParam(required = false) Boolean activo
    ) {
        return ResponseEntity.ok(productoService.listarProductos(pageable, codigoBase, codigoMatchMode, nombre, nombreMatchMode, activo));
    }

    // Endpoint para exportar productos
    @GetMapping("/exportar")
    public void exportar(
            @RequestParam(required = false) String codigoBase,
            @RequestParam(required = false) String codigoMatchMode,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nombreMatchMode,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "pdf") String formato,
            HttpServletResponse response
    ) throws IOException {
        // 1. Reconstruir Specification
        Specification<Producto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro Código Base (usando tu Utility existente)
            if (codigoBase != null) {
                SpecificationUtils.addPredicate(predicates, cb, root.get("codigoBase"), codigoBase, codigoMatchMode);
            }

            // Filtro Nombre (usando tu Utility existente)
            if (nombre != null) {
                SpecificationUtils.addPredicate(predicates, cb, root.get("nombre"), nombre, nombreMatchMode);
            }

            // Filtro Activo (Manual, ya que es Boolean y addPredicate espera String)
            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. Configurar cabeceras de la respuesta HTTP ANTES de escribir nada
        String filename = "productos_" + System.currentTimeMillis() + "." + formato;
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        if ("csv".equalsIgnoreCase(formato)) {
            response.setContentType("text/csv");
            // 3. Llamar al servicio pasando el outputStream de la respuesta
            productoService.exportar(spec, formato, response.getOutputStream());
        } else {
            response.setContentType("application/pdf");
            productoService.exportar(spec, formato, response.getOutputStream());
        }
    }

    // Endpoint para actualizar un producto
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCTO_EDITAR')")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestPart("producto") ProductoRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen
    ) {
        String nombreImagen = null;
        if (imagen != null && !imagen.isEmpty()) {
            nombreImagen = storageService.store(imagen);
        }
        ProductoResponse productoActualizado = productoService.actualizarProducto(id, request, nombreImagen);
        return ResponseEntity.ok(productoActualizado);
    }

    // Endpoint para eliminar un producto
    @PreAuthorize("hasAuthority('PRODUCTO_ELIMINAR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }

    // Endpoint para activar un producto previamente eliminado
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('PRODUCTO_CREAR')")
    public ResponseEntity<Void> activarProducto(@PathVariable Long id) {
        productoService.activarProducto(id);
        return ResponseEntity.ok().build();
    }
}
