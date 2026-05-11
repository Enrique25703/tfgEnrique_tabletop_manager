package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // TODO: Puedes dejar solo uno de estos dos metodos si decides permitir login
    // solo por nombre de usuario o solo por email.
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByEmail(String email);
}
