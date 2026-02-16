package org.watts.projects.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.watts.projects.model.Proyecto;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long>, JpaSpecificationExecutor<Proyecto> {
    // Busca proyectos donde el usuario es miembro (por ID del usuario)
    @Query("SELECT p FROM Proyecto p JOIN p.miembros m WHERE m.usuario.id = :usuarioId")
    Page<Proyecto> findProyectosByUsuarioId(Long usuarioId, Pageable pageable);
}
