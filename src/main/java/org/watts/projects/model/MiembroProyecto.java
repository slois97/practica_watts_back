package org.watts.projects.model;

import jakarta.persistence.*;
import org.watts.security.user.model.Usuario;
import org.watts.shared.model.Auditable;

@Entity
@Table(name = "miembros_proyecto", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"proyecto_id", "usuario_id"})
})
public class MiembroProyecto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolProyecto rol;

    // Constructor vacío (obligatorio en JPA/Hibernate)
    public MiembroProyecto() {

    }

    // Constructor con parámetros
    public MiembroProyecto(Proyecto proyecto, Usuario usuario, RolProyecto rol) {
        this.proyecto = proyecto;
        this.usuario = usuario;
        this.rol = rol;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public RolProyecto getRol() {
        return rol;
    }

    public void setRol(RolProyecto rol) {
        this.rol = rol;
    }
}
