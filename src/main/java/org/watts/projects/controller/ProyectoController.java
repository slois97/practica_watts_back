package org.watts.projects.controller;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.watts.projects.dto.ArchivoResponse;
import org.watts.projects.dto.InvitarUsuarioRequest;
import org.watts.projects.dto.ProyectoRequest;
import org.watts.projects.dto.ProyectoResponse;
import org.watts.projects.service.ProyectoService;

@RestController
@RequestMapping("/api/proyectos")
@CrossOrigin(origins = "*")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    // Endpoint para crear un proyecto
    @PostMapping
    @PreAuthorize("hasAuthority('PROYECTO_CREAR')")
    public ResponseEntity<ProyectoResponse> crearProyecto(
            @Valid @RequestBody ProyectoRequest proyectoRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return new ResponseEntity<>(proyectoService.crearProyecto(proyectoRequest, userDetails.getUsername()), HttpStatus.CREATED);
    }

    // Endpoint para invitar a un usuario a un proyecto
    @PostMapping("/{id}/invitar")
    @PreAuthorize("hasAuthority('PROYECTO_LEER')")
    public ResponseEntity<Void> invitarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody InvitarUsuarioRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        proyectoService.invitarUsuario(id, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // Endpoint para subir un archivo a un proyecto
    @PostMapping(value = "/{id}/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROYECTO_LEER')")
    public ResponseEntity<ArchivoResponse> subirArchivo(
            @PathVariable Long id,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(required = false, defaultValue = "false") boolean reemplazar,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(proyectoService.subirArchivo(id, archivo, userDetails.getUsername(), reemplazar));
    }

    // Endpoint para listar los proyectos de un usuario
    @GetMapping
    @PreAuthorize("hasAuthority('PROYECTO_LEER')")
    public ResponseEntity<Page<ProyectoResponse>> listarMisProyectos(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nombreMatchMode,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String descripcionMatchMode,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        return ResponseEntity.ok(proyectoService.listarMisProyectos(
                userDetails.getUsername(),
                pageable,
                nombre,
                nombreMatchMode,
                descripcion,
                descripcionMatchMode
        ));
    }

    // Endpoint para listar los archivos de un proyecto
    @GetMapping("/{id}/archivos")
    @PreAuthorize("hasAuthority('PROYECTO_LEER')")
    public ResponseEntity<Page<ArchivoResponse>> listarArchivos(
            @PathVariable Long id,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(proyectoService.listarArchivos(id, userDetails.getUsername(), pageable));
    }

    // Endpoint para descargar un archivo
    @GetMapping("/archivos/{id}/descargar")
    @PreAuthorize("hasAuthority('PROYECTO_LEER')")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Resource archivo = proyectoService.descargarArchivo(id, userDetails.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo);
    }

    // Endpoint para actualizar un proyecto
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTO_LEER')") // Aunque ponga el permiso proyecto leer, solo el propietario puede editar
    public ResponseEntity<ProyectoResponse> actualizarProyecto(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(proyectoService.actualizarProyecto(id, request, userDetails.getUsername()));
    }

    // Endpoint para eliminar un proyecto
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTO_LEER')") // Aunque ponga el permiso proyecto leer, solo el propietario puede borrar
    public ResponseEntity<Void> eliminarProyecto(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        proyectoService.eliminarProyecto(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Endpoint para eliminar un archivo
    @DeleteMapping("/archivos/{id}")
    @PreAuthorize("hasAuthority('PROYECTO_LEER')") // Aunque ponga el permiso proyecto leer, solo el editor o propietario puede borrar
    public ResponseEntity<Void> eliminarArchivo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        proyectoService.eliminarArchivo(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
