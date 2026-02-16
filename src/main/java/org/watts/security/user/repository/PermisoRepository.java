package org.watts.security.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.watts.security.user.model.Permiso;

import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    // Metodo para buscar un permiso por su nombre (Ej: "PRODUCTO_CREAR")
    Optional<Permiso> findByNombre(String nombre);
}