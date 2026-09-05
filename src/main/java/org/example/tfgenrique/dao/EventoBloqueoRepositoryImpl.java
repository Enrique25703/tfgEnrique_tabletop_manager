package org.example.tfgenrique.dao;

import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.example.tfgenrique.entity.Evento;

public class EventoBloqueoRepositoryImpl implements EventoBloqueoRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Evento> findByIdForUpdate(Long id) {
        Evento evento = entityManager.find(Evento.class, id);
        if (evento != null && entityManager.getLockMode(evento) != LockModeType.PESSIMISTIC_WRITE) {
            // Una notificación puede haber cargado el evento antes de adquirir el bloqueo.
            // refresh evita decidir con un aforo/estado antiguo del contexto de persistencia.
            // Si ya lo bloqueamos en esta transacción, conservamos sus cambios pendientes.
            entityManager.refresh(evento, LockModeType.PESSIMISTIC_WRITE);
        }
        return Optional.ofNullable(evento);
    }
}
