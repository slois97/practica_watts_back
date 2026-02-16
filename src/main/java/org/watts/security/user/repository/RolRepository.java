package org.watts.security.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.watts.security.user.model.Rol;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Metodo para buscar un rol por su nombre (Ej: "ADMIN", "GESTOR")
    Optional<Rol> findByNombre(String nombre);
}