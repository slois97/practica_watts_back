package org.watts.inventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.watts.inventory.models.Almacen;

import java.util.Optional;

public interface AlmacenRepository extends JpaRepository<Almacen, Long>, JpaSpecificationExecutor<Almacen> {
    Optional<Almacen> findByCodigo(String codigo);

    // Se usa para buscar todos los almacenes que no han sido eliminados, es decir, los activos
    Page<Almacen> findByActivoTrue(Pageable pageable);

    boolean existsByCodigo(String codigo);

}
