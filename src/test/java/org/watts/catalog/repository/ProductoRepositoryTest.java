package org.watts.catalog.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.watts.catalog.model.Producto;
import org.watts.config.AuditConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AuditConfig.class)
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    @DisplayName("Debe verificar si existe un código base")
    void existsByCodigoBaseTest() {
        // GIVEN
        Producto p = new Producto();
        p.setNombre("Casco");
        p.setCodigoBase("CAS-1");
        productoRepository.save(p);

        // WHEN & THEN
        assertTrue(productoRepository.existsByCodigoBase("CAS-1"));
        assertFalse(productoRepository.existsByCodigoBase("CAS-99"));
    }

    @Test
    @DisplayName("Debe encontrar producto por código base")
    void findByCodigoBaseTest() {
        // GIVEN
        Producto p = new Producto();
        p.setNombre("Guantes");
        p.setCodigoBase("GUA-1");
        productoRepository.save(p);

        // WHEN
        Optional<Producto> encontrado = productoRepository.findByCodigoBase("GUA-1");

        // THEN
        assertTrue(encontrado.isPresent());
        assertEquals("Guantes", encontrado.get().getNombre());
    }
}