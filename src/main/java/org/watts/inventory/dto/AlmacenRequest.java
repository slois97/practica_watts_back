package org.watts.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public class AlmacenRequest {
    //Hace que el codigo sea obligatorio
    @NotBlank(message = "El código del almacén es obligatorio")
    private String codigo;

    private String descripcion;

    private String ubicacionMaps;


    //Getters y setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacionMaps() {
        return ubicacionMaps;
    }

    public void setUbicacionMaps(String ubicacionMaps) {
        this.ubicacionMaps = ubicacionMaps;
    }
}
