package org.watts.projects.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.watts.projects.dto.ArchivoResponse;
import org.watts.projects.dto.InvitarUsuarioRequest;
import org.watts.projects.dto.ProyectoRequest;
import org.watts.projects.dto.ProyectoResponse;
import org.watts.projects.model.Archivo;
import org.watts.projects.model.MiembroProyecto;
import org.watts.projects.model.Proyecto;
import org.watts.projects.model.RolProyecto;
import org.watts.projects.repository.ArchivoRepository;
import org.watts.projects.repository.MiembroProyectoRepository;
import org.watts.projects.repository.ProyectoRepository;
import org.watts.security.user.model.Usuario;
import org.watts.security.user.repository.UsuarioRepository;
import org.watts.shared.exception.ResourceNotFoundException;
import org.watts.shared.service.StorageService;
import org.springframework.core.io.Resource;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import org.watts.shared.utils.SpecificationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProyectoService {
    private final ProyectoRepository proyectoRepository;
    private final MiembroProyectoRepository miembroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArchivoRepository archivoRepository;
    private final StorageService storageService;

    public ProyectoService(
            ProyectoRepository proyectoRepository,
            MiembroProyectoRepository miembroRepository,
            UsuarioRepository usuarioRepository,
            ArchivoRepository archivoRepository,
            StorageService storageService
    ) {
        this.proyectoRepository = proyectoRepository;
        this.miembroRepository = miembroRepository;
        this.usuarioRepository = usuarioRepository;
        this.archivoRepository = archivoRepository;
        this.storageService = storageService;
    }

    @Transactional
    public ProyectoResponse crearProyecto(ProyectoRequest request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado", username));

        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(request.nombre());
        proyecto.setDescripcion(request.descripcion());
        proyecto = proyectoRepository.save(proyecto);

        MiembroProyecto miembro = new MiembroProyecto(proyecto, usuario, RolProyecto.PROPIETARIO);
        miembroRepository.save(miembro);

        return mapToResponse(proyecto, RolProyecto.PROPIETARIO);
    }

    @Transactional(readOnly = true)
    public Page<ProyectoResponse> listarMisProyectos(
            String username,
            Pageable pageable,
            String nombre,
            String nombreMatchMode,
            String descripcion,
            String descripcionMatchMode
    ) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado", username));

        Specification<Proyecto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Solo proyectos donde se es miembro
            Join<Object, Object> miembrosJoin = root.join("miembros");
            predicates.add(cb.equal(miembrosJoin.get("usuario").get("id"), usuario.getId()));

            // 2. Filtros DINÁMICOS

            // Filtro por Nombre
            SpecificationUtils.addPredicate(predicates, cb, root.get("nombre"), nombre, nombreMatchMode);

            // Filtro por Descripción
            SpecificationUtils.addPredicate(predicates, cb, root.get("descripcion"), descripcion, descripcionMatchMode);

            // Aseguramos distinct para no traer el proyecto multiples veces si hubiera algún join extraño (aunque aqui es 1:N)
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Proyecto> proyectosPage = proyectoRepository.findAll(spec, pageable);

        // Transformamos la página de enidades a página de DTOs
        return proyectosPage.map(proyecto -> {
            MiembroProyecto miembroProyecto = miembroRepository.findByProyectoAndUsuario(proyecto, usuario).orElseThrow();
            return mapToResponse(proyecto, miembroProyecto.getRol());
        });
    }

    @Transactional
    public void invitarUsuario(Long proyectoId, InvitarUsuarioRequest request, String usernameSolicitante) {
        // Validamos que quien invita tenga permisos de propietario
        Proyecto proyecto = obtenerProyectoSiEsMiembro(proyectoId, usernameSolicitante, RolProyecto.PROPIETARIO);

        // Buscamos al usuario invitado por su nombre de usuario
        Usuario invitado = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado", request.username()));

        // Validamos que no sea ya miembro
        if (miembroRepository.existsByProyectoAndUsuario(proyecto, invitado)) {
            throw new RuntimeException("El usuario " + request.username() + " ya es miembro del proyecto");
        }

        MiembroProyecto nuevoMiembro = new MiembroProyecto(proyecto, invitado, request.rol());
        miembroRepository.save(nuevoMiembro);
    }

    @Transactional
    public ArchivoResponse subirArchivo(Long proyectoId, MultipartFile file, String username, boolean reemplazar) {
        Proyecto proyecto = obtenerProyectoSiEsMiembro(proyectoId, username, RolProyecto.EDITOR);

        // Buscamos si ya existe el archivo
        Optional<Archivo> archivoExistente = archivoRepository.findByProyectoIdAndNombreOriginal(proyectoId, file.getOriginalFilename());

        if (archivoExistente.isPresent()) {
            if (!reemplazar) {
                // Si existe y no indicamos reemplazar, lanzamos excepción (o podrías retornar null y manejarlo)
                throw new IllegalArgumentException("El archivo ya existe");
            }

            // --- LOGICA DE REEMPLAZO ---
            Archivo archivo = archivoExistente.get();

            // Borramos el archivo físico antiguo
            storageService.delete(archivo.getNombreAlmacenado());

            // Guardamos el nuevo archivo físico
            String nuevoNombreGuardado = storageService.store(file);

            // Actualizamos metadatos
            archivo.setNombreAlmacenado(nuevoNombreGuardado);
            archivo.setTamanyo(file.getSize());
            archivo.setTipoContenido(file.getContentType());

            archivo = archivoRepository.save(archivo);

            return new ArchivoResponse(
                    archivo.getId(),
                    archivo.getNombreOriginal(),
                    archivo.getTipoContenido(),
                    archivo.getTamanyo(),
                    username,
                    archivo.getFechaCreacion()
                );
        }

        String nombreAlmacenado = storageService.store(file);

        Archivo archivo = new Archivo();
        archivo.setNombreOriginal(file.getOriginalFilename());
        archivo.setNombreAlmacenado(nombreAlmacenado);
        archivo.setTipoContenido(file.getContentType());
        archivo.setTamanyo(file.getSize());
        archivo.setProyecto(proyecto);

        archivo = archivoRepository.save(archivo);

        return new ArchivoResponse(
                archivo.getId(),
                archivo.getNombreOriginal(),
                archivo.getTipoContenido(),
                archivo.getTamanyo(),
                username,
                archivo.getFechaCreacion()
            );
    }

    @Transactional(readOnly = true)
    public Page<ArchivoResponse> listarArchivos(Long proyectoId, String username, Pageable pageable) {
        obtenerProyectoSiEsMiembro(proyectoId, username, RolProyecto.LECTOR);

        Page<Archivo> archivosPage = archivoRepository.findByProyectoId(proyectoId, pageable);

        return archivosPage.map(archivo -> new ArchivoResponse(
                archivo.getId(),
                archivo.getNombreOriginal(),
                archivo.getTipoContenido(),
                archivo.getTamanyo(),
                archivo.getCreadoPor(),
                archivo.getFechaCreacion()
        ));
    }

    @Transactional
    public ProyectoResponse actualizarProyecto(Long proyectoId, ProyectoRequest request, String username) {
        // Solo el propietario puede editar el proyecto
        Proyecto proyecto = obtenerProyectoSiEsMiembro(proyectoId, username, RolProyecto.PROPIETARIO);

        proyecto.setNombre(request.nombre());
        proyecto.setDescripcion(request.descripcion());

        proyecto = proyectoRepository.save(proyecto);

        return mapToResponse(proyecto, RolProyecto.PROPIETARIO);
    }

    @Transactional
    public void eliminarProyecto(Long proyectoId, String username) {
        // Solo el propietario puede borrar el proyecto
        Proyecto proyecto = obtenerProyectoSiEsMiembro(proyectoId, username, RolProyecto.PROPIETARIO);

        // Primero borramos los archivos físicos del disco
        for (Archivo archivo : proyecto.getArchivos()) {
            storageService.delete(archivo.getNombreAlmacenado());
        }
        // Después borramos el proyecto de la BDD (JPA Cascade borrará miembros y registros de archivos)
        proyectoRepository.delete(proyecto);
    }

    @Transactional
    public void eliminarArchivo(Long archivoId, String username) {
        Archivo archivo = archivoRepository.findById(archivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado", archivoId));

        // Se requiere al menos ser editor para borrar archivos
        obtenerProyectoSiEsMiembro(archivo.getProyecto().getId(), username, RolProyecto.EDITOR);

        // Primero borramos el archivo físico del disco
        storageService.delete(archivo.getNombreAlmacenado());

        // Después borramos el archivo de la BDD
        archivoRepository.delete(archivo);
    }

    public Resource descargarArchivo(Long archivoId, String username) {
        // Buscamos el archivo en la BD y validamos que tenga permisos
        Archivo archivoDb = archivoRepository.findById(archivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado", archivoId));

        // Usamos el StorageService para obtener el recurso físico
        return storageService.loadAsResource(archivoDb.getNombreAlmacenado());
    }

    private Proyecto obtenerProyectoSiEsMiembro(Long proyectoId, String username, RolProyecto rolMinimoRequerido) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado", username));
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado", proyectoId));

        MiembroProyecto miembro = miembroRepository.findByProyectoAndUsuario(proyecto, usuario)
                .orElseThrow(() -> new RuntimeException("No tienes acceso a este proyecto"));

        if (rolMinimoRequerido == RolProyecto.PROPIETARIO && miembro.getRol() != RolProyecto.PROPIETARIO) {
            throw new RuntimeException("Se requieren permisos de Propietario");
        }
        if (rolMinimoRequerido == RolProyecto.EDITOR && miembro.getRol() == RolProyecto.LECTOR) {
            throw new RuntimeException("Se requieren permisos de Editor o superior");
        }

        return proyecto;
    }

    private ProyectoResponse mapToResponse(Proyecto proyecto, RolProyecto rol) {
        return new ProyectoResponse(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                rol,
                proyecto.getFechaCreacion(),
                proyecto.getMiembros().size()
        );
    }

}
