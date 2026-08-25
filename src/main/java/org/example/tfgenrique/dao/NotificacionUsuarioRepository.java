package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.NotificacionUsuario;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionUsuarioRepository extends JpaRepository<NotificacionUsuario, Long> {
    List<NotificacionUsuario> findByReceptorUsuarioOrderByCreadoEnDesc(Usuario receptorUsuario);

    long countByReceptorUsuarioAndEstado(Usuario receptorUsuario, String estado);

    Optional<NotificacionUsuario> findByIdAndReceptorUsuario(Long id, Usuario receptorUsuario);
}
