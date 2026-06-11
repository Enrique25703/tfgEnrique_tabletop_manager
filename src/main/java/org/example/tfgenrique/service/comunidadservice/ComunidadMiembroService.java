package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.AfiliacionComunidadRepository;
import org.example.tfgenrique.dao.ComunidadRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComunidadMiembroService {

    private final UsuarioRepository usuarioRepository;
    private final ComunidadRepository comunidadRepository;
    private final AfiliacionComunidadRepository afiliacionComunidadRepository;

    public ComunidadMiembroService(
            UsuarioRepository usuarioRepository,
            ComunidadRepository comunidadRepository,
            AfiliacionComunidadRepository afiliacionComunidadRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.comunidadRepository = comunidadRepository;
        this.afiliacionComunidadRepository = afiliacionComunidadRepository;
    }

    @Transactional
    public Long crearComunidad(Long usuarioId, String nombreComunidad) {
        Usuario usuario = buscarUsuario(usuarioId);
        String nombreNormalizado = normalizarTexto(nombreComunidad);

        if (nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre para la comunidad.");
        }
        if (comunidadRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe una comunidad con ese nombre.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        Comunidad comunidad = new Comunidad();
        comunidad.setNombre(nombreNormalizado);
        comunidad.setLogoUrl(null);
        comunidad.setActivo(true);
        comunidad.setCreadoEn(ahora);
        comunidad.setActualizadoEn(ahora);
        comunidadRepository.save(comunidad);

        AfiliacionComunidad afiliacion = new AfiliacionComunidad();
        afiliacion.setComunidad(comunidad);
        afiliacion.setUsuario(usuario);
        afiliacion.setRolComunidad(ComunidadConstantes.ROL_PROPIETARIO);
        afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_ACTIVA);
        afiliacion.setUnidoEn(ahora);
        afiliacionComunidadRepository.save(afiliacion);

        return comunidad.getId();
    }

    @Transactional
    public Long unirseAComunidad(Long usuarioId, Long comunidadId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Comunidad comunidad = buscarComunidad(comunidadId);

        AfiliacionComunidad afiliacionExistente = afiliacionComunidadRepository
                .findByComunidadAndUsuario(comunidad, usuario)
                .orElse(null);

        if (afiliacionExistente != null
                && ComunidadConstantes.ESTADO_AFILIACION_ACTIVA.equals(afiliacionExistente.getEstadoAfiliacion())) {
            throw new IllegalArgumentException("Ya formas parte de esta comunidad.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (afiliacionExistente != null) {
            afiliacionExistente.setRolComunidad(ComunidadConstantes.ROL_USUARIO);
            afiliacionExistente.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_ACTIVA);
            afiliacionExistente.setUnidoEn(ahora);
            afiliacionComunidadRepository.save(afiliacionExistente);
        } else {
            AfiliacionComunidad afiliacion = new AfiliacionComunidad();
            afiliacion.setComunidad(comunidad);
            afiliacion.setUsuario(usuario);
            afiliacion.setRolComunidad(ComunidadConstantes.ROL_USUARIO);
            afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_ACTIVA);
            afiliacion.setUnidoEn(ahora);
            afiliacionComunidadRepository.save(afiliacion);
        }

        return comunidad.getId();
    }

    public Usuario buscarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("La sesion ha caducado. Inicia sesion de nuevo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta de usuario no esta activa.");
        }

        return usuario;
    }

    public Comunidad buscarComunidad(Long comunidadId) {
        if (comunidadId == null) {
            throw new IllegalArgumentException("Debes seleccionar una comunidad.");
        }

        return comunidadRepository.findByIdAndActivoTrue(comunidadId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la comunidad."));
    }

    public AfiliacionComunidad buscarAfiliacionActiva(Usuario usuario, Comunidad comunidad) {
        AfiliacionComunidad afiliacion = afiliacionComunidadRepository
                .findByComunidadAndUsuario(comunidad, usuario)
                .orElseThrow(() -> new IllegalArgumentException("No formas parte de esta comunidad."));

        if (!ComunidadConstantes.ESTADO_AFILIACION_ACTIVA.equals(afiliacion.getEstadoAfiliacion())) {
            throw new IllegalArgumentException("Tu afiliacion a la comunidad no esta activa.");
        }

        return afiliacion;
    }

    public List<AfiliacionComunidad> buscarComunidadesUsuario(Usuario usuario) {
        return afiliacionComunidadRepository
                .findByUsuarioAndEstadoAfiliacionOrderByUnidoEnAsc(
                        usuario,
                        ComunidadConstantes.ESTADO_AFILIACION_ACTIVA
                );
    }

    public List<AfiliacionComunidad> buscarMiembrosComunidad(Comunidad comunidad) {
        return afiliacionComunidadRepository
                .findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(
                        comunidad,
                        ComunidadConstantes.ESTADO_AFILIACION_ACTIVA
                );
    }

    public int contarMiembros(Comunidad comunidad) {
        return buscarMiembrosComunidad(comunidad).size();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }
}
