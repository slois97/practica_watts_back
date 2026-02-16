package org.watts.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.watts.catalog.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    // Se usa para buscar un producto por su codigoBase
    Optional<Producto> findByCodigoBase(String codigoBase);

    // Se usa para buscar todos los productos que no han sido eliminados, es decir, los activos
    Page<Producto> findByActivoTrue(Pageable pageable);

    // Se usa para saber si el codigo Base ya existe
    boolean existsByCodigoBase(String codigoBase);
}
