package org.watts.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.watts.projects.model.RolProyecto;

public record InvitarUsuarioRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String username,

        @NotNull(message = "El rol es obligatorio")
        RolProyecto rol
) {
}
