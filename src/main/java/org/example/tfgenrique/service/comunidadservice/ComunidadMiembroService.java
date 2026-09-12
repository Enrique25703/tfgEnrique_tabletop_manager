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
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.web.util.UriUtils;

@Service
public class ComunidadMiembroService {
    private static final String PREFIJO_LOGO_COMUNIDAD = "/comunitiespicks/";
    private static final List<String> LOGOS_COMUNIDAD = List.of(
            "ChatGPT Image 29 ago 2026, 23_27_08 (1).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (2).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (3).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (4).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (5).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (6).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (7).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (8).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (9).png",
            "ChatGPT Image 29 ago 2026, 23_27_08 (10).png"
    );

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
    public Long crearComunidad(Long usuarioId, String nombreComunidad, String descripcion) {
        return crearComunidad(usuarioId, nombreComunidad, descripcion, null);
    }

    @Transactional
    public Long crearComunidad(Long usuarioId, String nombreComunidad, String descripcion, String logoUrl) {
        Usuario usuario = buscarUsuario(usuarioId);
        String nombreNormalizado = normalizarTexto(nombreComunidad);
        String descripcionNormalizada = normalizarTexto(descripcion);

        if (nombreNormalizado.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre para la comunidad.");
        }
        if (nombreNormalizado.length() > 120) {
            throw new IllegalArgumentException("El nombre de la comunidad no puede superar los 120 caracteres.");
        }
        if (comunidadRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe una comunidad con ese nombre.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        Comunidad comunidad = new Comunidad();
        comunidad.setNombre(nombreNormalizado);
        comunidad.setLogoUrl(normalizarLogoComunidad(logoUrl));
        comunidad.setDescripcion(descripcionNormalizada.isBlank() ? null : descripcionNormalizada);
        comunidad.setPrivacidad(ComunidadConstantes.PRIVACIDAD_PUBLICA);
        comunidad.setActivo(true);
        comunidad.setCreadoEn(ahora);
        comunidad.setActualizadoEn(ahora);
        comunidadRepository.save(comunidad);

        AfiliacionComunidad afiliacion = new AfiliacionComunidad();
        afiliacion.setComunidad(comunidad);
        afiliacion.setUsuario(usuario);
        afiliacion.setRolComunidad(ComunidadConstantes.ROL_ADMINISTRADOR);
        afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_ACTIVA);
        afiliacion.setUnidoEn(ahora);
        afiliacionComunidadRepository.save(afiliacion);

        return comunidad.getId();
    }

    @Transactional
    public Long unirseAComunidad(Long usuarioId, Long comunidadId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Comunidad comunidad = buscarComunidad(comunidadId);

        activarAfiliacion(usuario, comunidad);
        return comunidad.getId();
    }

