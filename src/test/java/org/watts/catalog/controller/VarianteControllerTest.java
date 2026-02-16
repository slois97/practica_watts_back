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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.watts.catalog.dto.VarianteRequest;
import org.watts.catalog.dto.VarianteResponse;
import org.watts.catalog.service.VarianteService;
import org.watts.security.jwt.JwtAuthEntryPoint;
import org.watts.security.jwt.JwtAuthFilter;
import org.watts.security.jwt.JwtUtils;
import org.watts.shared.service.StorageService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VarianteController.class)
@Import(VarianteControllerTest.SecurityConfigTest.class)
class VarianteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private VarianteService varianteService;
    @MockBean private StorageService storageService;

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
    @DisplayName("POST /api/variantes - Crear variante")
    @WithMockUser(authorities = "VARIANTE_CREAR")
    void crearVarianteTest() throws Exception {
        VarianteRequest request = new VarianteRequest(1L, 2L, 3L, 50.0, 100.0, null);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "variante",
                "",
                "application/json",
                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // Creamos la imagen
        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "variante.png", "image/png", "datos".getBytes()
        );

        when(storageService.store(any())).thenReturn("stored.png");

        VarianteResponse response = new VarianteResponse(
                10L, "SKU-TEST", 50.0, 100.0, null, "M", "Azul", "stored.png", true, null, null, null, null
        );

        // Simulamos la llamada al servicio
        when(varianteService.crearVariante(any(VarianteRequest.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/variantes")
                        .file(jsonPart)
                        .file(imagenPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-TEST"));
    }

    @Test
    @DisplayName("PUT /api/variantes/{id} - Actualizar variante")
    @WithMockUser(authorities = "VARIANTE_EDITAR")
    void actualizarVarianteTest() throws Exception {
        VarianteRequest request = new VarianteRequest(1L, 2L, 3L, 60.0, 120.0, null);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "variante", "", "application/json",
                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        when(varianteService.actualizarVariante(eq(10L), any(), any()))
                .thenReturn(new VarianteResponse(10L, "SKU-TEST", 60.0, 120.0, null, "M", "Azul", null, true, null, null, null, null));

        mockMvc.perform(multipart("/api/variantes/10")
                        .file(jsonPart)
                        .with(proc -> { proc.setMethod("PUT"); return proc; })
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioVenta").value(120.0));
    }
}