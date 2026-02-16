package org.watts.inventory.models;

import jakarta.persistence.*;
import org.watts.catalog.model.Variante;

@Entity
@Table(name = "inventario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"variante_id", "almacen_id"}))

public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Variante variante;

    @ManyToOne(optional = false)
    private Almacen almacen;

    private int stock;

    //getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Variante getVariante() {
        return variante;
    }

    public void setVariante(Variante variante) {
        this.variante = variante;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
