package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InvitacionPartidaComunidad;
import org.example.tfgenrique.entity.SolicitudComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ComunidadVisorService {
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter
            .ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"));
    private static final DateTimeFormatter FECHA_EVENTO = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-ES"));
    private static final DateTimeFormatter FECHA_FORMULARIO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ComunidadMiembroService miembroService;
    private final ComunidadSolicitudService solicitudService;
    private final ComunidadEventoService eventoService;
    private final ComunidadInvitacionService invitacionService;

    public ComunidadVisorService(
            ComunidadMiembroService miembroService,
            ComunidadSolicitudService solicitudService,
            ComunidadEventoService eventoService,
            ComunidadInvitacionService invitacionService
    ) {
        this.miembroService = miembroService;
        this.solicitudService = solicitudService;
        this.eventoService = eventoService;
        this.invitacionService = invitacionService;
    }

    @Transactional(readOnly = true)
    public MiembrosPaginaView prepararMiembros(Long usuarioId, Long comunidadId) {
        Contexto contexto = cargarContexto(usuarioId, comunidadId);
        List<AfiliacionComunidad> afiliaciones = miembroService.buscarMiembrosComunidad(contexto.comunidad());
        int totalAdministradores = miembroService.contarAdministradores(contexto.comunidad());
        List<MiembroView> miembros = afiliaciones.stream().map(afiliacion -> {
            Usuario miembro = afiliacion.getUsuario();
            boolean miembroAdministrador = ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad());
            boolean esUsuarioActual = miembro.getId().equals(contexto.usuario().getId());
            boolean puedeExpulsar = contexto.administrador()
                    && !(esUsuarioActual && miembroAdministrador && totalAdministradores <= 1);
            return new MiembroView(
                    miembro.getId(),
                    valorSeguro(miembro.getNombreUsuario(), "Usuario"),
                    valorSeguro(miembro.getFotoUrl(), ""),
                    FECHA_CORTA.format(afiliacion.getUnidoEn()),
                    nombreRol(afiliacion.getRolComunidad()),
                    miembroAdministrador,
                    esUsuarioActual,
                    contexto.administrador() && !miembroAdministrador,
                    contexto.administrador() && !esUsuarioActual && !miembroAdministrador,
                    puedeExpulsar
            );
        }).toList();

        return new MiembrosPaginaView(crearCabecera(contexto), miembros, contexto.administrador());
    }

    @Transactional(readOnly = true)
    public SolicitudesPaginaView prepararSolicitudes(Long usuarioId, Long comunidadId) {
        Contexto contexto = cargarContexto(usuarioId, comunidadId);
        if (!contexto.administrador()) {
            throw new IllegalArgumentException("Solo los administradores pueden consultar las solicitudes de la comunidad.");
        }
        boolean publica = esPublica(contexto.comunidad());
        List<SolicitudView> solicitudes = publica
                ? List.of()
                : solicitudService.buscarPendientes(contexto.comunidad()).stream()
                        .map(this::crearSolicitudView)
                        .toList();
        return new SolicitudesPaginaView(
                crearCabecera(contexto),
                publica,
                contexto.administrador(),
                solicitudes
        );
    }

    @Transactional(readOnly = true)
    public EventosPaginaView prepararEventos(Long usuarioId, Long comunidadId) {
        Contexto contexto = cargarContexto(usuarioId, comunidadId);
        List<EventoView> eventos = eventoService.buscarEventos(contexto.comunidad()).stream()
                .map(evento -> crearEventoView(contexto, evento))
                .toList();
        List<ComunidadService.InvitacionPartidaView> invitaciones = invitacionService
                .buscarInvitaciones(contexto.comunidad()).stream()
                .map(this::crearInvitacionView)
                .toList();
        boolean puedeCrearInvitacion = ComunidadConstantes.ROL_USUARIO
                .equals(contexto.afiliacion().getRolComunidad());
        return new EventosPaginaView(
                crearCabecera(contexto),
                eventos,
                true,
                invitaciones,
                puedeCrearInvitacion
        );
    }

    @Transactional(readOnly = true)
    public AjustesPaginaView prepararAjustes(Long usuarioId, Long comunidadId) {
        Contexto contexto = cargarContexto(usuarioId, comunidadId);
        if (!contexto.administrador()) {
            throw new IllegalArgumentException("Solo los administradores pueden modificar los ajustes de la comunidad.");
        }
        return new AjustesPaginaView(crearCabecera(contexto));
    }

    private Contexto cargarContexto(Long usuarioId, Long comunidadId) {
        Usuario usuario = miembroService.buscarUsuario(usuarioId);
        Comunidad comunidad = miembroService.buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacion = miembroService.buscarAfiliacionActiva(usuario, comunidad);
        return new Contexto(
                usuario,
                comunidad,
                afiliacion,
                ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad())
        );
    }

    private CabeceraComunidadView crearCabecera(Contexto contexto) {
        Comunidad comunidad = contexto.comunidad();
        return new CabeceraComunidadView(
                comunidad.getId(),
                valorSeguro(comunidad.getNombre(), "Comunidad"),
                valorSeguro(comunidad.getDescripcion(), "Esta comunidad todavía no tiene descripción."),
                valorSeguro(comunidad.getLogoUrl(), ""),
                miembroService.contarMiembros(comunidad),
                FECHA_CORTA.format(comunidad.getCreadoEn()),
                esPublica(comunidad) ? "Pública" : "Privada",
                nombreRol(contexto.afiliacion().getRolComunidad()),
                contexto.administrador(),
                solicitudService.contarPendientes(comunidad)
        );
    }

    private SolicitudView crearSolicitudView(SolicitudComunidad solicitud) {
        return new SolicitudView(
                solicitud.getId(),
                solicitud.getUsuario().getId(),
                valorSeguro(solicitud.getUsuario().getNombreUsuario(), "Usuario"),
                valorSeguro(solicitud.getUsuario().getFotoUrl(), ""),
                FECHA_CORTA.format(solicitud.getFechaSolicitud())
        );
    }

    private EventoView crearEventoView(Contexto contexto, Evento evento) {
        boolean tieneCoordenadas = evento.getLatitud() != null && evento.getLongitud() != null;
        long participantes = eventoService.contarInscritos(evento);
        boolean inscrito = eventoService.usuarioInscrito(evento, contexto.usuario());
        return new EventoView(
                evento.getId(),
                valorSeguro(evento.getTitulo(), "Evento"),
                valorSeguro(evento.getDescripcion(), "Sin descripción."),
                FECHA_EVENTO.format(evento.getInicioEn()),
                FECHA_FORMULARIO.format(evento.getInicioEn()),
                eventoService.obtenerNombreFormato(evento.getSistemaJuego().getCodigo()),
                evento.getSistemaJuego().getCodigo(),
                valorSeguro(evento.getUbicacion(), "-"),
                tieneCoordenadas ? evento.getLatitud().toPlainString() : "",
                tieneCoordenadas ? evento.getLongitud().toPlainString() : "",
                tieneCoordenadas,
                valorSeguro(evento.getOrganizadorUsuario().getNombreUsuario(), "-"),
                evento.getRondasPlanificadas() == null ? 0 : evento.getRondasPlanificadas(),
                evento.getMaxParticipantes(),
                participantes,
                inscrito,
                !inscrito && eventoService.permiteInscripcion(evento, participantes),
                eventoService.puedeGestionar(contexto.afiliacion(), contexto.usuario(), evento)
        );
    }

    private ComunidadService.InvitacionPartidaView crearInvitacionView(InvitacionPartidaComunidad invitacion) {
        return new ComunidadService.InvitacionPartidaView(
                valorSeguro(invitacion.getCreadorUsuario().getNombreUsuario(), "Usuario"),
                eventoService.obtenerNombreFormato(invitacion.getFormatoJuego()),
                FECHA_EVENTO.format(invitacion.getFechaPropuesta()),
                valorSeguro(invitacion.getLugar(), "-"),
                valorSeguro(invitacion.getMensaje(), "Sin mensaje.")
        );
    }

    private boolean esPublica(Comunidad comunidad) {
        return !ComunidadConstantes.PRIVACIDAD_PRIVADA.equalsIgnoreCase(comunidad.getPrivacidad());
    }

    private String nombreRol(String rol) {
        return ComunidadConstantes.esAdministrador(rol) ? "Administrador" : "Afiliado";
    }

    private String valorSeguro(String valor, String defecto) {
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }

    private record Contexto(
            Usuario usuario,
            Comunidad comunidad,
            AfiliacionComunidad afiliacion,
            boolean administrador
    ) {
    }

    public record CabeceraComunidadView(
            Long id,
            String nombre,
            String descripcion,
            String logoUrl,
            int totalMiembros,
            String creadaEn,
            String privacidad,
            String rolUsuario,
            boolean administrador,
            long totalSolicitudesPendientes
    ) {
    }

    public record MiembrosPaginaView(
            CabeceraComunidadView cabecera,
            List<MiembroView> miembros,
            boolean puedeGestionar
    ) {
    }

    public record MiembroView(
            Long usuarioId,
            String nombreUsuario,
            String fotoUrl,
            String miembroDesde,
            String rol,
            boolean administrador,
            boolean usuarioActual,
            boolean puedePromover,
            boolean puedeCederPropiedad,
            boolean puedeExpulsar
    ) {
    }

    public record SolicitudesPaginaView(
            CabeceraComunidadView cabecera,
            boolean publica,
            boolean puedeGestionar,
            List<SolicitudView> solicitudes
    ) {
    }

    public record SolicitudView(
            Long solicitudId,
            Long usuarioId,
            String nombreUsuario,
            String fotoUrl,
            String fechaSolicitud
    ) {
    }

    public record EventosPaginaView(
            CabeceraComunidadView cabecera,
            List<EventoView> eventos,
            boolean puedeCrear,
            List<ComunidadService.InvitacionPartidaView> invitaciones,
            boolean puedeCrearInvitacion
    ) {
    }

    public record AjustesPaginaView(CabeceraComunidadView cabecera) {
    }

    public record EventoView(
            Long id,
            String titulo,
            String descripcion,
            String fecha,
            String fechaFormulario,
            String formato,
            String codigoFormato,
            String ubicacion,
            String latitud,
            String longitud,
            boolean tieneCoordenadas,
            String organizador,
            int rondas,
            Integer maxParticipantes,
            long participantes,
            boolean inscrito,
            boolean puedeInscribirse,
            boolean puedeGestionar
    ) {
    }
}
