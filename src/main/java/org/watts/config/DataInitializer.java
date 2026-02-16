package org.watts.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Talla;
import org.watts.catalog.repository.ColorRepository;
import org.watts.catalog.repository.TallaRepository;
import org.watts.security.user.model.Permiso;
import org.watts.security.user.model.Rol;
import org.watts.security.user.model.Usuario;
import org.watts.security.user.repository.PermisoRepository;
import org.watts.security.user.repository.RolRepository;
import org.watts.security.user.repository.UsuarioRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initData(TallaRepository tallaRepo,
                                      ColorRepository colorRepo,
                                      UsuarioRepository usuarioRepo,
                                      RolRepository rolRepo,
                                      PermisoRepository permisoRepo,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Inicializar Tallas y Colores
            inicializarTallas(tallaRepo);
            inicializarColores(colorRepo);

            // 2. Inicializar Permisos y Roles (ADMIN y LECTURA)
            inicializarSeguridad(rolRepo, permisoRepo);

            // 3. Crear Usuario Admin (Si no existe)
            if (usuarioRepo.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setEmail("admin@watts.org");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setActivo(true);

                // Buscamos el rol ADMIN que acabamos de crear/actualizar
                admin.setRol(rolRepo.findByNombre("ADMIN").orElseThrow());

                usuarioRepo.save(admin);
                System.out.println("Usuario ADMIN creado: admin / admin123");
            }
        };
    }

    private void inicializarSeguridad(RolRepository rolRepo, PermisoRepository permisoRepo) {
        // A. Definir la lista completa de permisos
        List<String> nombresPermisos = Arrays.asList(
                // Usuarios
                "USUARIO_GLOBAL",
                // Inventario
                "PRODUCTO_LEER", "PRODUCTO_CREAR", "PRODUCTO_EDITAR", "PRODUCTO_ELIMINAR",
                "VARIANTE_LEER", "VARIANTE_CREAR", "VARIANTE_EDITAR", "VARIANTE_ELIMINAR",
                "ALMACEN_LEER", "ALMACEN_CREAR", "ALMACEN_EDITAR", "ALMACEN_ELIMINAR",
                // Transacciones
                "MOVIMIENTO_LEER", "MOVIMIENTO_CREAR",
                // Proyectos
                "PROYECTO_LEER", "PROYECTO_CREAR",
                // Tallas y colores
                "TALLA_CREAR", "TALLA_EDITAR", "COLOR_CREAR", "COLOR_EDITAR"
        );

        Set<Permiso> permisosTodo = new HashSet<>();
        Set<Permiso> permisosLectura = new HashSet<>();

        // B. Crear permisos en BDD y separarlos en grupos
        for (String nombre : nombresPermisos) {
            Permiso permiso = permisoRepo.findByNombre(nombre)
                    .orElseGet(() -> {
                        Permiso p = new Permiso();
                        p.setNombre(nombre);
                        return permisoRepo.save(p);
                    });

            // Agregamos a la lista de "Todo"
            permisosTodo.add(permiso);

            // Si el permiso termina en "_LEER", lo agregamos a la lista de solo lectura
            if (nombre.endsWith("_LEER")) {
                permisosLectura.add(permiso);
            }
        }

        // C. Crear o Actualizar Rol ADMIN (Tiene TODOS los permisos)
        Rol adminRol = rolRepo.findByNombre("ADMIN").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("ADMIN");
            return r;
        });
        adminRol.setPermisos(permisosTodo);
        rolRepo.save(adminRol);

        // D. Crear o Actualizar Rol LECTURA (Tiene solo permisos _LEER)
        Rol lecturaRol = rolRepo.findByNombre("LECTURA").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("LECTURA");
            return r;
        });
        lecturaRol.setPermisos(permisosLectura);
        rolRepo.save(lecturaRol);

        // E. Crear o Actualizar Rol ESCRITURA
        Rol escrituraRol = rolRepo.findByNombre("ESCRITURA").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre("ESCRITURA");
            return r;
        });
    }

    // Métodos auxiliares de Tallas y Colores (sin cambios)
    private void inicializarTallas(TallaRepository tallaRepo) {
        Arrays.asList("XS", "S", "M", "L", "XL", "XXL", "TU").forEach(nombreTalla -> {
            if (tallaRepo.findByNombre(nombreTalla).isEmpty()) {
                Talla t = new Talla();
                t.setNombre(nombreTalla);
                tallaRepo.save(t);
            }
        });
    }

    private void inicializarColores(ColorRepository repo) {
        Arrays.asList("Negro", "Blanco", "Rojo", "Azul", "Verde").forEach(nombre -> {
            if (repo.findByNombre(nombre).isEmpty()) {
                Color c = new Color();
                c.setNombre(nombre);
                repo.save(c);
            }
        });
    }
}