package org.watts.catalog.model;

import jakarta.persistence.*;
import org.watts.shared.model.Auditable;

@Entity
@Table(name = "variantes",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"producto_id", "talla_id", "color_id"})
        }
        )
public class Variante extends Auditable { // Extends Auditable para auditoría automática
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "talla_id", nullable = false)
    private Talla talla;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    private Double precioCompra;
    private Double precioVenta;
    // Campo para soft delete
    private boolean activo = true;

    private String imagenUrl;

    // Constructor vacío (obligatorio en JPA/Hibernate)
    public Variante(){
    }

    // Generador de SKUs
    @PrePersist // Esto hace que cuando se le de a guardar a una variante se dispare automáticamente este metodo
    public void generarSkuAutomatico() {
        if (producto != null && talla != null && color != null) {

            // 1 Cogemos el código del producto
            String codigoProd = producto.getCodigoBase().toUpperCase();

            // 2 Cogemos la talla
            String nombreTalla = talla.getNombre().toUpperCase();

            // 3 Cogemos las 3 primeras letras del color
            String nombreColor = color.getNombre().trim().toUpperCase();
            if (nombreColor.length() > 3) {
                nombreColor = nombreColor.substring(0, 3);
            }

            // 4 Unimos
            this.sku = codigoProd + "-" + nombreTalla + "-" + nombreColor;
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Talla getTalla() {
        return talla;
    }

    public void setTalla(Talla talla) {
        this.talla = talla;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
