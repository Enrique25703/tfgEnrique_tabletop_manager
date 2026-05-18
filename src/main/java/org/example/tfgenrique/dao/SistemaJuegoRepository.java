package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.SistemaJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SistemaJuegoRepository extends JpaRepository<SistemaJuego, Long> {
    Optional<SistemaJuego> findByCodigo(String codigo);

    Optional<SistemaJuego> findByNombre(String nombre);
}
