package org.example.tfgenrique.service.partidas;

import jakarta.persistence.EntityManager;
import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class HistorialPartidasService {
    private final PartidaRepository partidas;
    private final SistemaJuegoRepository sistemas;
    private final UsuarioRepository usuarios;
    private final EntityManager entityManager;

    public HistorialPartidasService(PartidaRepository partidas, SistemaJuegoRepository sistemas,
                                   UsuarioRepository usuarios, EntityManager entityManager) {
        this.partidas = partidas;
        this.sistemas = sistemas;
        this.usuarios = usuarios;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public HistorialView obtenerHistorial(Long usuarioId, String formato, String desde, String hasta, String resultado) {
        Usuario usuario = buscarUsuario(usuarioId);
        LocalDate fechaDesde = leerFecha(desde);
        LocalDate fechaHasta = leerFecha(hasta);
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        if (!List.of("", "VICTORIA", "DERROTA", "EMPATE").contains(resultado)) {
            throw new IllegalArgumentException("Selecciona un resultado válido.");
        }
        var formatos = sistemas.findAll(Sort.by("nombre")).stream()
                .map(s -> new FormatoView(s.getCodigo(), s.getNombre())).toList();
        if (!formato.isBlank() && formatos.stream().noneMatch(f -> f.codigo().equals(formato))) {
            throw new IllegalArgumentException("Selecciona un formato de juego válido.");
        }
        var filas = partidas.buscarHistorial(usuarioId).stream()
                .filter(p -> formato.isBlank() || formato.equals(p.getSistemaJuego().getCodigo()))
                .filter(p -> coincideFecha(fechaPartida(p), fechaDesde, fechaHasta))
                .map(p -> crearFila(p, usuarioId))
                .filter(p -> resultado.isBlank() || resultado.equals(p.resultado()))
                .toList();
        return new HistorialView(usuario.getNombreUsuario(), formatos, filas);
    }

    @Transactional
    public void eliminar(Long usuarioId, Long partidaId) {
        buscarUsuario(usuarioId);
        Partida partida = partidas.findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la partida."));
        if (!usuarioId.equals(partida.getCreadoPorUsuario().getId())) {
            throw new IllegalArgumentException("Solo puedes eliminar las partidas que has creado.");
        }
        if (!"FINALIZADA".equals(partida.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden eliminar partidas finalizadas del historial.");
        }
        // Las tres tablas referencian la partida sin borrado en cascada en el esquema existente.
        entityManager.createQuery("delete from NotificacionUsuario n where n.partida.id = :id")
                .setParameter("id", partidaId).executeUpdate();
        entityManager.createQuery("delete from ConfiguracionMisionPartida c where c.partida.id = :id")
                .setParameter("id", partidaId).executeUpdate();
        entityManager.createQuery("delete from RondaPartida r where r.partida.id = :id")
                .setParameter("id", partidaId).executeUpdate();
        partidas.delete(partida);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarios.findById(usuarioId).filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe o no está activo."));
    }

    private LocalDate leerFecha(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : LocalDate.parse(valor);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Introduce una fecha válida (aaaa-mm-dd).");
        }
    }

    private LocalDateTime fechaPartida(Partida p) {
        if (p.getFinalizadaEn() != null) return p.getFinalizadaEn();
        return p.getIniciadaEn() != null ? p.getIniciadaEn() : p.getCreadoEn();
    }

    private boolean coincideFecha(LocalDateTime fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) return desde == null && hasta == null;
        return (desde == null || !fecha.toLocalDate().isBefore(desde))
                && (hasta == null || !fecha.toLocalDate().isAfter(hasta));
    }

    private PartidaView crearFila(Partida p, Long usuarioId) {
        int puntos1 = p.getJugador1PuntuacionTotal() == null ? 0 : p.getJugador1PuntuacionTotal();
        int puntos2 = p.getJugador2PuntuacionTotal() == null ? 0 : p.getJugador2PuntuacionTotal();
        boolean jugador1 = p.getJugador1Usuario() != null && usuarioId.equals(p.getJugador1Usuario().getId());
        boolean jugador2 = p.getJugador2Usuario() != null && usuarioId.equals(p.getJugador2Usuario().getId());
        String resultado = !jugador1 && !jugador2 ? "SIN_RESULTADO"
                : puntos1 == puntos2 ? "EMPATE"
                : (jugador1 ? puntos1 > puntos2 : puntos2 > puntos1) ? "VICTORIA" : "DERROTA";
        LocalDateTime fecha = fechaPartida(p);
        return new PartidaView(p.getId(), p.getSistemaJuego().getNombre(),
                nombre(p.getJugador1NombreSnapshot(), p.getJugador1Usuario()),
                nombre(p.getJugador2NombreSnapshot(), p.getJugador2Usuario()), puntos1, puntos2,
                fecha == null ? "Sin fecha" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                resultado, usuarioId.equals(p.getCreadoPorUsuario().getId()));
    }

    private String nombre(String snapshot, Usuario usuario) {
        return snapshot != null && !snapshot.isBlank() ? snapshot
                : usuario != null ? usuario.getNombreUsuario() : "Invitado";
    }

    public record FormatoView(String codigo, String nombre) { }
    public record PartidaView(Long id, String sistemaJuego, String jugador1, String jugador2,
                              int puntos1, int puntos2, String fecha, String resultado, boolean puedeEliminar) { }
    public record HistorialView(String nombreUsuario, List<FormatoView> formatos, List<PartidaView> partidas) { }
}
