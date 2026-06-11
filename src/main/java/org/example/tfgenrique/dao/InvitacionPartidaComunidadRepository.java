package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.InvitacionPartidaComunidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitacionPartidaComunidadRepository extends JpaRepository<InvitacionPartidaComunidad, Long> {
    List<InvitacionPartidaComunidad> findByComunidadOrderByFechaPropuestaAscCreadaEnDesc(Comunidad comunidad);
}
