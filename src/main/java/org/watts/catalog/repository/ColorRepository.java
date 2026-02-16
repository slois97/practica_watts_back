package org.watts.catalog.repository;

import org.watts.catalog.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    // Se usa para buscar un color por su nombre
    Optional<Color> findByNombre(String nombre);
}
