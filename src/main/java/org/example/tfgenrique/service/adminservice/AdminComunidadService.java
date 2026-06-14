package org.example.tfgenrique.service.adminservice;

import org.example.tfgenrique.dao.AfiliacionComunidadRepository;
import org.example.tfgenrique.dao.ComunidadRepository;
import org.example.tfgenrique.dao.EventoRepository;
import org.example.tfgenrique.dao.InvitacionPartidaComunidadRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AdminComunidadService {
    private static final String ROL_COMUNIDAD_PROPIETARIO = "PROPIETARIO";
    private static final String ESTADO_AFILIACION_ACTIVA = "ACTIVA";
    private static final String ESTADO_AFILIACION_INACTIVA = "INACTIVA";

    private final ComunidadRepository comunidadRepository;
    private final AfiliacionComunidadRepository afiliacionComunidadRepository;
    private final EventoRepository eventoRepository;
    private final InvitacionPartidaComunidadRepository invitacionPartidaComunidadRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminComunidadService(
            ComunidadRepository comunidadRepository,
            AfiliacionComunidadRepository afiliacionComunidadRepository,
            EventoRepository eventoRepository,
            InvitacionPartidaComunidadRepository invitacionPartidaComunidadRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.comunidadRepository = comunidadRepository;
        this.afiliacionComunidadRepository = afiliacionComunidadRepository;
        this.eventoRepository = eventoRepository;
        this.invitacionPartidaComunidadRepository = invitacionPartidaComunidadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminService.ComunidadAdminView> listarComunidadesActivas(String busqueda) {
        String termino = normalizarBusqueda(busqueda);
        return comunidadRepository.findAllByActivoTrueOrderByNombreAsc().stream()
                .filter(comunidad -> coincideBusqueda(comunidad.getNombre(), termino))
                .map(this::crearResumenComunidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminService.ComunidadDetalleAdminView obtenerDetalleComunidad(Long comunidadId) {
        if (comunidadId == null) {
            return null;
        }

        Comunidad comunidad = buscarComunidadActiva(comunidadId);
        List<AfiliacionComunidad> miembrosActivos = afiliacionComunidadRepository
                .findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(comunidad, ESTADO_AFILIACION_ACTIVA);

        List<AdminService.MiembroComunidadAdminView> miembros = miembrosActivos.stream()
                .map(afiliacion -> new AdminService.MiembroComunidadAdminView(
                        afiliacion.getUsuario().getId(),
                        valorSeguro(afiliacion.getUsuario().getNombreUsuario()),
                        valorSeguro(afiliacion.getRolComunidad()),
                        !ROL_COMUNIDAD_PROPIETARIO.equalsIgnoreCase(afiliacion.getRolComunidad())
                ))
                .toList();

        return new AdminService.ComunidadDetalleAdminView(
                comunidad.getId(),
                valorSeguro(comunidad.getNombre()),
                valorSeguro(comunidad.getLogoUrl()),
                miembros
        );
    }

    @Transactional
    public Comunidad actualizarComunidad(Long comunidadId, AdminService.ActualizarComunidadRequest request) {
        Comunidad comunidad = buscarComunidadActiva(comunidadId);

        String nombre = normalizarTexto(request.nombre());
        String logoUrl = normalizarTexto(request.logoUrl());

        if (nombre.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre para la comunidad.");
        }

        Comunidad comunidadNombre = comunidadRepository.findByNombreIgnoreCase(nombre).orElse(null);
        if (comunidadNombre != null && !comunidadNombre.getId().equals(comunidad.getId())) {
            throw new IllegalArgumentException("Ya existe una comunidad con ese nombre.");
        }

        comunidad.setNombre(nombre);
        comunidad.setLogoUrl(logoUrl.isBlank() ? null : logoUrl);
        comunidad.setActualizadoEn(LocalDateTime.now());
        return comunidadRepository.save(comunidad);
    }

    @Transactional
    public void desactivarComunidad(Long comunidadId) {
        Comunidad comunidad = buscarComunidadActiva(comunidadId);
        List<AfiliacionComunidad> miembrosActivos = afiliacionComunidadRepository
                .findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(comunidad, ESTADO_AFILIACION_ACTIVA);

        comunidad.setActivo(false);
        comunidad.setActualizadoEn(LocalDateTime.now());
        comunidadRepository.save(comunidad);

        for (AfiliacionComunidad afiliacion : miembrosActivos) {
            afiliacion.setEstadoAfiliacion(ESTADO_AFILIACION_INACTIVA);
        }
        afiliacionComunidadRepository.saveAll(miembrosActivos);
    }

    @Transactional
    public void expulsarMiembro(Long comunidadId, Long usuarioId) {
        Comunidad comunidad = buscarComunidadActiva(comunidadId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el miembro seleccionado."));

        AfiliacionComunidad afiliacion = afiliacionComunidadRepository.findByComunidadAndUsuario(comunidad, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Ese usuario no pertenece a la comunidad indicada."));

        if (!ESTADO_AFILIACION_ACTIVA.equalsIgnoreCase(valorSeguro(afiliacion.getEstadoAfiliacion()))) {
            throw new IllegalArgumentException("La afiliacion del miembro ya no esta activa.");
        }
        if (ROL_COMUNIDAD_PROPIETARIO.equalsIgnoreCase(valorSeguro(afiliacion.getRolComunidad()))) {
            throw new IllegalArgumentException("No puedes expulsar al administrador de la comunidad.");
        }

        afiliacion.setEstadoAfiliacion(ESTADO_AFILIACION_INACTIVA);
        afiliacionComunidadRepository.save(afiliacion);
    }

    private AdminService.ComunidadAdminView crearResumenComunidad(Comunidad comunidad) {
        List<AfiliacionComunidad> miembrosActivos = afiliacionComunidadRepository
                .findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(comunidad, ESTADO_AFILIACION_ACTIVA);

        return new AdminService.ComunidadAdminView(
                comunidad.getId(),
                valorSeguro(comunidad.getNombre()),
                valorSeguro(comunidad.getLogoUrl()),
                miembrosActivos.size(),
                eventoRepository.findByComunidadOrderByInicioEnAsc(comunidad).size(),
                invitacionPartidaComunidadRepository.findByComunidadOrderByFechaPropuestaAscCreadaEnDesc(comunidad).size()
        );
    }

    private Comunidad buscarComunidadActiva(Long comunidadId) {
        if (comunidadId == null) {
            throw new IllegalArgumentException("Debes seleccionar una comunidad.");
        }

        return comunidadRepository.findByIdAndActivoTrue(comunidadId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la comunidad indicada."));
    }

    private String normalizarBusqueda(String valor) {
        return normalizarTexto(valor).toLowerCase(Locale.ROOT);
    }

    private boolean coincideBusqueda(String valor, String busqueda) {
        if (busqueda.isBlank()) {
            return true;
        }
        return valorSeguro(valor).toLowerCase(Locale.ROOT).contains(busqueda);
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }
}
