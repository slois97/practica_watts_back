package org.watts.transaction.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Talla;
import org.watts.catalog.model.Variante;
import org.watts.config.AuditConfig;
import org.watts.inventory.models.Almacen;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.model.Movimiento;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(AuditConfig.class)
class MovimientoRepositoryTest {

    @Autowired private MovimientoRepository movimientoRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    @DisplayName("Debe encontrar movimientos por variante ID")
    void findByVarianteIdTest() {
        // PREPARAR DATOS (Necesitamos persistir toda la jerarquía)
        Producto p = new Producto(); p.setNombre("P"); p.setCodigoBase("C"); entityManager.persist(p);
        Talla t = new Talla(); t.setNombre("T"); entityManager.persist(t);
        Color c = new Color(); c.setNombre("C"); entityManager.persist(c);

        Variante v = new Variante();
        v.setProducto(p); v.setTalla(t); v.setColor(c); v.setSku("SKU");
        entityManager.persist(v);

        Almacen a = new Almacen(); a.setCodigo("A1"); entityManager.persist(a);

        Movimiento m1 = new Movimiento();
        m1.setVariante(v); m1.setAlmacen(a); m1.setTipo(TipoMovimiento.COMPRA); m1.setFechaCreacion(LocalDateTime.now());
        entityManager.persist(m1);

        Movimiento m2 = new Movimiento(); // Movimiento de otra variante (simulado)
        // ... (para testear que solo trae los de 'v', necesitaríamos crear otra variante)

        entityManager.flush();

        // WHEN
        Page<Movimiento> resultado = movimientoRepository.findByVarianteId(v.getId(), Pageable.unpaged());

        // THEN
        assertEquals(1, resultado.getTotalElements());
        assertEquals(v.getId(), resultado.getContent().get(0).getVariante().getId());
    }
}