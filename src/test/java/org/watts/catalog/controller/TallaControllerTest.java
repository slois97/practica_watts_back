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
import org.watts.catalog.dto.TallaRequest;
import org.watts.catalog.model.Talla;
import org.watts.catalog.service.TallaService;
import org.watts.security.jwt.JwtAuthEntryPoint;
import org.watts.security.jwt.JwtAuthFilter;
import org.watts.security.jwt.JwtUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TallaController.class)
@Import(TallaControllerTest.SecurityConfigTest.class) // Importamos nuestra configuración manual de seguridad para tests
class TallaControllerTest {

    @Autowired
    private MockMvc mockMvc; // Herramienta principal para simular peticiones HTTP

    @MockBean
    private TallaService tallaService; // Mock del servicio (lógica de negocio simulada)

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON

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
    @DisplayName("GET /api/tallas - Debe listar todas las tallas (200 OK)")
    @WithMockUser(authorities = "TALLA_LEER") // Simulamos un usuario autenticado con permiso de lectura
    void obtenerTallasTest() throws Exception {
        // GIVEN: Preparamos los datos simulados
        Talla t1 = new Talla();
        t1.setId(1L);
        t1.setNombre("S");

        // Cuando el servicio sea llamado, devolverá nuestra lista simulada
        when(tallaService.listarTallas()).thenReturn(List.of(t1));

        // WHEN & THEN: Hacemos la petición y verificamos resultados
        mockMvc.perform(get("/api/tallas"))
                .andExpect(status().isOk()) // Esperamos HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Esperamos JSON
                .andExpect(jsonPath("$[0].nombre").value("S")); // Verificamos el contenido del JSON
    }

    @Test
    @DisplayName("POST /api/tallas - Debe crear una talla correctamente (201 Created)")
    @WithMockUser(username = "admin", authorities = "TALLA_CREAR") // Simulamos usuario con permiso de crear
    void crearTallaTest() throws Exception {
        // GIVEN
        TallaRequest request = new TallaRequest("M");

        Talla tallaCreada = new Talla();
        tallaCreada.setId(10L);
        tallaCreada.setNombre("M");

        // Simulamos que al guardar, el servicio devuelve el objeto creado
        when(tallaService.crearTalla(any(TallaRequest.class))).thenReturn(tallaCreada);

        // WHEN & THEN
        mockMvc.perform(post("/api/tallas")
                        .with(csrf()) // Token CSRF es necesario en tests POST aunque esté desactivado globalmente
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Convertimos el objeto request a JSON string
                .andExpect(status().isCreated()) // Esperamos HTTP 201
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("M"));
    }

    @Test
    @DisplayName("PUT /api/tallas/{id} - Debe actualizar una talla existente (200 OK)")
    @WithMockUser(authorities = "TALLA_EDITAR") // Simulamos usuario con permiso de editar
    void actualizarTallaTest() throws Exception {
        // GIVEN
        Long idTalla = 2L;
        TallaRequest request = new TallaRequest("L");

        Talla tallaActualizada = new Talla();
        tallaActualizada.setId(idTalla);
        tallaActualizada.setNombre("L");

        when(tallaService.actualizarTalla(any(Long.class), any(TallaRequest.class)))
                .thenReturn(tallaActualizada);

        // WHEN & THEN
        mockMvc.perform(put("/api/tallas/{id}", idTalla)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("L"));
    }
}