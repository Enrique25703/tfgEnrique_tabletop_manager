package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AfiliacionComunidadRepository extends JpaRepository<AfiliacionComunidad, Long> {
    List<AfiliacionComunidad> findByUsuarioAndEstadoAfiliacionOrderByUnidoEnAsc(Usuario usuario, String estadoAfiliacion);

    List<AfiliacionComunidad> findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(
            Comunidad comunidad,
            String estadoAfiliacion
    );

    Optional<AfiliacionComunidad> findByComunidadAndUsuario(Comunidad comunidad, Usuario usuario);
}
