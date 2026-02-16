package org.watts.transaction.model;

import jakarta.persistence.*;
import org.watts.catalog.model.Variante;
import org.watts.inventory.models.Almacen;
import org.watts.shared.model.Auditable;
import org.watts.transaction.enums.TipoMovimiento;

@Entity
@Table(name = "movimientos")
public class Movimiento extends Auditable { // Extends Auditable para auditoría automática

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Variante variante;

    @ManyToOne(optional = false)
    private Almacen almacen;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    // Cantidad que afecta al stock, si es positiva suma, si es negativa resta
    private int cantidad;

    private int stockResultante;

    @Column(columnDefinition = "TEXT") // Para que sea texto largo
    private String observaciones;

    private Double precioCompraUnitario;

    private Double precioVentaUnitario;

    private Double precioCompraTotal;

    private Double precioVentaTotal;

    // Getters y setters

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

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockResultante() {
        return stockResultante;
    }

    public void setStockResultante(int stockResultante) {
        this.stockResultante = stockResultante;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Double getPrecioCompraUnitario() {
        return precioCompraUnitario;
    }

    public void setPrecioCompraUnitario(Double precioCompraUnitario) {
        this.precioCompraUnitario = precioCompraUnitario;
    }

    public Double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(Double precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario;
    }

    public Double getPrecioCompraTotal() {
        return precioCompraTotal;
    }

    public void setPrecioCompraTotal(Double precioCompraTotal) {
        this.precioCompraTotal = precioCompraTotal;
    }

    public Double getPrecioVentaTotal() {
        return precioVentaTotal;
    }

    public void setPrecioVentaTotal(Double precioVentaTotal) {
        this.precioVentaTotal = precioVentaTotal;
    }
}
