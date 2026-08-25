package org.example.tfgenrique.service.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.example.tfgenrique.dao.NotificacionUsuarioRepository;
import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.NotificacionUsuario;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.comunidadservice.ComunidadEventoService;
import org.example.tfgenrique.service.comunidadservice.ComunidadMiembroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    private static final String TIPO_INVITACION_COMUNIDAD = "INVITACION_COMUNIDAD";
    private static final String TIPO_INVITACION_EVENTO = "INVITACION_EVENTO";
    private static final String TIPO_DESAFIO_PARTIDA = "DESAFIO_PARTIDA";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_ACEPTADA = "ACEPTADA";
    private static final String ESTADO_RECHAZADA = "RECHAZADA";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificacionUsuarioRepository notificacionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunidadMiembroService comunidadMiembroService;
    private final ComunidadEventoService comunidadEventoService;
    private final PartidaRepository partidaRepository;

    public NotificacionService(
            NotificacionUsuarioRepository notificacionUsuarioRepository,
            UsuarioRepository usuarioRepository,
            ComunidadMiembroService comunidadMiembroService,
            ComunidadEventoService comunidadEventoService,
            PartidaRepository partidaRepository
    ) {
        this.notificacionUsuarioRepository = notificacionUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.comunidadMiembroService = comunidadMiembroService;
        this.comunidadEventoService = comunidadEventoService;
        this.partidaRepository = partidaRepository;
    }

    @Transactional(readOnly = true)
    public BandejaNotificacionesView prepararBandeja(Long usuarioId) {
        if (usuarioId == null) {
            return new BandejaNotificacionesView(0, List.of());
        }

        Usuario usuario = buscarUsuario(usuarioId);
        List<NotificacionItemView> items = notificacionUsuarioRepository.findByReceptorUsuarioOrderByCreadoEnDesc(usuario)
                .stream()
                .map(this::crearItemView)
                .toList();
        long pendientes = notificacionUsuarioRepository.countByReceptorUsuarioAndEstado(usuario, ESTADO_PENDIENTE);
        return new BandejaNotificacionesView(pendientes, items);
    }

    @Transactional
    public String aceptarNotificacion(Long usuarioId, Long notificacionId) {
        Usuario usuario = buscarUsuario(usuarioId);
        NotificacionUsuario notificacion = buscarNotificacion(notificacionId, usuario);

        if (!ESTADO_PENDIENTE.equalsIgnoreCase(notificacion.getEstado())) {
            throw new IllegalArgumentException("La notificacion ya ha sido gestionada.");
        }

        String destino = resolverAceptacion(usuario, notificacion);
        notificacion.setEstado(ESTADO_ACEPTADA);
        notificacion.setRespondidoEn(LocalDateTime.now());
        notificacionUsuarioRepository.save(notificacion);
        return destino;
    }

    @Transactional
    public void rechazarNotificacion(Long usuarioId, Long notificacionId) {
        Usuario usuario = buscarUsuario(usuarioId);
        NotificacionUsuario notificacion = buscarNotificacion(notificacionId, usuario);

        if (!ESTADO_PENDIENTE.equalsIgnoreCase(notificacion.getEstado())) {
            throw new IllegalArgumentException("La notificacion ya ha sido gestionada.");
        }

        notificacion.setEstado(ESTADO_RECHAZADA);
        notificacion.setRespondidoEn(LocalDateTime.now());
        notificacionUsuarioRepository.save(notificacion);
    }

    @Transactional
    public Long crearInvitacionComunidad(Long emisorUsuarioId, Long receptorUsuarioId, Long comunidadId, String mensajeExtra) {
        Usuario emisor = buscarUsuario(emisorUsuarioId);
        Usuario receptor = buscarUsuario(receptorUsuarioId);
        Comunidad comunidad = comunidadMiembroService.buscarComunidad(comunidadId);
        comunidadMiembroService.buscarAfiliacionActiva(emisor, comunidad);
        return guardarNotificacion(receptor, emisor, comunidad, null, null, TIPO_INVITACION_COMUNIDAD, mensajeExtra).getId();
    }

    @Transactional
    public Long crearInvitacionEvento(Long emisorUsuarioId, Long receptorUsuarioId, Long eventoId, String mensajeExtra) {
        Usuario emisor = buscarUsuario(emisorUsuarioId);
        Usuario receptor = buscarUsuario(receptorUsuarioId);
        Evento evento = comunidadEventoService.buscarEvento(eventoId);
        return guardarNotificacion(receptor, emisor, evento.getComunidad(), evento, null, TIPO_INVITACION_EVENTO, mensajeExtra).getId();
    }

    @Transactional
    public Long crearDesafioPartida(Long emisorUsuarioId, Long receptorUsuarioId, Long partidaId, String mensajeExtra) {
        Usuario emisor = buscarUsuario(emisorUsuarioId);
        Usuario receptor = buscarUsuario(receptorUsuarioId);
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la partida."));
        return guardarNotificacion(receptor, emisor, partida.getComunidad(), partida.getEvento(), partida, TIPO_DESAFIO_PARTIDA, mensajeExtra).getId();
    }

    private NotificacionUsuario guardarNotificacion(
            Usuario receptor,
            Usuario emisor,
            Comunidad comunidad,
            Evento evento,
            Partida partida,
            String tipo,
            String mensajeExtra
    ) {
        NotificacionUsuario notificacion = new NotificacionUsuario();
        notificacion.setReceptorUsuario(receptor);
        notificacion.setEmisorUsuario(emisor);
        notificacion.setComunidad(comunidad);
        notificacion.setEvento(evento);
        notificacion.setPartida(partida);
        notificacion.setTipo(tipo);
        notificacion.setEstado(ESTADO_PENDIENTE);
        notificacion.setMensajeExtra(normalizarTexto(mensajeExtra));
        notificacion.setCreadoEn(LocalDateTime.now());
        return notificacionUsuarioRepository.save(notificacion);
    }

    private String resolverAceptacion(Usuario usuario, NotificacionUsuario notificacion) {
        String tipo = normalizarTexto(notificacion.getTipo()).toUpperCase(Locale.ROOT);
        return switch (tipo) {
            case TIPO_INVITACION_COMUNIDAD -> {
                Long comunidadId = notificacion.getComunidad() == null ? null : notificacion.getComunidad().getId();
                comunidadMiembroService.unirseAComunidad(usuario.getId(), comunidadId);
                yield comunidadId == null ? "/comunidades" : "/comunidades?comunidadId=" + comunidadId;
            }
            case TIPO_INVITACION_EVENTO -> {
                Evento evento = notificacion.getEvento();
                if (evento == null) {
                    throw new IllegalArgumentException("La notificacion no referencia ningun evento.");
                }
                Long comunidadId = comunidadEventoService.unirseAEvento(usuario.getId(), evento.getId());
                yield "/comunidades?comunidadId=" + comunidadId;
            }
            case TIPO_DESAFIO_PARTIDA -> {
                Partida partida = notificacion.getPartida();
                if (partida == null) {
                    throw new IllegalArgumentException("La notificacion no referencia ninguna partida.");
                }
                aceptarDesafioPartida(usuario, partida);
                yield "/partidas/" + partida.getId() + "/jugadores";
            }
            default -> throw new IllegalArgumentException("Tipo de notificacion no soportado.");
        };
    }

    private void aceptarDesafioPartida(Usuario usuario, Partida partida) {
        if (partida.getJugador2Usuario() != null && !usuario.getId().equals(partida.getJugador2Usuario().getId())) {
            throw new IllegalArgumentException("La partida ya tiene un segundo jugador asignado.");
        }
        if (partida.getJugador1Usuario() != null && usuario.getId().equals(partida.getJugador1Usuario().getId())) {
            throw new IllegalArgumentException("No puedes aceptar un desafio a tu propia plaza de jugador.");
        }

        partida.setJugador2Usuario(usuario);
        if (normalizarTexto(partida.getJugador2NombreSnapshot()).isBlank()) {
            partida.setJugador2NombreSnapshot(usuario.getNombreUsuario());
        }
        partida.setActualizadoEn(LocalDateTime.now());
        partidaRepository.save(partida);
    }

    private NotificacionUsuario buscarNotificacion(Long notificacionId, Usuario usuario) {
        return notificacionUsuarioRepository.findByIdAndReceptorUsuario(notificacionId, usuario)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la notificacion."));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));
    }

    private NotificacionItemView crearItemView(NotificacionUsuario notificacion) {
        String tipo = normalizarTexto(notificacion.getTipo()).toUpperCase(Locale.ROOT);
        String estado = normalizarTexto(notificacion.getEstado()).toUpperCase(Locale.ROOT);
        String emisor = notificacion.getEmisorUsuario() == null ? "Alguien" : notificacion.getEmisorUsuario().getNombreUsuario();
        String mensaje = switch (tipo) {
            case TIPO_INVITACION_COMUNIDAD -> emisor + " te ha invitado a la comunidad "
                    + nombreComunidad(notificacion.getComunidad()) + ".";
            case TIPO_INVITACION_EVENTO -> emisor + " te ha invitado a un evento "
                    + nombreEvento(notificacion.getEvento()) + " con fecha " + fechaEvento(notificacion.getEvento()) + ".";
            case TIPO_DESAFIO_PARTIDA -> emisor + " te ha desafiado a una partida con fecha "
                    + fechaPartida(notificacion.getPartida()) + ".";
            default -> "Tienes una notificacion pendiente.";
        };

        return new NotificacionItemView(
                notificacion.getId(),
                mensaje,
                notificacion.getMensajeExtra(),
                ESTADO_PENDIENTE.equalsIgnoreCase(estado),
                estado
        );
    }

    private String nombreComunidad(Comunidad comunidad) {
        return comunidad == null ? "desconocida" : comunidad.getNombre();
    }

    private String nombreEvento(Evento evento) {
        return evento == null ? "desconocido" : evento.getTitulo();
    }

    private String fechaEvento(Evento evento) {
        return evento == null || evento.getInicioEn() == null ? "sin fecha" : FORMATO_FECHA.format(evento.getInicioEn());
    }

    private String fechaPartida(Partida partida) {
        LocalDateTime fecha = partida == null ? null : partida.getProgramadaEn();
        if (fecha == null && partida != null) {
            fecha = partida.getCreadoEn();
        }
        return fecha == null ? "sin fecha" : FORMATO_FECHA.format(fecha);
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public record BandejaNotificacionesView(long totalPendientes, List<NotificacionItemView> items) {
    }

    public record NotificacionItemView(
            Long id,
            String mensaje,
            String mensajeExtra,
            boolean pendiente,
            String estado
    ) {
    }
}
