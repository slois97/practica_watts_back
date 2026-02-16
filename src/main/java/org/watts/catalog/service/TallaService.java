package org.watts.catalog.service;

import org.springframework.stereotype.Service;
import org.watts.catalog.dto.TallaRequest;
import org.watts.catalog.model.Talla;
import org.watts.catalog.repository.TallaRepository;
import org.watts.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TallaService {

    private final TallaRepository tallaRepository;

    public TallaService(TallaRepository tallaRepository) {
        this.tallaRepository = tallaRepository;
    }

    // Metodo para crear una nueva talla
    public Talla crearTalla(TallaRequest request) {
        Talla talla = new Talla();
        talla.setNombre(request.nombre());
        return tallaRepository.save(talla);
    }

    // Metodo para obtener todas las tallas
    public List<Talla> listarTallas() {
        return tallaRepository.findAll();
    }

    @Transactional
    public Talla actualizarTalla(Long id, TallaRequest request) {
        Talla talla = tallaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Talla", id));

        talla.setNombre(request.nombre());

        return tallaRepository.save(talla);
    }
}
