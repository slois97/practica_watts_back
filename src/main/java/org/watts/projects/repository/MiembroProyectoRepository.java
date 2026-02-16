package org.watts.projects.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.watts.projects.model.MiembroProyecto;
import org.watts.projects.model.Proyecto;
import org.watts.security.user.model.Usuario;

import java.util.Optional;

public interface MiembroProyectoRepository extends JpaRepository<MiembroProyecto, Long> {
    Optional<MiembroProyecto> findByProyectoAndUsuario(Proyecto proyecto, Usuario usuario);
    boolean existsByProyectoAndUsuario(Proyecto proyecto, Usuario usuario);
}
