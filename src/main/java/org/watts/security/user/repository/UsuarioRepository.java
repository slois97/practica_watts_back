package org.watts.security.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.watts.security.user.model.Usuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Acceso a la tabla usuarios
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    //Se usa para buscar a un usuario por su nombre de usuario
    Optional<Usuario>findByUsername(String username);

    // Se usa para listar a todos los usuarios
    Page<Usuario> findAll(Pageable pageable);

    //Se usa para listar a los usuarios por su rol
    List<Usuario> findByRolNombre(String nombreRol);

}
