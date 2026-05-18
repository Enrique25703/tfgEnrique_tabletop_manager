package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.FuenteCatalogo;
import org.example.tfgenrique.entity.SistemaJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuenteCatalogoRepository extends JpaRepository<FuenteCatalogo, Long> {
    Optional<FuenteCatalogo> findFirstBySistemaJuegoAndActivoTrue(SistemaJuego sistemaJuego);
}
