package org.watts.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Clase para manejar todas las excepciones del programa
@RestControllerAdvice // Esto le dice a Spring que vigile todas las peticiones que entrar en cualquier controlador y si alguna lanza una excepción, la maneje
public class GlobalExceptionHandler {

    // Captura cuando no se encuentra algo (Error 404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> manejarNotFound(ResourceNotFoundException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", 404);
        respuesta.put("error", "Recurso no encontrado");
        respuesta.put("message", ex.getMessage());

        return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND);
    }

    // Captura access denied (Error 403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccessDenied(AccessDeniedException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", 403);
        respuesta.put("error", "Acceso denegado");
        respuesta.put("message", "No tienes permisos para realizar esta acción");

        return new ResponseEntity<>(respuesta, HttpStatus.FORBIDDEN);
    }

    // Captura error imprevisto (Error 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(Exception ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", 500);
        respuesta.put("error", "Error interno del servidor");
        respuesta.put("message", ex.getMessage());

        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Captura los errores de validación de datos @NotNull, @Min, etc (Error 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", 400);
        respuesta.put("error", "Error de validación de datos");

        // Sacamos la lista de campos que han fallado
        Map<String, String> erroresDeCampo = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Ejemplo: "precioVenta": "El precio no puede ser negativo"
            erroresDeCampo.put(error.getField(), error.getDefaultMessage());
        }
        // Añadimos la lista de errores a la respuesta
        respuesta.put("message", erroresDeCampo);

        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

}
