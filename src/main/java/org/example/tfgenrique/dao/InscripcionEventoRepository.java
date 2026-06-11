package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionEventoRepository extends JpaRepository<InscripcionEvento, Long> {
    boolean existsByEventoAndUsuario(Evento evento, Usuario usuario);

    long countByEvento(Evento evento);

    List<InscripcionEvento> findByEventoOrderByInscritoEnAsc(Evento evento);
}
