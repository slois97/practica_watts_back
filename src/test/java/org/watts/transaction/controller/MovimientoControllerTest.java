package org.watts.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.watts.catalog.model.Variante;
import org.watts.catalog.repository.VarianteRepository;
import org.watts.inventory.dto.InventarioResponse;
import org.watts.inventory.models.Almacen;
import org.watts.inventory.repository.AlmacenRepository;
import org.watts.security.jwt.JwtAuthEntryPoint;
import org.watts.security.jwt.JwtAuthFilter;
import org.watts.security.jwt.JwtUtils;
import org.watts.transaction.dto.MovimientoRequest;
import org.watts.transaction.enums.TipoMovimiento;
import org.watts.transaction.service.MovimientoService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimientoController.class)
@Import(MovimientoControllerTest.SecurityConfigTest.class)
class MovimientoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MovimientoService movimientoService;
    // Mocks adicionales requeridos por la lógica del Controller antes de llamar al servicio
    @MockBean private VarianteRepository varianteRepository;
    @MockBean private AlmacenRepository almacenRepository;

    // --- MOCKS DE SEGURIDAD ---
    @MockBean private JwtUtils jwtUtils;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtAuthEntryPoint jwtAuthEntryPoint;

    @TestConfiguration
    static class SecurityConfigTest {
        @Bean
        @Primary
        public JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(null, null);
        }
    }

    @Test
    @DisplayName("POST /api/movimientos - Crear movimiento correctamente")
    @WithMockUser(authorities = "MOVIMIENTO_CREAR")
    void crearMovimientoTest() throws Exception {
        MovimientoRequest request = new MovimientoRequest(
                "SKU-123", 1L, 10, TipoMovimiento.COMPRA, "Obs", 50.0, 100.0
        );

        when(varianteRepository.findBySku("SKU-123")).thenReturn(Optional.of(new Variante()));
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(new Almacen()));

        InventarioResponse response = new InventarioResponse();
        response.setStock(10);
        when(movimientoService.procesarMovimiento(any(), any(), eq(10), eq(TipoMovimiento.COMPRA), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/movimientos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/movimientos - Fallar 404 si variante no existe")
    @WithMockUser(authorities = "MOVIMIENTO_CREAR")
    void crearMovimientoVarianteNoExisteTest() throws Exception {
        MovimientoRequest request = new MovimientoRequest(
                "SKU-FALSO", 1L, 10, TipoMovimiento.COMPRA, null, null, null
        );

        when(varianteRepository.findBySku("SKU-FALSO")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/movimientos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}