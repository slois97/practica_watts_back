package org.watts.security.user.controller;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.watts.security.user.dto.UsuarioUpdateRequest;
import org.watts.security.user.model.Usuario;
import org.watts.security.user.service.UsuarioService;
import java.util.List;
import java.util.Map;

@RestController
//Se configura para que solo pueda hacer la consulta el usuario admin
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasAuthority('USUARIO_GLOBAL')")
public class UsuarioController {
    private final UsuarioService usuarioService;
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint para crear un usuario
    // Recibe un JSON, genera el código base y lo guarda en la BDD
    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario){
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    // Endpoint para obtener todos los usuarios
    @GetMapping
    public ResponseEntity<Page<Usuario>> obtenerUsuarios(
            @ParameterObject Pageable pageable
    ) {
        Page<Usuario> usuarios = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(usuarios);
    }

    // Endpoint para editar la contraseña de un usuario
    @PatchMapping("/{id}")
    public ResponseEntity<Usuario> editarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioUpdateRequest request) {

        Usuario usuarioEditado = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(usuarioEditado);
    }

    // Endpoint para eliminar un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }

    // Endpoint para activar un usuario
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activarUsuario(@PathVariable Long id) {
        usuarioService.activarUsuario(id);
        return ResponseEntity.ok().build();
    }
}
