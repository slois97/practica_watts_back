package org.watts.security.user.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.watts.catalog.model.Variante;
import org.watts.security.user.model.Usuario;
import org.watts.security.user.repository.UsuarioRepository;
import org.watts.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario crearUsuario(Usuario usuario){
        //Codificamos la contraseña del usuario que estamos creando
        usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, org.watts.security.user.dto.UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Actualizar contraseña solo si viene en la petición
        if (request.getPasswordHash() != null && !request.getPasswordHash().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        }

        // Actualizar rol solo si viene en la petición
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }

        // Actualizar estado activo solo si viene en la petición
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        // Actualizar email si viene en la petición
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            usuario.setEmail(request.getEmail());
        }

        return usuarioRepository.save(usuario);
    }

    //Metodo que devuelve todos los usuarios
    public Page<Usuario> listarUsuarios(Pageable pageable){ return usuarioRepository.findAll(pageable); }

    // Metodo para eliminar un usuario usando soft delete
    @Transactional
    public void eliminarUsuario(Long id) {
        // Buscamos el usuario
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        // Desactivamos el usuario
        usuario.setActivo(false);
        // Lo guardamos en la BDD
        usuarioRepository.save(usuario);
    }

    // Metodo para volver a activar un usuario borrado con soft-delete
    @Transactional
    public void activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
