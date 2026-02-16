package org.watts.catalog.controller;

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
import org.watts.catalog.dto.ColorRequest;
import org.watts.catalog.model.Color;
import org.watts.catalog.service.ColorService;
import org.watts.config.SecurityConfig; // <--- Importamos tu configuración real
import org.watts.security.jwt.JwtAuthEntryPoint;
import org.watts.security.jwt.JwtAuthFilter;
import org.watts.security.jwt.JwtUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ColorController.class)
// IMPORTANTE: Importamos SecurityConfig.class para activar @EnableMethodSecurity
// y nuestra configuración de test (SecurityConfigTest) para el filtro JWT
@Import({SecurityConfig.class, ColorControllerTest.SecurityConfigTest.class})
class ColorControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ColorService colorService;

    // --- MOCKS DE SEGURIDAD ---
    @MockBean private JwtUtils jwtUtils;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtAuthEntryPoint jwtAuthEntryPoint;

    // Configuración para simular el filtro JWT sin dependencias reales
    @TestConfiguration
    static class SecurityConfigTest {
        @Bean
        @Primary
        public JwtAuthFilter jwtAuthFilter() {
            return new JwtAuthFilter(null, null);
        }
    }

    @Test
    @DisplayName("GET /api/colores - Debe devolver lista 200 OK")
    @WithMockUser(authorities = "COLOR_LEER") // Asignamos un permiso válido por si acaso
    void obtenerColoresTest() throws Exception {
        Color c1 = new Color();
        c1.setId(1L);
        c1.setNombre("Negro");

        when(colorService.listarColores()).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/colores"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nombre").value("Negro"));
    }

    @Test
    @DisplayName("POST /api/colores - Debe crear color (201 Created)")
    @WithMockUser(authorities = "COLOR_CREAR") // Permiso correcto
    void crearColorTest() throws Exception {
        ColorRequest request = new ColorRequest("Blanco");
        Color colorCreado = new Color();
        colorCreado.setId(2L);
        colorCreado.setNombre("Blanco");

        when(colorService.crearColor(any(ColorRequest.class))).thenReturn(colorCreado);

        mockMvc.perform(post("/api/colores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Blanco"));
    }

    @Test
    @DisplayName("POST /api/colores - Debe fallar 403 si NO tiene permiso")
    @WithMockUser(authorities = "ROL_CUALQUIERA") // Permiso incorrecto
    void crearColorSinPermisoTest() throws Exception {
        ColorRequest request = new ColorRequest("Rosa");

        // Al importar SecurityConfig, @PreAuthorize funcionará y bloqueará la petición
        // antes de llegar al controlador, devolviendo 403.
        mockMvc.perform(post("/api/colores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/colores/{id} - Debe actualizar color")
    @WithMockUser(authorities = "COLOR_EDITAR")
    void actualizarColorTest() throws Exception {
        ColorRequest request = new ColorRequest("Naranja");
        Color colorActualizado = new Color();
        colorActualizado.setId(10L);
        colorActualizado.setNombre("Naranja");

        when(colorService.actualizarColor(eq(10L), any(ColorRequest.class))).thenReturn(colorActualizado);

        mockMvc.perform(put("/api/colores/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Naranja"));
    }
}