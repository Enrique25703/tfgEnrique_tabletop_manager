package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface InscripcionEventoRepository extends JpaRepository<InscripcionEvento, Long> {
    boolean existsByEventoAndUsuario(Evento evento, Usuario usuario);

    Optional<InscripcionEvento> findByEventoAndUsuario(Evento evento, Usuario usuario);

    long countByEvento(Evento evento);

    /**
     * Lectura actual de InnoDB, incluso bajo REPEATABLE READ con una instantánea previa.
     * Adquirir primero el bloqueo de Evento; no sustituir por un COUNT sin bloqueo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InscripcionEvento i where i.evento = :evento order by i.id")
    List<InscripcionEvento> buscarInscripcionesParaActualizar(@Param("evento") Evento evento);

    List<InscripcionEvento> findByEventoOrderByInscritoEnAsc(Evento evento);

    @Query("""
            select inscripcion
            from InscripcionEvento inscripcion
            join fetch inscripcion.evento evento
            join fetch evento.organizadorUsuario
            join fetch evento.sistemaJuego
            left join fetch evento.comunidad
            where inscripcion.usuario = :usuario
              and inscripcion.estadoInscripcion = :estado
              and coalesce(evento.finEn, evento.inicioEn) >= :inicioDesde
              and evento.estado <> 'CANCELADO'
            order by evento.inicioEn asc
            """)
    List<InscripcionEvento> buscarProximasDelUsuario(
            @Param("usuario") Usuario usuario,
            @Param("estado") String estadoInscripcion,
            @Param("inicioDesde") LocalDateTime inicioDesde
    );
}
