package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.SolicitudComunidadRepository;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.SolicitudComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComunidadSolicitudService {
    private final SolicitudComunidadRepository solicitudRepository;
    private final ComunidadMiembroService miembroService;

    public ComunidadSolicitudService(
            SolicitudComunidadRepository solicitudRepository,
            ComunidadMiembroService miembroService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.miembroService = miembroService;
    }

    @Transactional
    public ResultadoUnion solicitarOUnirse(Long usuarioId, Long comunidadId) {
        Usuario usuario = miembroService.buscarUsuario(usuarioId);
        Comunidad comunidad = miembroService.buscarComunidad(comunidadId);

        if (!ComunidadConstantes.PRIVACIDAD_PRIVADA.equalsIgnoreCase(comunidad.getPrivacidad())) {
            miembroService.activarAfiliacion(usuario, comunidad);
            return new ResultadoUnion(comunidad.getId(), true);
        }

        if (miembroService.perteneceActivo(usuario, comunidad)) {
            throw new IllegalArgumentException("Ya formas parte de esta comunidad.");
        }

        SolicitudComunidad solicitud = solicitudRepository.findByComunidadAndUsuario(comunidad, usuario).orElse(null);
        if (solicitud != null && ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE.equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("Ya tienes una solicitud pendiente para esta comunidad.");
        }

        if (solicitud == null) {
            solicitud = new SolicitudComunidad();
            solicitud.setComunidad(comunidad);
            solicitud.setUsuario(usuario);
        }
        solicitud.setEstado(ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setResueltaEn(null);
        solicitudRepository.save(solicitud);
        return new ResultadoUnion(comunidad.getId(), false);
    }

    @Transactional(readOnly = true)
    public List<SolicitudComunidad> buscarPendientes(Comunidad comunidad) {
        return solicitudRepository.findByComunidadAndEstadoOrderByFechaSolicitudAsc(
                comunidad,
                ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE
        );
    }

    @Transactional(readOnly = true)
    public long contarPendientes(Comunidad comunidad) {
        return solicitudRepository.countByComunidadAndEstado(
                comunidad,
                ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE
        );
    }

    @Transactional(readOnly = true)
    public boolean tieneSolicitudPendiente(Comunidad comunidad, Usuario usuario) {
        return solicitudRepository.existsByComunidadAndUsuarioAndEstado(
                comunidad,
                usuario,
                ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE
        );
    }

    @Transactional
    public void aceptar(Long usuarioActorId, Long comunidadId, Long solicitudId) {
        SolicitudComunidad solicitud = buscarSolicitudPendiente(usuarioActorId, comunidadId, solicitudId);
        miembroService.activarAfiliacion(solicitud.getUsuario(), solicitud.getComunidad());
        solicitud.setEstado(ComunidadConstantes.ESTADO_SOLICITUD_ACEPTADA);
        solicitud.setResueltaEn(LocalDateTime.now());
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void rechazar(Long usuarioActorId, Long comunidadId, Long solicitudId) {
        SolicitudComunidad solicitud = buscarSolicitudPendiente(usuarioActorId, comunidadId, solicitudId);
        solicitud.setEstado(ComunidadConstantes.ESTADO_SOLICITUD_RECHAZADA);
        solicitud.setResueltaEn(LocalDateTime.now());
        solicitudRepository.save(solicitud);
    }

    private SolicitudComunidad buscarSolicitudPendiente(Long usuarioActorId, Long comunidadId, Long solicitudId) {
        Usuario actor = miembroService.buscarUsuario(usuarioActorId);
        Comunidad comunidad = miembroService.buscarComunidad(comunidadId);
        miembroService.exigirAdministrador(actor, comunidad);

        SolicitudComunidad solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la solicitud."));
        if (!solicitud.getComunidad().getId().equals(comunidad.getId())) {
            throw new IllegalArgumentException("La solicitud no pertenece a esta comunidad.");
        }
        if (!ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE.equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("La solicitud ya ha sido resuelta.");
        }
        return solicitud;
    }

    public record ResultadoUnion(Long comunidadId, boolean unidoDirectamente) {
    }
}
