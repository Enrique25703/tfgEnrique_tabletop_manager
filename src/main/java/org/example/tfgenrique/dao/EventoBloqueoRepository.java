package org.example.tfgenrique.dao;

import java.util.Optional;
import org.example.tfgenrique.entity.Evento;

public interface EventoBloqueoRepository {
    /** Debe invocarse dentro de la transacción que comprueba y modifica el aforo. */
    Optional<Evento> findByIdForUpdate(Long id);
}
