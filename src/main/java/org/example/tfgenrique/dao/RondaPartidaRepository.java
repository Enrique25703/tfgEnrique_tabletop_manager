package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.RondaPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RondaPartidaRepository extends JpaRepository<RondaPartida, Long> {
    List<RondaPartida> findByPartidaOrderByNumeroRondaAsc(Partida partida);

    Optional<RondaPartida> findByPartidaAndNumeroRonda(Partida partida, Integer numeroRonda);
}
