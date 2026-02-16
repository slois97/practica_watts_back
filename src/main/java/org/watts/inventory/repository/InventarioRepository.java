package org.watts.inventory.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.watts.catalog.model.Variante;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.models.Inventario;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long>, JpaSpecificationExecutor<Inventario> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // Bloquea la fila hasta que termine la transacción para evitar race conditions
    Optional<Inventario> findByVarianteAndAlmacen(Variante variante, Almacen almacen);

    List<Inventario> findByVariante(Variante variante);

    Page<Inventario> findByAlmacen_Id(Long id, Pageable pageable);
}