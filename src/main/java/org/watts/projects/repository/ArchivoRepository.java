package org.watts.projects.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.watts.projects.model.Archivo;

import java.util.Optional;

public interface ArchivoRepository extends JpaRepository<Archivo, Long> {
    Page<Archivo> findByProyectoId(Long proyectoId, Pageable pageable);
    Optional<Archivo> findByProyectoIdAndNombreOriginal(Long proyectoId, String nombreOriginal);
}
