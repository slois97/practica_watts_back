package org.watts.transaction.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;
import org.watts.inventory.dto.InventarioResponse;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.models.Inventario;
import org.watts.inventory.service.InventarioService;
import org.watts.shared.service.EmailService;
import org.watts.shared.service.ReportService;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.mapper.MovimientoMapper;
import org.watts.transaction.model.Movimiento;
import org.watts.transaction.repository.MovimientoRepository;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock private MovimientoRepository movimientoRepository;
    @Mock private InventarioService inventarioService;
    @Mock private MovimientoMapper movimientoMapper;
    @Mock private ReportService reportService;
    @Mock private EmailService emailService;

    @InjectMocks
    private MovimientoService movimientoService;

    @Test
    @DisplayName("Debe procesar movimiento: actualizar stock, guardar historial y enviar email")
    void procesarMovimientoTest() {
        // GIVEN
        Variante variante = new Variante(); variante.setSku("SKU-1"); variante.setPrecioCompra(10.0); variante.setPrecioVenta(20.0);
        Almacen almacen = new Almacen(); almacen.setDescripcion("Principal");
        int cantidad = 5;
        TipoMovimiento tipo = TipoMovimiento.COMPRA;
        String observaciones = "Entrada inicial";

        // Simulamos la respuesta de InventarioService (Stock actualizado)
        Inventario inventarioActualizado = new Inventario();
        inventarioActualizado.setStock(50); // Stock resultante
        when(inventarioService.updateStock(variante, almacen, cantidad, tipo)).thenReturn(inventarioActualizado);

        // Simulamos el guardado del movimiento
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(i -> i.getArgument(0));

        // Simulamos el mapper final
        InventarioResponse responseEsperada = new InventarioResponse();
        responseEsperada.setStock(50);
        when(inventarioService.mapearADTO(inventarioActualizado)).thenReturn(responseEsperada);

        // WHEN
        InventarioResponse resultado = movimientoService.procesarMovimiento(
                variante, almacen, cantidad, tipo, observaciones, null, null
        );

        // THEN
        assertNotNull(resultado);
        assertEquals(50, resultado.getStock());

        // Verificaciones clave
        verify(inventarioService).updateStock(variante, almacen, cantidad, tipo); // 1. Actualizó stock
        verify(movimientoRepository).save(any(Movimiento.class)); // 2. Guardó movimiento
        verify(emailService).enviarNotificacionMovimiento(anyString(), anyString()); // 3. Envió email
    }

    @Test
    @DisplayName("Debe listar historial con filtros")
    void verTodosLosMovimientosTest() {
        Page<Movimiento> page = new PageImpl<>(Collections.singletonList(new Movimiento()));
        when(movimientoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        movimientoService.verTodosLosMovimientos(
                Pageable.unpaged(), "Var", null, "Alm", null, TipoMovimiento.VENTA, null, null, null, null, null, null
        );

        verify(movimientoRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}