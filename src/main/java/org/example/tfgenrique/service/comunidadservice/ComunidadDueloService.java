package org.example.tfgenrique.service.comunidadservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.example.tfgenrique.dao.EventoRepository;
import org.example.tfgenrique.dao.InscripcionEventoRepository;
import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.user.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunidadDueloService {
    private static final String ESTADO_PARTIDA_CREADA = "CREADA";
    private static final String ESTILO_EQUILIBRADO = "EQUILIBRADO";

    private final ComunidadMiembroService miembroService;
    private final SistemaJuegoRepository sistemaJuegoRepository;
    private final EventoRepository eventoRepository;
    private final InscripcionEventoRepository inscripcionEventoRepository;
    private final PartidaRepository partidaRepository;
    private final NotificacionService notificacionService;

    public ComunidadDueloService(
            ComunidadMiembroService miembroService,
            SistemaJuegoRepository sistemaJuegoRepository,
            EventoRepository eventoRepository,
            InscripcionEventoRepository inscripcionEventoRepository,
            PartidaRepository partidaRepository,
            NotificacionService notificacionService
    ) {
        this.miembroService = miembroService;
        this.sistemaJuegoRepository = sistemaJuegoRepository;
        this.eventoRepository = eventoRepository;
        this.inscripcionEventoRepository = inscripcionEventoRepository;
        this.partidaRepository = partidaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public void crearDuelo(
            Long retadorId,
            Long comunidadId,
            Long oponenteId,
            LocalDateTime fecha,
            String lugar,
            Double latitud,
            Double longitud,
            String formatoJuego,
            String mensaje
    ) {
        Usuario retador = miembroService.buscarUsuario(retadorId);
        Usuario oponente = miembroService.buscarUsuario(oponenteId);
        Comunidad comunidad = miembroService.buscarComunidad(comunidadId);
        miembroService.buscarAfiliacionActiva(retador, comunidad);
        miembroService.buscarAfiliacionActiva(oponente, comunidad);

        if (retador.getId().equals(oponente.getId())) {
            throw new IllegalArgumentException("No puedes desafiarte a ti mismo.");
        }
        if (fecha == null || !fecha.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha del duelo debe ser futura.");
        }
        String ubicacion = normalizar(lugar);
        if (ubicacion.isBlank() || ubicacion.length() > 150) {
            throw new IllegalArgumentException("Debes indicar un lugar valido para el duelo.");
        }
        if (latitud == null || longitud == null
                || latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Debes seleccionar una ubicacion valida en el mapa.");
        }
        String codigoFormato = normalizar(formatoJuego);
        SistemaJuego sistemaJuego = sistemaJuegoRepository.findByCodigo(codigoFormato)
                .filter(sistema -> Boolean.TRUE.equals(sistema.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("El formato de juego seleccionado no esta disponible."));

        LocalDateTime ahora = LocalDateTime.now();
        Evento evento = new Evento();
        evento.setOrganizadorUsuario(retador);
        evento.setComunidad(comunidad);
        evento.setSistemaJuego(sistemaJuego);
        evento.setTitulo("Duelo: " + retador.getNombreUsuario() + " vs " + oponente.getNombreUsuario());
        evento.setDescripcion("Duelo privado pendiente de aceptacion.");
        evento.setTipoEvento(ComunidadConstantes.TIPO_EVENTO_COMUNIDAD);
        evento.setRondasPlanificadas(1);
        evento.setMaxParticipantes(2);
        evento.setUbicacion(ubicacion);
        evento.setLatitud(BigDecimal.valueOf(latitud));
        evento.setLongitud(BigDecimal.valueOf(longitud));
        evento.setFechaLimiteInscripcion(fecha);
        evento.setInicioEn(fecha);
        evento.setFinEn(fecha.plusHours(3));
        evento.setEstado(ComunidadConstantes.ESTADO_EVENTO_ABIERTO);
        evento.setCreadoEn(ahora);
        evento.setActualizadoEn(ahora);
        eventoRepository.save(evento);

        InscripcionEvento inscripcion = new InscripcionEvento();
        inscripcion.setEvento(evento);
        inscripcion.setUsuario(retador);
        inscripcion.setEstadoInscripcion(ComunidadConstantes.ESTADO_INSCRIPCION_CONFIRMADA);
        inscripcion.setInscritoEn(ahora);
        inscripcionEventoRepository.save(inscripcion);

        Partida partida = new Partida();
        partida.setEvento(evento);
        partida.setComunidad(comunidad);
        partida.setCreadoPorUsuario(retador);
        partida.setSistemaJuego(sistemaJuego);
        partida.setJugador1Usuario(retador);
        partida.setJugador1NombreSnapshot(retador.getNombreUsuario());
        partida.setJugador1PuntuacionTotal(0);
        partida.setJugador2PuntuacionTotal(0);
        partida.setEsEmpate(false);
        partida.setEstiloJuego(ESTILO_EQUILIBRADO);
        partida.setEstado(ESTADO_PARTIDA_CREADA);
        partida.setProgramadaEn(fecha);
        partida.setCreadoEn(ahora);
        partida.setActualizadoEn(ahora);
        partidaRepository.save(partida);

        notificacionService.crearDesafioPartida(retadorId, oponenteId, partida.getId(), normalizar(mensaje));
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
