package org.watts.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.watts.catalog.model.Variante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarianteRepository extends JpaRepository<Variante, Long>, JpaSpecificationExecutor<Variante> {

    // Se usa para buscar una variante por su SKU
    Optional<Variante> findBySku(String sku);

    // Se usa para buscar todas las variantes de un producto específico (Paginado)
    Page<Variante> findByProductoIdAndActivo(Long productoId, Boolean activo, Pageable pageable);

    // Se usa para buscar todas las variantes
    Page<Variante> findByActivo(Boolean activo, Pageable pageable);
}
