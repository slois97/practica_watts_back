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
import org.watts.catalog.dto.VarianteRequest;
import org.watts.catalog.dto.VarianteResponse;
import org.watts.catalog.mapper.VarianteMapper;
import org.watts.catalog.model.Color;
import org.watts.catalog.model.Producto;
import org.watts.catalog.model.Talla;
import org.watts.catalog.model.Variante;
import org.watts.catalog.repository.ColorRepository;
import org.watts.catalog.repository.ProductoRepository;
import org.watts.catalog.repository.TallaRepository;
import org.watts.catalog.repository.VarianteRepository;
import org.watts.shared.service.ReportService;
import org.watts.shared.service.StorageService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VarianteServiceTest {

    @Mock private VarianteRepository varianteRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private TallaRepository tallaRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private VarianteMapper varianteMapper;
    @Mock private StorageService storageService;
    @Mock private ReportService reportService;

    @InjectMocks
    private VarianteService varianteService;

    @Test
    @DisplayName("Debe crear una variante correctamente buscando sus dependencias")
    void crearVarianteTest() {
        // GIVEN
        VarianteRequest request = new VarianteRequest(1L, 2L, 3L, 10.0, 20.0, null);

        Producto producto = new Producto(); producto.setId(1L); producto.setCodigoBase("PROD");
        Talla talla = new Talla(); talla.setId(2L); talla.setNombre("L");
        Color color = new Color(); color.setId(3L); color.setNombre("Rojo");

        // Simulamos que existen en BDD
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(tallaRepository.findById(2L)).thenReturn(Optional.of(talla));
        when(colorRepository.findById(3L)).thenReturn(Optional.of(color));

        // Simulamos guardado
        Variante varianteGuardada = new Variante();
        varianteGuardada.setId(100L);
        varianteGuardada.setProducto(producto);
        varianteGuardada.setTalla(talla);
        varianteGuardada.setColor(color);
        // Simulamos el SKU que generaría el @PrePersist
        varianteGuardada.setSku("PROD-L-ROJ");

        when(varianteRepository.save(any(Variante.class))).thenReturn(varianteGuardada);

        VarianteResponse responseEsperada = new VarianteResponse(
                100L, "PROD-L-ROJ", 10.0, 20.0, null, "L", "Rojo", "img.jpg", true, null, null, null, null
        );
        when(varianteMapper.toResponse(any(Variante.class))).thenReturn(responseEsperada);

        // WHEN
        VarianteResponse resultado = varianteService.crearVariante(request, "img.jpg");

        // THEN
        assertNotNull(resultado);
        assertEquals("PROD-L-ROJ", resultado.sku());

        // Verificamos que se llamó a buscar las dependencias
        verify(productoRepository).findById(1L);
        verify(tallaRepository).findById(2L);
        verify(colorRepository).findById(3L);
    }

    @Test
    @DisplayName("Debe actualizar precios e imagen de variante")
    void actualizarVarianteTest() {
        // GIVEN
        Long id = 50L;
        VarianteRequest request = new VarianteRequest(1L, 2L, 3L, 15.0, 30.0, null); // Nuevos precios
        String nuevaImagen = "nueva.jpg";

        Variante varianteExistente = new Variante();
        varianteExistente.setId(id);
        varianteExistente.setImagenUrl("vieja.jpg");
        varianteExistente.setPrecioCompra(10.0);

        when(varianteRepository.findById(id)).thenReturn(Optional.of(varianteExistente));
        when(varianteRepository.save(any(Variante.class))).thenAnswer(i -> i.getArgument(0)); // Devuelve lo que guarda

        // Mock mapper
        when(varianteMapper.toResponse(any(Variante.class))).thenReturn(mock(VarianteResponse.class));

        // WHEN
        varianteService.actualizarVariante(id, request, nuevaImagen);

        // THEN
        assertEquals(15.0, varianteExistente.getPrecioCompra()); // Precio actualizado
        assertEquals("nueva.jpg", varianteExistente.getImagenUrl()); // Imagen actualizada

        verify(storageService).delete("vieja.jpg"); // Se debió borrar la anterior
        verify(varianteRepository).save(varianteExistente);
    }

    @Test
    @DisplayName("Debe listar variantes con filtros")
    void listarVariantesTest() {
        Page<Variante> page = new PageImpl<>(Collections.singletonList(new Variante()));
        when(varianteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<VarianteResponse> resultado = varianteService.listarVariantes(
                null, Pageable.unpaged(), "SKU", null, null, null, null, null
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }
}