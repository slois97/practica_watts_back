package org.watts.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.watts.catalog.model.Producto;
import org.watts.transaction.model.Movimiento;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {

    // Buscar movimientos paginados de una variante específica
    Page<Movimiento> findByVarianteId(Long varianteId, Pageable pageable);

    // Buscar movimientos de un almacén específico
    List<Movimiento> findByAlmacenId(Long almacenId);

    // Buscar movimientos de una variante en un almacén específico
    List<Movimiento> findByVarianteIdAndAlmacenId(Long varianteId, Long almacenId);

    // Buscar movimientos entre dos fechas específicas
    List<Movimiento> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
