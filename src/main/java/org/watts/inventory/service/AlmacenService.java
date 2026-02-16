package org.watts.inventory.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.watts.inventory.dto.AlmacenRequest;
import org.watts.inventory.dto.AlmacenResponse;
import org.watts.inventory.mapper.AlmacenMapper;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.repository.AlmacenRepository;
import org.watts.shared.exception.ResourceNotFoundException;
import org.watts.shared.utils.SpecificationUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final AlmacenMapper almacenMapper;

    public AlmacenService(AlmacenRepository almacenRepository,  AlmacenMapper almacenMapper) {
        this.almacenRepository = almacenRepository;
        this.almacenMapper = almacenMapper;
    }

    @Transactional(readOnly = true)
    public AlmacenResponse obtenerPorId(Long id) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacen", id));
        return almacenMapper.toResponse(almacen);
    }

    public AlmacenResponse crearAlmacen(AlmacenRequest request) {
        // Limpiamos el código
        request.setCodigo(request.getCodigo().trim().toUpperCase());

        if (almacenRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new RuntimeException("Error: Ya existe un almacén con el código " + request.getCodigo());
        }
        // Mapeamos los datos del DTO
        Almacen nuevoAlmacen = new Almacen();

        nuevoAlmacen.setCodigo(request.getCodigo());
        nuevoAlmacen.setDescripcion(request.getDescripcion());
        nuevoAlmacen.setUbicacionMaps(request.getUbicacionMaps());
        nuevoAlmacen.setActivo(true);

        // Lo guardamos en la BDD
        Almacen almacenGuardado = almacenRepository.save(nuevoAlmacen);

        return almacenMapper.toResponse(almacenGuardado);
    }

    // Metodo para actualizar los datos del almacen
    @Transactional
    public AlmacenResponse actualizarAlmacen(Long id, AlmacenRequest request) {
        // Buscamos el almacen
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacen", id));

        // Actualizamos los datos
        if (request.getCodigo() != null) {
            almacen.setCodigo(request.getCodigo());
        }
        if (request.getDescripcion() != null) {
            almacen.setDescripcion(request.getDescripcion());
        }

        if (request.getUbicacionMaps() != null) {
            almacen.setUbicacionMaps(request.getUbicacionMaps());
        }

        // Lo guardamos en la BDD
        Almacen almacenActualizado = almacenRepository.save(almacen);

        return almacenMapper.toResponse(almacenActualizado);
    }

    // Metodo para eliminar una variante usando soft delete
    @Transactional
    public void eliminarAlmacen(Long id) {
        // Buscamos la variante
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacen", id));

        // Desactivamos el producto
        almacen.setActivo(false);

        // Lo guardamos en la BDD
        almacenRepository.save(almacen);
    }

    // Metodo para listar todos los almacenes con paginación
    public Page<AlmacenResponse> listarAlmacenes(
            Pageable pageable,
            String codigo,
            String codigoMode,
            String descripcion,
            String descripcionMode,
            Boolean activo
    ) {
        Specification<Almacen> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.addPredicate(predicates, cb, root.get("codigo"), codigo, codigoMode);
            SpecificationUtils.addPredicate(predicates, cb, root.get("descripcion"), descripcion, descripcionMode);

            if (activo != null) {
                predicates.add(cb.equal(root.get("activo"), activo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return almacenRepository.findAll(spec, pageable).map(almacenMapper::toResponse);
    }

    @Transactional
    public void activarAlmacen(Long id) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Almacen", id));

        almacen.setActivo(true);
        almacenRepository.save(almacen);
    }
}
