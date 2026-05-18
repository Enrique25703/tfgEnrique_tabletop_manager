package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.ListaEjercito;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListaEjercitoRepository extends JpaRepository<ListaEjercito, Long> {
    Optional<ListaEjercito> findByPropietarioUsuarioAndSistemaJuegoAndNombre(
            Usuario propietarioUsuario,
            SistemaJuego sistemaJuego,
            String nombre
    );
}
