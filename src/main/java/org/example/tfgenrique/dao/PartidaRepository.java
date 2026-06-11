package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    List<Partida> findByCreadoPorUsuarioOrderByCreadoEnDesc(Usuario usuario);
    List<Partida> findByJugador1UsuarioOrderByCreadoEnAsc(Usuario usuario);
}
