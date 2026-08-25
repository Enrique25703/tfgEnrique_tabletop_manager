package org.example.tfgenrique.dao;

import org.example.tfgenrique.entity.ListaEjercito;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListaEjercitoRepository extends JpaRepository<ListaEjercito, Long> {
    /**
     * Serializes version creation for an existing list within a transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select lista
            from ListaEjercito lista
            where lista.propietarioUsuario = :propietario
              and lista.sistemaJuego = :sistemaJuego
              and lista.nombre = :nombre
            """)
    Optional<ListaEjercito> findForUpdate(
            @Param("propietario") Usuario propietario,
            @Param("sistemaJuego") SistemaJuego sistemaJuego,
            @Param("nombre") String nombre
    );
}
