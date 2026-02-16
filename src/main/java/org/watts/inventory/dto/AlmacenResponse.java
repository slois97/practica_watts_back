package org.watts.inventory.dto;

public class AlmacenResponse {

    private Long id;
    private String codigo;
    private String descripcion;
    private String ubicacionMaps;
    private boolean activo;

    //Constructor vacío
    public AlmacenResponse() {}

    //Constructor completo
    public AlmacenResponse(Long id, String codigo, String descripcion, String ubicacionMaps, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.ubicacionMaps = ubicacionMaps;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
