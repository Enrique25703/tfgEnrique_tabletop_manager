package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Comunidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {
    List<Comunidad> findAllByActivoTrueOrderByNombreAsc();

    Optional<Comunidad> findByIdAndActivoTrue(Long id);

    boolean existsByNombreIgnoreCase(String nombre);
}
