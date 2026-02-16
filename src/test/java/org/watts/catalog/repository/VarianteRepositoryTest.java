package org.watts.catalog.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Talla;
import org.watts.catalog.model.Variante;
import org.watts.config.AuditConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AuditConfig.class)
class VarianteRepositoryTest {

    @Autowired private VarianteRepository varianteRepository;
    @Autowired private TestEntityManager entityManager; // Utilidad para persistir datos previos al test

    @Test
    @DisplayName("Debe encontrar variante por SKU")
    void findBySkuTest() {
        // GIVEN: Persistimos las entidades necesarias
        Producto p = new Producto(); p.setNombre("P"); p.setCodigoBase("COD");
        entityManager.persist(p);

        Talla t = new Talla(); t.setNombre("XL");
        entityManager.persist(t);

        Color c = new Color(); c.setNombre("Negro");
        entityManager.persist(c);

        Variante v = new Variante();
        v.setProducto(p);
        v.setTalla(t);
        v.setColor(c);
        // El @PrePersist generará el SKU al guardar: COD-XL-NEG
        entityManager.persist(v);
        entityManager.flush(); // Forzamos escritura en la BDD

        // WHEN
        Optional<Variante> encontrada = varianteRepository.findBySku("COD-XL-NEG");

        // THEN
        assertTrue(encontrada.isPresent());
        assertEquals(v.getId(), encontrada.get().getId());
    }
}