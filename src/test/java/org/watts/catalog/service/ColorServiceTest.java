package org.watts.catalog.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.watts.catalog.dto.ColorRequest;
import org.watts.catalog.model.Color;
import org.watts.catalog.repository.ColorRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColorServiceTest {

    @Mock
    private ColorRepository colorRepository;

    @InjectMocks
    private ColorService colorService;

    @Test
    @DisplayName("Debe crear un color correctamente")
    void crearColorTest() {
        // GIVEN
        ColorRequest request = new ColorRequest("Rojo Fuego");
        Color colorGuardado = new Color();
        colorGuardado.setId(1L);
        colorGuardado.setNombre("Rojo Fuego");

        when(colorRepository.save(any(Color.class))).thenReturn(colorGuardado);

        // WHEN
        Color resultado = colorService.crearColor(request);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Rojo Fuego", resultado.getNombre());
        verify(colorRepository, times(1)).save(any(Color.class));
    }

    @Test
    @DisplayName("Debe listar todos los colores")
    void listarColoresTest() {
        // GIVEN
        Color c1 = new Color(); c1.setNombre("Azul");
        Color c2 = new Color(); c2.setNombre("Verde");
        when(colorRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        // WHEN
        List<Color> lista = colorService.listarColores();

        // THEN
        assertEquals(2, lista.size());
        assertEquals("Azul", lista.get(0).getNombre());
    }

    @Test
    @DisplayName("Debe actualizar un color existente")
    void actualizarColorTest() {
        // GIVEN
        Long id = 5L;
        ColorRequest request = new ColorRequest("Amarillo Pollo");
        Color colorExistente = new Color();
        colorExistente.setId(id);
        colorExistente.setNombre("Amarillo Viejo");

        when(colorRepository.findById(id)).thenReturn(Optional.of(colorExistente));
        when(colorRepository.save(any(Color.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Color resultado = colorService.actualizarColor(id, request);

        // THEN
        assertEquals("Amarillo Pollo", resultado.getNombre());
        verify(colorRepository).findById(id);
        verify(colorRepository).save(colorExistente);
    }
}