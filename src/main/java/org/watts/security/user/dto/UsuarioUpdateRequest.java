package org.watts.security.user.dto;

import org.watts.security.user.model.Rol;

public class UsuarioUpdateRequest {
    private String passwordHash;
    private String email;
    private Rol rol;
    private Boolean activo;

    // Getters y Setters
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}