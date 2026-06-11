package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.ComunidadRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InvitacionPartidaComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ComunidadViewService {

    private final ComunidadRepository comunidadRepository;
    private final ComunidadMiembroService comunidadMiembroService;
    private final ComunidadEventoService comunidadEventoService;
    private final ComunidadInvitacionService comunidadInvitacionService;

    public ComunidadViewService(
            ComunidadRepository comunidadRepository,
            ComunidadMiembroService comunidadMiembroService,
            ComunidadEventoService comunidadEventoService,
            ComunidadInvitacionService comunidadInvitacionService
    ) {
        this.comunidadRepository = comunidadRepository;
        this.comunidadMiembroService = comunidadMiembroService;
        this.comunidadEventoService = comunidadEventoService;
        this.comunidadInvitacionService = comunidadInvitacionService;
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

        return new ComunidadService.ComunidadPaginaView(
                usuario.getNombreUsuario(),
                misComunidades,
                comunidadesDisponibles,
                comunidadSeleccionada
        );
    }

    private ComunidadService.ComunidadResumenView crearResumenComunidad(Comunidad comunidad, String rolUsuario) {
        int totalMiembros = comunidadMiembroService.contarMiembros(comunidad);
        int totalEventos = comunidadEventoService.buscarEventos(comunidad).size();

        return new ComunidadService.ComunidadResumenView(
                comunidad.getId(),
                comunidad.getNombre(),
                rolUsuario,
                totalMiembros,
                totalEventos
        );
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

        boolean propietario = ComunidadConstantes.ROL_PROPIETARIO.equals(afiliacionUsuario.getRolComunidad());
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
}
