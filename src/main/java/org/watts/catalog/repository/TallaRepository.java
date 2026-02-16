package org.watts.catalog.repository;

import org.watts.catalog.model.Talla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TallaRepository extends JpaRepository<Talla, Long> {

    // Se usa para buscar una talla por su nombre
    Optional<Talla> findByNombre(String nombre);
}
