package org.watts.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.watts.catalog.dto.ProductoRequest;
import org.watts.catalog.dto.ProductoResponse;
import org.watts.catalog.mapper.ProductoMapper;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;
import org.watts.catalog.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.watts.shared.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.watts.shared.service.ReportService;
import org.watts.shared.service.StorageService;
import org.watts.shared.utils.SpecificationUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ReportService reportService;
    private final StorageService storageService;

    public ProductoService(ProductoRepository productoRepository,
                           ProductoMapper productoMapper,
                           ReportService reportService,
                           StorageService storageService
    ) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.reportService = reportService;
        this.storageService = storageService;
    }

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request, String imagenUrl) {
        // Limpiamos el String codigoBase
        // Si el usuario envía " cami " lo convertimos a "CAMI"
        String codigoLimpio = request.codigoBase().trim().toUpperCase();

        int contador = 1;

        // Creamos el primer candidato: XXX-1
        String codigoCandidato = codigoLimpio + "-" + contador;

        // Bucle mientras XXX-X exista, probamos con el siguiente número
        while (productoRepository.existsByCodigoBase(codigoCandidato)) {
            contador++;
            codigoCandidato = codigoLimpio + "-" + contador;
        }

        // Mapeamos los datos del DTO
        Producto nuevoProducto = new Producto();

        nuevoProducto.setNombre(request.nombre());
        nuevoProducto.setCaracteristicasTecnicas(request.caracteristicasTecnicas());
        if (imagenUrl != null) {
            nuevoProducto.setImagenUrl(imagenUrl);
        }
        nuevoProducto.setActivo(true);

        // Asignamos el primer código libre encontrado anteriormente
        nuevoProducto.setCodigoBase(codigoCandidato);

        // Lo guardamos en la BDD
        Producto productoGuardado = productoRepository.save(nuevoProducto);

        return productoMapper.toResponse(productoGuardado);
    }

    // Metodo para actualizar un producto existente
    @Transactional
    public ProductoResponse actualizarProducto(Long id, ProductoRequest request, String imagenUrl) {
        // Buscamos el producto
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        // ------------------------------------------
        // Logica para actualizar codigo base y SKUs
        String nuevoCodigo = request.codigoBase().trim().toUpperCase();
        // Verificamos si el código ha cambiado respecto al que ya tenía
        if (!nuevoCodigo.equals(producto.getCodigoBase())) {
            // Si cambió, verificamos que el nuevo código no esté ocupado por otro producto
            if (productoRepository.existsByCodigoBase(nuevoCodigo)) {
                throw new RuntimeException("No se puede actualizar. El código base " + nuevoCodigo + " ya existe.");
            }
            // Asignamos el nuevo código al producto
            producto.setCodigoBase(nuevoCodigo);
            // IMPORTANTE: Recorremos todas las variantes y regeneramos su SKU
            // Al estar dentro de una transacción (@Transactional), estos cambios se guardarán automáticamente
            if (producto.getVariantes() != null) {
                for (Variante variante : producto.getVariantes()) {
                    // Reutilizamos el metodo existente en la entidad Variante
                    variante.generarSkuAutomatico();
                }
            }
        }
        // -------------------------------------------
        // Actualizamos los datos
        producto.setNombre(request.nombre());
        producto.setCaracteristicasTecnicas(request.caracteristicasTecnicas());
        // Si el producto ya tenía una imagen, la borramos del FTP
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            if (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty()) {
                storageService.delete(producto.getImagenUrl());
            }
            // Asignamos la nueva imagen
            producto.setImagenUrl(imagenUrl);
        }

        // Lo guardamos en la BDD
        Producto productoActualizado = productoRepository.save(producto);

        return productoMapper.toResponse(productoActualizado);
    }

    // Metodo para eliminar un producto haciendo soft delete (Para no borrarlo completamente de la BDD)
    @Transactional
    public void eliminarProducto(Long id) {
        // Buscamos el producto
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        // Desactivamos el producto
        producto.setActivo(false);

        // Lo guardamos en la BDD
        productoRepository.save(producto);
    }

    // Metodo para volver a activar un producto eliminado con soft-delete
    @Transactional
    public void activarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));

        producto.setActivo(true);
        productoRepository.save(producto);
    }

    // Metodo que devuelve todos los productos
    public Page<ProductoResponse> listarProductos(
            Pageable pageable,
            String codigoBase,
            String codigoMode,
            String nombre,
            String nombreMode,
            Boolean activo
    ) {
        Specification<Producto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.addPredicate(predicates, cb, root.get("codigoBase"), codigoBase, codigoMode);
            SpecificationUtils.addPredicate(predicates, cb, root.get("nombre"), nombre, nombreMode);

            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productoRepository.findAll(spec, pageable).map(productoMapper::toResponse);
    }

    // Metodo para exportar usando el servicio ReportService
    public void exportar(Specification<Producto> spec, String formato, OutputStream outputStream) {
        List<Producto> lista = productoRepository.findAll(spec);

        if ("csv".equalsIgnoreCase(formato)) {
            reportService.generarCsvProductos(lista, outputStream);
        } else {
            reportService.generarPdfProductos(lista, outputStream);
        }
    }
}
