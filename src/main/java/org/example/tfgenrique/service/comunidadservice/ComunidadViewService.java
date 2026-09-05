package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.ComunidadRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InvitacionPartidaComunidad;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.text.Normalizer;

@Service
public class ComunidadViewService {

    private static final DateTimeFormatter FORMATO_DIA_EVENTO = DateTimeFormatter
            .ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"));
    private static final DateTimeFormatter FORMATO_HORA_EVENTO = DateTimeFormatter.ofPattern("HH:mm");

    private final ComunidadRepository comunidadRepository;
    private final ComunidadMiembroService comunidadMiembroService;
    private final ComunidadEventoService comunidadEventoService;
    private final ComunidadInvitacionService comunidadInvitacionService;
    private final ComunidadSolicitudService comunidadSolicitudService;

    public ComunidadViewService(
            ComunidadRepository comunidadRepository,
            ComunidadMiembroService comunidadMiembroService,
            ComunidadEventoService comunidadEventoService,
            ComunidadInvitacionService comunidadInvitacionService,
            ComunidadSolicitudService comunidadSolicitudService
    ) {
        this.comunidadRepository = comunidadRepository;
        this.comunidadMiembroService = comunidadMiembroService;
        this.comunidadEventoService = comunidadEventoService;
        this.comunidadInvitacionService = comunidadInvitacionService;
        this.comunidadSolicitudService = comunidadSolicitudService;
    }

    @Transactional(readOnly = true)
    public ComunidadService.EventosCercanosPaginaView prepararEventosCercanos(Long usuarioId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        List<AfiliacionComunidad> afiliaciones = comunidadMiembroService.buscarComunidadesUsuario(usuario);
        Set<Long> comunidadesUsuario = afiliaciones.stream()
                .map(afiliacion -> afiliacion.getComunidad().getId())
                .collect(java.util.stream.Collectors.toSet());
        int sugerencias = (int) comunidadRepository.findAllByActivoTrueOrderByNombreAsc().stream()
                .filter(comunidad -> !comunidadesUsuario.contains(comunidad.getId()))
                .count();
        return new ComunidadService.EventosCercanosPaginaView(
                usuario.getNombreUsuario(),
                afiliaciones.size(),
                sugerencias,
                crearEventosProximos(usuario)
        );
    }

    @Transactional(readOnly = true)
    public ComunidadService.MisComunidadesPaginaView prepararMisComunidades(Long usuarioId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        List<ComunidadService.ComunidadResumenView> comunidades = comunidadMiembroService
                .buscarComunidadesUsuario(usuario).stream()
                .map(afiliacion -> crearResumenComunidad(
                        afiliacion.getComunidad(),
                        afiliacion.getRolComunidad()
                ))
                .toList();
        return new ComunidadService.MisComunidadesPaginaView(usuario.getNombreUsuario(), comunidades);
    }

    @Transactional(readOnly = true)
    public ComunidadService.DescubrirComunidadesPaginaView prepararDescubrirComunidades(
            Long usuarioId,
            Long comunidadId,
            String busqueda
    ) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Set<Long> comunidadesUsuario = comunidadMiembroService.buscarComunidadesUsuario(usuario).stream()
                .map(afiliacion -> afiliacion.getComunidad().getId())
                .collect(java.util.stream.Collectors.toSet());
        String termino = normalizarBusqueda(busqueda);
        List<Comunidad> comunidades = comunidadRepository.findAllByActivoTrueOrderByNombreAsc().stream()
                .filter(comunidad -> !comunidadesUsuario.contains(comunidad.getId()))
                .filter(comunidad -> coincideBusqueda(comunidad, termino))
                .toList();
        List<ComunidadService.ComunidadDescubrimientoView> resultados = comunidades.stream()
                .map(comunidad -> crearDescubrimientoView(comunidad, usuario))
                .toList();

