package org.watts.catalog.service;

import aj.org.objectweb.asm.commons.InstructionAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.watts.catalog.dto.ProductoResumen;
import org.watts.catalog.dto.VarianteRequest;
import org.watts.catalog.dto.VarianteResponse;
import org.watts.catalog.mapper.VarianteMapper;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Talla;
import org.watts.catalog.model.Variante;
import org.watts.catalog.repository.ColorRepository;
import org.watts.catalog.repository.ProductoRepository;
import org.watts.catalog.repository.TallaRepository;
import org.watts.catalog.repository.VarianteRepository;
import org.watts.shared.exception.ResourceNotFoundException;
import org.watts.shared.service.ReportService;
import org.watts.shared.service.StorageService;
import org.watts.shared.utils.SpecificationUtils;
import jakarta.persistence.criteria.Predicate;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class VarianteService {

    private final VarianteRepository varianteRepository;
    private final ProductoRepository productoRepository;
    private final TallaRepository tallaRepository;
    private final ColorRepository colorRepository;
    private final VarianteMapper varianteMapper;
    private final ReportService reportService;
    private final StorageService storageService;

    public VarianteService(VarianteRepository varianteRepository,
                           ProductoRepository productoRepository,
                           TallaRepository tallaRepository,
                           ColorRepository colorRepository,
                           VarianteMapper varianteMapper,
                           ReportService reportService,
                           StorageService storageService
    ) {
        this.varianteRepository = varianteRepository;
        this.productoRepository = productoRepository;
        this.tallaRepository = tallaRepository;
        this.colorRepository = colorRepository;
        this.varianteMapper = varianteMapper;
        this.reportService = reportService;
        this.storageService = storageService;
    }

    // Metodo para crear nuevas variantes
    @Transactional // Esto le dice a Spring que o se guarda todo correctamente o se hace rollback
    public VarianteResponse crearVariante(VarianteRequest request, String imagenUrl) {
        // 1 Buscamos las entidades. Si no existen, lanzamos la excepción del Shared.
        // Lo cual activará el GlobalException Handler y devolverá error 404

        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", request.productoId()));
        Talla talla = tallaRepository.findById(request.tallaId())
                .orElseThrow(() -> new ResourceNotFoundException("Talla", request.tallaId()));
        Color color = colorRepository.findById(request.colorId())
                .orElseThrow(() -> new ResourceNotFoundException("Color", request.colorId()));

        // 2 Instanciamos la variante y le asignamos las relaciones
        Variante nuevaVariante = new Variante();
        nuevaVariante.setProducto(producto);
        nuevaVariante.setTalla(talla);
        nuevaVariante.setColor(color);
        nuevaVariante.setPrecioCompra(request.precioCompra());
        nuevaVariante.setPrecioVenta(request.precioVenta());
        if (imagenUrl != null) {
            nuevaVariante.setImagenUrl(imagenUrl);
        }

        // 3 Guardamos
        Variante varianteGuardada = varianteRepository.save(nuevaVariante);

        // 4 Devolvemos conversión a DTO
        return varianteMapper.toResponse(varianteGuardada);
    }

    // Metodo para actualizar los precios de una variante existente
    @Transactional
    public VarianteResponse actualizarVariante(Long id, VarianteRequest request, String imagenUrl) {
        // Buscamos la variante
        Variante variante = varianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variante", id));

        // Actualizamos los datos
        if (request.precioCompra() != null) {
            variante.setPrecioCompra(request.precioCompra());
        }
        if (request.precioVenta() != null) {
            variante.setPrecioVenta(request.precioVenta());
        }
        // Si la variante ya tenía una imagen, la borramos del FTP
        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            if (variante.getImagenUrl() != null && !variante.getImagenUrl().isEmpty()) {
                storageService.delete(variante.getImagenUrl());
            }
            // Asignamos la nueva imagen
            variante.setImagenUrl(imagenUrl);
        }
        // Lo guardamos en la BDD
        Variante varianteActualizada = varianteRepository.save(variante);

        return varianteMapper.toResponse(varianteActualizada);
    }

    // Metodo para eliminar una variante usando soft delete
    @Transactional
    public void eliminarVariante(Long id) {
        // Buscamos la variante
        Variante variante = varianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variante", id));
        // Desactivamos la variante
        variante.setActivo(false);
        // Lo guardamos en la BDD
        varianteRepository.save(variante);
    }

    @Transactional
    public void activarVariante(Long id) {
        Variante variante = varianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variante", id));
        // Validamos que el padre esté activo
        if (!variante.getProducto().isActivo()) {
            throw new RuntimeException("No se puede activar la variante porque el producto padre (" +
                    variante.getProducto().getNombre() + ") está inactivo.");
        }
        variante.setActivo(true);
        varianteRepository.save(variante);
    }

    // Metodo modificado para que devuelva todas las variantes activas de un producto o mostrar todas con paginación
    public Page<VarianteResponse> listarVariantes(
            Long productoId,
            Pageable pageable,
            String sku,
            String skuMatchMode,
            String talla,
            String tallaMatchMode,
            String color,
            String colorMatchMode
    ) {

        Specification<Variante> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro EXACTO por Producto Padre
            if (productoId != null) {
                predicates.add(cb.equal(root.get("producto").get("id"), productoId));
            }

            // 3. Filtros DINÁMICOS

            // Filtro por SKU
            SpecificationUtils.addPredicate(predicates, cb, root.get("sku"), sku, skuMatchMode);

            // Filtro por Talla
            if (talla != null && !talla.isEmpty()) {
                SpecificationUtils.addPredicate(predicates, cb, root.get("talla").get("nombre"), talla, tallaMatchMode);
            }

            // Filtro por Color
            if (color != null && !color.isEmpty()) {
                SpecificationUtils.addPredicate(predicates, cb, root.get("color").get("nombre"), color, colorMatchMode);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return varianteRepository.findAll(spec, pageable).map(varianteMapper::toResponse);
    }

    // Metodo para exportar usando el servicio ReportService
    public void exportar(Specification<Variante> spec, String formato, OutputStream outputStream) {
        List<Variante> lista = varianteRepository.findAll(spec);

        if ("csv".equalsIgnoreCase(formato)) {
            reportService.generarCsvVariantes(lista, outputStream);
        } else {
            reportService.generarPdfVariantes(lista, outputStream);
        }
    }
}
