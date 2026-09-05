package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.SolicitudComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudComunidadRepository extends JpaRepository<SolicitudComunidad, Long> {
    Optional<SolicitudComunidad> findByComunidadAndUsuario(Comunidad comunidad, Usuario usuario);

    List<SolicitudComunidad> findByComunidadAndEstadoOrderByFechaSolicitudAsc(
            Comunidad comunidad,
            String estado
    );

    long countByComunidadAndEstado(Comunidad comunidad, String estado);

    boolean existsByComunidadAndUsuarioAndEstado(Comunidad comunidad, Usuario usuario, String estado);
}
