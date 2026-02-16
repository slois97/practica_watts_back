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
import org.watts.catalog.dto.ProductoRequest;
import org.watts.catalog.dto.ProductoResponse;
import org.watts.catalog.service.ProductoService;
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

@WebMvcTest(ProductoController.class)
@Import(ProductoControllerTest.SecurityConfigTest.class)
class ProductoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProductoService productoService;
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
    @DisplayName("POST /api/productos - Crear producto con imagen")
    @WithMockUser(authorities = "PRODUCTO_CREAR")
    void crearProductoConImagenTest() throws Exception {
        ProductoRequest request = new ProductoRequest("Bici", "BIC", "Rapida", null);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "producto", "", "application/json",
                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile imagenPart = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "bytes".getBytes()
        );

        when(storageService.store(any())).thenReturn("uuid.jpg");
        when(productoService.crearProducto(any(ProductoRequest.class), eq("uuid.jpg")))
                .thenReturn(new ProductoResponse(1L, "Bici", "BIC-1", "Rapida", "uuid.jpg", true, null, null, null, null));

        mockMvc.perform(multipart("/api/productos")
                        .file(jsonPart)
                        .file(imagenPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Bici"));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - Actualizar producto")
    @WithMockUser(authorities = "PRODUCTO_EDITAR")
    void actualizarProductoTest() throws Exception {
        ProductoRequest request = new ProductoRequest("Bici Mod", "BIC", "Mejor", null);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "producto", "", "application/json",
                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        when(productoService.actualizarProducto(eq(1L), any(), any()))
                .thenReturn(new ProductoResponse(1L, "Bici Mod", "BIC-1", "Mejor", null, true, null, null, null, null));

        mockMvc.perform(multipart("/api/productos/1")
                        .file(jsonPart)
                        .with(proc -> { proc.setMethod("PUT"); return proc; }) // Override a PUT
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Bici Mod"));
    }
}