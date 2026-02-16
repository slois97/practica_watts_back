package org.watts.security.user.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.watts.security.user.model.Permiso;
import org.watts.security.user.model.Usuario;
import org.watts.security.user.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

//Convierte al usuario en UserDetail de Spring Security
@Service
public class UsuarioDetailServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    public UsuarioDetailServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos el usuario en la BDD por su nombre de usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con usuario: " + username ));

        // VALIDACIÓN DE USUARIO ACTIVO
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("El usuario se encuentra desactivado");
        }

        // 1. Crear lista de autoridades
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 2. Añadir el Rol como autoridad (Opcional, pero útil: ROLE_ADMIN)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));

        // 3. AÑADIR CADA PERMISO ESPECÍFICO DEL ROL
        for (Permiso permiso : usuario.getRol().getPermisos()) {
            authorities.add(new SimpleGrantedAuthority(permiso.getNombre()));
        }

        // Devolvemos el UserDetails construido con el rol correspondiente
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(authorities) // Pasamos la lista completa
                .build();
    }

}