        int indiceSeleccionado = 0;
        if (comunidadId != null) {
            for (int indice = 0; indice < comunidades.size(); indice++) {
                if (comunidades.get(indice).getId().equals(comunidadId)) {
                    indiceSeleccionado = indice;
                    break;
                }
            }
        }
        ComunidadService.ComunidadDetalleDescubrimientoView seleccionada = comunidades.isEmpty()
                ? null
                : new ComunidadService.ComunidadDetalleDescubrimientoView(
                        resultados.get(indiceSeleccionado),
                        crearEventosDescubrimiento(comunidades.get(indiceSeleccionado))
                );
        return new ComunidadService.DescubrirComunidadesPaginaView(
                usuario.getNombreUsuario(),
                busqueda == null ? "" : busqueda.trim(),
                resultados,
                seleccionada
        );
    }

    @Transactional(readOnly = true)
    public ComunidadService.ComunidadPaginaView prepararPagina(Long usuarioId, Long comunidadId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        List<AfiliacionComunidad> afiliaciones = comunidadMiembroService.buscarComunidadesUsuario(usuario);

        List<ComunidadService.ComunidadResumenView> misComunidades = new ArrayList<>();
        Set<Long> idsComunidadesUsuario = new HashSet<>();

        for (AfiliacionComunidad afiliacion : afiliaciones) {
            Comunidad comunidad = afiliacion.getComunidad();
            idsComunidadesUsuario.add(comunidad.getId());
            misComunidades.add(crearResumenComunidad(comunidad, afiliacion.getRolComunidad()));
        }

        List<ComunidadService.ComunidadResumenView> comunidadesDisponibles = new ArrayList<>();
        List<Comunidad> todasLasComunidades = comunidadRepository.findAllByActivoTrueOrderByNombreAsc();
        for (Comunidad comunidad : todasLasComunidades) {
            if (!idsComunidadesUsuario.contains(comunidad.getId())) {
                comunidadesDisponibles.add(crearResumenComunidad(comunidad, "NO_UNIDO"));
            }
        }

        ComunidadService.ComunidadDetalleView comunidadSeleccionada = null;
        if (!afiliaciones.isEmpty()) {
            Comunidad comunidadElegida = afiliaciones.get(0).getComunidad();
            if (comunidadId != null) {
                for (AfiliacionComunidad afiliacion : afiliaciones) {
                    if (afiliacion.getComunidad().getId().equals(comunidadId)) {
                        comunidadElegida = afiliacion.getComunidad();
                        break;
                    }
                }
            }
            comunidadSeleccionada = crearDetalleComunidad(usuario, comunidadElegida);
        }

        List<ComunidadService.EventoActualView> eventosProximos = crearEventosProximos(usuario);

        return new ComunidadService.ComunidadPaginaView(
                usuario.getNombreUsuario(),
                misComunidades,
                comunidadesDisponibles,
                comunidadSeleccionada,
                eventosProximos
        );
    }

    private List<ComunidadService.EventoActualView> crearEventosProximos(Usuario usuario) {
        List<ComunidadService.EventoActualView> eventos = new ArrayList<>();
        List<InscripcionEvento> inscripciones = comunidadEventoService
                .buscarInscripcionesProximas(usuario, LocalDateTime.now());

        for (InscripcionEvento inscripcion : inscripciones) {
            Evento evento = inscripcion.getEvento();
            boolean tieneCoordenadas = evento.getLatitud() != null && evento.getLongitud() != null;
            String codigoFormato = evento.getSistemaJuego().getCodigo();
            Comunidad comunidad = evento.getComunidad();
            eventos.add(new ComunidadService.EventoActualView(
                    evento.getId(),
                    valorSeguro(evento.getTitulo()),
                    FORMATO_DIA_EVENTO.format(evento.getInicioEn()),
                    FORMATO_HORA_EVENTO.format(evento.getInicioEn()),
                    comunidadEventoService.obtenerNombreFormato(codigoFormato),
                    codigoFormato,
                    valorSeguro(evento.getUbicacion()),
                    tieneCoordenadas ? evento.getLatitud().toPlainString() : "",
                    tieneCoordenadas ? evento.getLongitud().toPlainString() : "",
                    tieneCoordenadas,
                    comunidad == null ? "Evento independiente" : valorSeguro(comunidad.getNombre()),
                    evento.getOrganizadorUsuario() == null
                            ? "-"
                            : valorSeguro(evento.getOrganizadorUsuario().getNombreUsuario()),
                    evento.getRondasPlanificadas() == null ? 0 : evento.getRondasPlanificadas(),
                    descripcionSegura(evento.getDescripcion())
            ));
        }
        return List.copyOf(eventos);
    }

    private ComunidadService.ComunidadResumenView crearResumenComunidad(Comunidad comunidad, String rolUsuario) {
        int totalMiembros = comunidadMiembroService.contarMiembros(comunidad);
        int totalEventos = comunidadEventoService.buscarEventos(comunidad).size();

        return new ComunidadService.ComunidadResumenView(
                comunidad.getId(),
                comunidad.getNombre(),
                descripcionComunidad(comunidad),
                comunidad.getLogoUrl() == null ? "" : comunidad.getLogoUrl(),
                ComunidadConstantes.esAdministrador(rolUsuario) ? "Administrador" : "Afiliado",
                ComunidadConstantes.PRIVACIDAD_PRIVADA.equalsIgnoreCase(comunidad.getPrivacidad()),
                totalMiembros,
                totalEventos
        );
    }

    private ComunidadService.ComunidadDescubrimientoView crearDescubrimientoView(
            Comunidad comunidad,
            Usuario usuario
    ) {
        List<Evento> eventos = comunidadEventoService.buscarEventos(comunidad);
        List<String> juegos = eventos.stream()
                .map(Evento::getSistemaJuego)
                .filter(java.util.Objects::nonNull)
                .map(sistema -> nombreFormatoCorto(sistema.getCodigo()))
                .distinct()
                .limit(3)
                .toList();
        return new ComunidadService.ComunidadDescubrimientoView(
                comunidad.getId(),
                comunidad.getNombre(),
                descripcionComunidad(comunidad),
                comunidad.getLogoUrl() == null ? "" : comunidad.getLogoUrl(),
                ComunidadConstantes.PRIVACIDAD_PRIVADA.equalsIgnoreCase(comunidad.getPrivacidad()),
                comunidadMiembroService.contarMiembros(comunidad),
                eventos.size(),
                juegos,
                comunidadSolicitudService.tieneSolicitudPendiente(comunidad, usuario)
        );
    }

    private List<ComunidadService.EventoDescubrimientoView> crearEventosDescubrimiento(Comunidad comunidad) {
        LocalDateTime ahora = LocalDateTime.now();
        return comunidadEventoService.buscarEventos(comunidad).stream()
                .filter(evento -> {
                    LocalDateTime fin = evento.getFinEn() == null ? evento.getInicioEn() : evento.getFinEn();
                    return fin != null && !fin.isBefore(ahora);
                })
                .limit(3)
                .map(evento -> {
                    long inscritos = comunidadEventoService.contarInscritos(evento);
                    Integer plazas = evento.getMaxParticipantes() == null
                            ? null
                            : Math.max(0, evento.getMaxParticipantes() - (int) inscritos);
                    return new ComunidadService.EventoDescubrimientoView(
                            evento.getId(),
                            valorSeguro(evento.getTitulo()),
                            evento.getInicioEn().format(DateTimeFormatter.ofPattern("dd")),
                            evento.getInicioEn().format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-ES")))
                                    .replace(".", "").toUpperCase(Locale.forLanguageTag("es-ES")),
                            ComunidadConstantes.FORMATO_FECHA.format(evento.getInicioEn()),
                            comunidadEventoService.obtenerNombreFormato(evento.getSistemaJuego().getCodigo()),
                            valorSeguro(evento.getUbicacion()),
                            plazas
                    );
                })
                .toList();
    }

    private ComunidadService.ComunidadDetalleView crearDetalleComunidad(Usuario usuario, Comunidad comunidad) {
        AfiliacionComunidad afiliacionUsuario = comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);

        List<ComunidadService.MiembroComunidadView> miembros = new ArrayList<>();
        List<AfiliacionComunidad> afiliaciones = comunidadMiembroService.buscarMiembrosComunidad(comunidad);
        for (AfiliacionComunidad afiliacion : afiliaciones) {
            miembros.add(new ComunidadService.MiembroComunidadView(
                    afiliacion.getUsuario().getNombreUsuario(),
                    afiliacion.getRolComunidad()
            ));
        }

        List<ComunidadService.EventoComunidadView> eventos = new ArrayList<>();
        List<Evento> eventosGuardados = comunidadEventoService.buscarEventos(comunidad);
        for (Evento evento : eventosGuardados) {
            boolean unido = comunidadEventoService.usuarioInscrito(evento, usuario);
            long inscritos = comunidadEventoService.contarInscritos(evento);

            eventos.add(new ComunidadService.EventoComunidadView(
                    evento.getId(),
                    evento.getTitulo(),
                    comunidadEventoService.obtenerNombreFormato(evento.getSistemaJuego().getCodigo()),
                    ComunidadConstantes.FORMATO_FECHA.format(evento.getInicioEn()),
                    evento.getRondasPlanificadas() == null ? 0 : evento.getRondasPlanificadas(),
                    valorSeguro(evento.getUbicacion()),
                    evento.getOrganizadorUsuario().getNombreUsuario(),
                    inscritos,
                    unido,
                    !unido
            ));
        }

        List<ComunidadService.InvitacionPartidaView> invitaciones = new ArrayList<>();
        List<InvitacionPartidaComunidad> invitacionesGuardadas = comunidadInvitacionService.buscarInvitaciones(comunidad);
        for (InvitacionPartidaComunidad invitacion : invitacionesGuardadas) {
            invitaciones.add(new ComunidadService.InvitacionPartidaView(
                    invitacion.getCreadorUsuario().getNombreUsuario(),
                    comunidadEventoService.obtenerNombreFormato(invitacion.getFormatoJuego()),
                    ComunidadConstantes.FORMATO_FECHA.format(invitacion.getFechaPropuesta()),
                    invitacion.getLugar(),
                    valorSeguro(invitacion.getMensaje())
            ));
        }

        boolean propietario = ComunidadConstantes.esAdministrador(afiliacionUsuario.getRolComunidad());
        boolean usuarioNormal = ComunidadConstantes.ROL_USUARIO.equals(afiliacionUsuario.getRolComunidad());

        return new ComunidadService.ComunidadDetalleView(
                comunidad.getId(),
                comunidad.getNombre(),
                afiliacionUsuario.getRolComunidad(),
                propietario,
                usuarioNormal,
                miembros,
                eventos,
                invitaciones
        );
    }

    private String valorSeguro(String texto) {
        if (texto == null || texto.isBlank()) {
            return "-";
        }
        return texto.trim();
    }

    private String descripcionSegura(String texto) {
        if (texto == null || texto.isBlank()) {
            return "Este evento no tiene una descripción añadida.";
        }
        return texto.trim();
    }

    private String descripcionComunidad(Comunidad comunidad) {
        if (comunidad.getDescripcion() == null || comunidad.getDescripcion().isBlank()) {
            return "Comunidad de jugadores de wargames y juegos de mesa.";
        }
        return comunidad.getDescripcion().trim();
    }

    private boolean coincideBusqueda(Comunidad comunidad, String termino) {
        if (termino.isBlank()) {
            return true;
        }
        return normalizarBusqueda(comunidad.getNombre()).contains(termino)
                || normalizarBusqueda(comunidad.getDescripcion()).contains(termino);
    }

    private String normalizarBusqueda(String texto) {
        String valor = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }

    private String nombreFormatoCorto(String codigo) {
        if (ComunidadConstantes.FORMATO_40K.equals(codigo)) {
            return "40K";
        }
        if (ComunidadConstantes.FORMATO_AOS.equals(codigo)) {
            return "AoS";
        }
        return codigo == null || codigo.isBlank() ? "Wargames" : codigo;
    }
}
