package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    @EntityGraph(attributePaths = {"sistemaJuego", "creadoPorUsuario", "jugador1Usuario", "jugador2Usuario"})
    @Query("""
            select p from Partida p
            where p.estado = 'FINALIZADA'
              and (p.creadoPorUsuario.id = :usuarioId or p.jugador1Usuario.id = :usuarioId
                   or p.jugador2Usuario.id = :usuarioId)
            order by coalesce(p.finalizadaEn, p.iniciadaEn, p.creadoEn) desc, p.id desc
            """)
    List<Partida> buscarHistorial(@Param("usuarioId") Long usuarioId);

    List<Partida> findByCreadoPorUsuarioOrderByCreadoEnDesc(Usuario usuario);
    List<Partida> findByJugador1UsuarioOrderByCreadoEnAsc(Usuario usuario);
}
