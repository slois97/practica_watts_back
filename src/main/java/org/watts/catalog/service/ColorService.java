package org.watts.catalog.service;

import org.springframework.stereotype.Service;
import org.watts.catalog.dto.ColorRequest;
import org.watts.catalog.model.Color;
import org.watts.catalog.repository.ColorRepository;
import org.springframework.transaction.annotation.Transactional;
import org.watts.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class ColorService {

    private final ColorRepository colorRepository;

    public ColorService(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    // Metodo para crear un nuevo color
    public Color crearColor(ColorRequest request) {
        Color color = new Color();
        color.setNombre(request.nombre());
        return colorRepository.save(color);
    }

    // Metodo para obtener todos los colores
    public List<Color> listarColores() {
        return colorRepository.findAll();
    }

    @Transactional
    public Color actualizarColor(Long id, ColorRequest request) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Color", id));

        color.setNombre(request.nombre());

        return colorRepository.save(color);
    }
}
