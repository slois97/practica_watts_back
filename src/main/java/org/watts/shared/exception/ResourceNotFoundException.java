package org.watts.shared.exception;

// Extends RuntimeException para evitar tener que usar try-catch
public class ResourceNotFoundException extends RuntimeException {

    // Se usa para los ID, que son Long
    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + "no encontrado con el ID: " + id);
    }

    // Se usa para el SKU, que es String
    public ResourceNotFoundException(String recurso, String sku) { super(recurso + "no encontrado con el ID: " + sku); }
}
