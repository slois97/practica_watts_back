package org.watts.catalog.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.watts.catalog.dto.TallaRequest;
import org.watts.catalog.model.Talla;
import org.watts.catalog.repository.TallaRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito con JUnit 5
class TallaServiceTest {

    @Mock
    private TallaRepository tallaRepository; // Simulamos el repositorio

    @InjectMocks
    private TallaService tallaService; // Inyectamos el mock en el servicio real

    @Test
    @DisplayName("Debe crear una talla correctamente")
    void crearTallaTest() {
        // GIVEN
        TallaRequest request = new TallaRequest("XL");
        Talla tallaGuardada = new Talla();
        tallaGuardada.setId(1L);
        tallaGuardada.setNombre("XL");

        // Simulamos que al guardar cualquier talla, devuelve la tallaGuardada
        when(tallaRepository.save(any(Talla.class))).thenReturn(tallaGuardada);

        // WHEN
        Talla resultado = tallaService.crearTalla(request);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("XL", resultado.getNombre());

        // Verificamos que se llamó al repositorio 1 vez
        verify(tallaRepository, times(1)).save(any(Talla.class));
    }

    @Test
    @DisplayName("Debe listar todas las tallas")
    void listarTallasTest() {
        // GIVEN
        when(tallaRepository.findAll()).thenReturn(List.of(new Talla(), new Talla()));

        // WHEN
        List<Talla> lista = tallaService.listarTallas();

        // THEN
        assertNotNull(lista);
        assertEquals(2, lista.size());
    }
}