    @Transactional
    public void promoverAdministrador(Long usuarioActorId, Long comunidadId, Long usuarioMiembroId) {
        Usuario actor = buscarUsuario(usuarioActorId);
        Comunidad comunidad = buscarComunidad(comunidadId);
        exigirAdministrador(actor, comunidad);

        Usuario miembro = buscarUsuario(usuarioMiembroId);
        AfiliacionComunidad afiliacion = buscarAfiliacionActiva(miembro, comunidad);
        if (ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad())) {
            throw new IllegalArgumentException("El miembro ya es administrador de la comunidad.");
        }
        afiliacion.setRolComunidad(ComunidadConstantes.ROL_ADMINISTRADOR);
        afiliacionComunidadRepository.save(afiliacion);
    }

    @Transactional
    public void expulsarMiembro(Long usuarioActorId, Long comunidadId, Long usuarioMiembroId) {
        Usuario actor = buscarUsuario(usuarioActorId);
        Comunidad comunidad = buscarComunidad(comunidadId);
        exigirAdministrador(actor, comunidad);

        Usuario miembro = buscarUsuario(usuarioMiembroId);
        AfiliacionComunidad afiliacion = buscarAfiliacionActiva(miembro, comunidad);
        if (ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad())
                && contarAdministradores(comunidad) <= 1) {
            throw new IllegalArgumentException("La comunidad debe conservar al menos un administrador.");
        }
        afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_INACTIVA);
        afiliacionComunidadRepository.save(afiliacion);
    }

    @Transactional
    public void abandonarComunidad(Long usuarioId, Long comunidadId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Comunidad comunidad = buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacion = buscarAfiliacionActiva(usuario, comunidad);

        if (ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad())
                && contarAdministradores(comunidad) <= 1) {
            List<AfiliacionComunidad> miembrosActivos = buscarMiembrosComunidad(comunidad);
            if (miembrosActivos.size() == 1) {
                afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_INACTIVA);
                comunidad.setActivo(false);
                comunidad.setActualizadoEn(LocalDateTime.now());
                afiliacionComunidadRepository.save(afiliacion);
                comunidadRepository.save(comunidad);
                return;
            }
            throw new IllegalArgumentException("Debes ceder la propiedad a otro miembro antes de abandonar la comunidad.");
        }

        afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_INACTIVA);
        afiliacionComunidadRepository.save(afiliacion);
    }

    @Transactional
    public void cederPropiedad(Long usuarioActorId, Long comunidadId, Long usuarioNuevoPropietarioId) {
        Usuario actor = buscarUsuario(usuarioActorId);
        Comunidad comunidad = buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacionActor = exigirAdministrador(actor, comunidad);

        if (usuarioActorId.equals(usuarioNuevoPropietarioId)) {
            throw new IllegalArgumentException("Debes seleccionar a otro miembro de la comunidad.");
        }

        Usuario nuevoPropietario = buscarUsuario(usuarioNuevoPropietarioId);
        AfiliacionComunidad afiliacionNuevoPropietario = buscarAfiliacionActiva(nuevoPropietario, comunidad);
        if (ComunidadConstantes.esAdministrador(afiliacionNuevoPropietario.getRolComunidad())) {
            throw new IllegalArgumentException("El miembro seleccionado ya es administrador de la comunidad.");
        }

        afiliacionNuevoPropietario.setRolComunidad(ComunidadConstantes.ROL_ADMINISTRADOR);
        afiliacionActor.setRolComunidad(ComunidadConstantes.ROL_USUARIO);
        afiliacionComunidadRepository.save(afiliacionNuevoPropietario);
        afiliacionComunidadRepository.save(afiliacionActor);
    }

    @Transactional
    public void cambiarPrivacidad(Long usuarioActorId, Long comunidadId, String privacidad) {
        Comunidad comunidad = buscarComunidad(comunidadId);
        cambiarAjustes(usuarioActorId, comunidadId, privacidad, comunidad.getLogoUrl());
    }

    @Transactional
    public void cambiarAjustes(Long usuarioActorId, Long comunidadId, String privacidad, String logoUrl) {
        Usuario actor = buscarUsuario(usuarioActorId);
        Comunidad comunidad = buscarComunidad(comunidadId);
        exigirAdministrador(actor, comunidad);

        String valor = normalizarTexto(privacidad).toUpperCase();
        if (!ComunidadConstantes.PRIVACIDAD_PUBLICA.equals(valor)
                && !ComunidadConstantes.PRIVACIDAD_PRIVADA.equals(valor)) {
            throw new IllegalArgumentException("La privacidad seleccionada no es valida.");
        }
        comunidad.setPrivacidad(valor);
        comunidad.setLogoUrl(normalizarLogoComunidad(logoUrl));
        comunidad.setActualizadoEn(LocalDateTime.now());
        comunidadRepository.save(comunidad);
    }

    AfiliacionComunidad activarAfiliacion(Usuario usuario, Comunidad comunidad) {

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
            return afiliacion;
        }
        return afiliacionExistente;
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

    public int contarAdministradores(Comunidad comunidad) {
        return (int) buscarMiembrosComunidad(comunidad).stream()
                .filter(afiliacion -> ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad()))
                .count();
    }

    public boolean esAdministrador(Usuario usuario, Comunidad comunidad) {
        return ComunidadConstantes.esAdministrador(buscarAfiliacionActiva(usuario, comunidad).getRolComunidad());
    }

    public boolean perteneceActivo(Usuario usuario, Comunidad comunidad) {
        return afiliacionComunidadRepository.findByComunidadAndUsuario(comunidad, usuario)
                .map(afiliacion -> ComunidadConstantes.ESTADO_AFILIACION_ACTIVA
                        .equals(afiliacion.getEstadoAfiliacion()))
                .orElse(false);
    }

    public AfiliacionComunidad exigirAdministrador(Usuario usuario, Comunidad comunidad) {
        AfiliacionComunidad afiliacion = buscarAfiliacionActiva(usuario, comunidad);
        if (!ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad())) {
            throw new IllegalArgumentException("Solo los administradores pueden realizar esta acción.");
        }
        return afiliacion;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }

    private String normalizarLogoComunidad(String logoUrl) {
        String valor = normalizarTexto(logoUrl);
        if (valor.isBlank()) {
            return null;
        }
        String valorDecodificado;
        try {
            valorDecodificado = UriUtils.decode(valor, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("La imagen seleccionada no es valida.");
        }
        if (!valorDecodificado.startsWith(PREFIJO_LOGO_COMUNIDAD)) {
            throw new IllegalArgumentException("La imagen seleccionada no es valida.");
        }
        String nombreArchivo = valorDecodificado.substring(PREFIJO_LOGO_COMUNIDAD.length());
        if (!LOGOS_COMUNIDAD.contains(nombreArchivo)) {
            throw new IllegalArgumentException("La imagen seleccionada no pertenece al catalogo de comunidades.");
        }
        return UriUtils.encodePath(PREFIJO_LOGO_COMUNIDAD + nombreArchivo, StandardCharsets.UTF_8);
    }
}
