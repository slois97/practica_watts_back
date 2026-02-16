package org.watts.catalog.model;

import jakarta.persistence.*;
import org.watts.shared.model.Auditable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto extends Auditable { // Extends Auditable para auditoría automática

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoBase;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String caracteristicasTecnicas;

    private boolean activo = true;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Variante> variantes = new ArrayList<>();

    private String imagenUrl;

    // Constructor vacío (obligatorio en JPA/Hibernate)
    public Producto(){
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoBase() {
        return codigoBase;
    }

    public void setCodigoBase(String codigoBase) {
        this.codigoBase = codigoBase;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCaracteristicasTecnicas() {
        return caracteristicasTecnicas;
    }

    public void setCaracteristicasTecnicas(String caracteristicasTecnicas) {
        this.caracteristicasTecnicas = caracteristicasTecnicas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Variante> getVariantes() {
        return variantes;
    }

    public void setVariantes(List<Variante> variantes) {
        this.variantes = variantes;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    // Metodo helper opcional: ayuda a añadir variantes y mantener la relación sincronizada
    public void addVariante(Variante variante) {
        variantes.add(variante);
        variante.setProducto(this);
    }
}
