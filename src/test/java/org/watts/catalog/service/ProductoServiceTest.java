package org.watts.catalog.service;

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
import org.watts.catalog.dto.ProductoRequest;
import org.watts.catalog.dto.ProductoResponse;
import org.watts.catalog.mapper.ProductoMapper;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Variante;
import org.watts.catalog.repository.ProductoRepository;
import org.watts.shared.service.ReportService;
import org.watts.shared.service.StorageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private ProductoMapper productoMapper;
    @Mock private StorageService storageService; // Necesario porque se usa al crear/actualizar
    @Mock private ReportService reportService;

    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("Debe crear un producto generando el código base correctamente (ej: CAMI -> CAMI-1)")
    void crearProductoTest() {
        // GIVEN
        ProductoRequest request = new ProductoRequest("Camiseta", "CAMI", "Algodón", null);
        String imagenUrl = "img.jpg";

        // Simulamos que NO existe CAMI-1, así que usará ese
        when(productoRepository.existsByCodigoBase("CAMI-1")).thenReturn(false);

        Producto productoGuardado = new Producto();
        productoGuardado.setId(1L);
        productoGuardado.setCodigoBase("CAMI-1"); // El servicio debería haber generado esto

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        ProductoResponse responseEsperada = new ProductoResponse(1L, "Camiseta", "CAMI-1", "Algodón", "img.jpg", true, null, null, null, null);
        when(productoMapper.toResponse(any(Producto.class))).thenReturn(responseEsperada);

        // WHEN
        ProductoResponse resultado = productoService.crearProducto(request, imagenUrl);

        // THEN
        assertNotNull(resultado);
        assertEquals("CAMI-1", resultado.codigoBase());
        verify(productoRepository).save(argThat(p -> p.getCodigoBase().equals("CAMI-1")));
    }

    @Test
    @DisplayName("Debe actualizar producto y regenerar SKUs de variantes si cambia el código base")
    void actualizarProductoConCambioCodigoTest() {
        // GIVEN
        Long id = 1L;
        ProductoRequest request = new ProductoRequest("Camiseta Nueva", "NUEVO", "Tela", null);

        Producto productoExistente = new Producto();
        productoExistente.setId(id);
        productoExistente.setCodigoBase("VIEJO-1");

        // Simulamos una variante asociada para verificar que se intenta regenerar su SKU
        Variante varianteMock = mock(Variante.class);
        productoExistente.setVariantes(Collections.singletonList(varianteMock));

        when(productoRepository.findById(id)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.existsByCodigoBase("NUEVO")).thenReturn(false); // El nuevo código está libre
        when(productoRepository.save(any(Producto.class))).thenReturn(productoExistente);

        // WHEN
        productoService.actualizarProducto(id, request, null);

        // THEN
        assertEquals("NUEVO", productoExistente.getCodigoBase());
        verify(varianteMock).generarSkuAutomatico(); // Verificamos que se llamó a regenerar SKU
        verify(productoRepository).save(productoExistente);
    }

    @Test
    @DisplayName("Debe listar productos paginados")
    void listarProductosTest() {
        // GIVEN
        Page<Producto> pagina = new PageImpl<>(Collections.singletonList(new Producto()));
        when(productoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pagina);
        when(productoMapper.toResponse(any())).thenReturn(new ProductoResponse(1L, "P", "C", "D", null, true, null, null, null, null));

        // WHEN
        Page<ProductoResponse> resultado = productoService.listarProductos(Pageable.unpaged(), null, null, null, null, null);

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }
}