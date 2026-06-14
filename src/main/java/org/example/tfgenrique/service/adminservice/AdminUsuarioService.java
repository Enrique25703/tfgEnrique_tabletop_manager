package org.example.tfgenrique.service.adminservice;

import org.example.tfgenrique.dao.AfiliacionComunidadRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AdminUsuarioService {
    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_COMUNIDAD_PROPIETARIO = "PROPIETARIO";
    private static final String ESTADO_AFILIACION_ACTIVA = "ACTIVA";
    private static final String ESTADO_AFILIACION_INACTIVA = "INACTIVA";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final AfiliacionComunidadRepository afiliacionComunidadRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUsuarioService(
            UsuarioRepository usuarioRepository,
            AfiliacionComunidadRepository afiliacionComunidadRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.afiliacionComunidadRepository = afiliacionComunidadRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional(readOnly = true)
    public List<AdminService.UsuarioAdminView> listarUsuariosActivos(String busqueda) {
        String termino = normalizarBusqueda(busqueda);
        return usuarioRepository.findAllByActivoTrueOrderByNombreUsuarioAsc().stream()
                .filter(usuario -> coincideBusqueda(usuario.getNombreUsuario(), termino)
                        || coincideBusqueda(usuario.getEmail(), termino))
                .map(usuario -> new AdminService.UsuarioAdminView(
                        usuario.getId(),
                        valorSeguro(usuario.getNombreUsuario()),
                        valorSeguro(usuario.getEmail()),
                        valorSeguro(usuario.getRol()),
                        formatearFecha(usuario.getCreadoEn())
                ))
                .toList();
    }

    @Transactional
    public Usuario actualizarUsuario(Long usuarioId, AdminService.ActualizarUsuarioRequest request) {
        Usuario usuario = buscarUsuarioActivo(usuarioId);

        String nombreUsuario = normalizarTexto(request.nombreUsuario());
        String email = normalizarEmail(request.email());

        if (nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre de usuario.");
        }
        if (email.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un correo.");
        }

        Usuario usuarioNombre = usuarioRepository.findByNombreUsuarioIgnoreCase(nombreUsuario).orElse(null);
        if (usuarioNombre != null && !usuarioNombre.getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }

        Usuario usuarioEmail = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuarioEmail != null && !usuarioEmail.getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        usuario.setNombreUsuario(nombreUsuario);
        usuario.setEmail(email);
        usuario.setActualizadoEn(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void actualizarContrasena(Long usuarioId, String nuevaContrasena) {
        Usuario usuario = buscarUsuarioActivo(usuarioId);
        String contrasenaNormalizada = normalizarTexto(nuevaContrasena);

        if (contrasenaNormalizada.length() < 6) {
            throw new IllegalArgumentException("La nueva contrasena debe tener al menos 6 caracteres.");
        }

        usuario.setContrasenaHash(passwordEncoder.encode(contrasenaNormalizada));
        usuario.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desactivarUsuario(Long adminId, Long usuarioId) {
        Usuario usuario = buscarUsuarioActivo(usuarioId);

        if (usuario.getId().equals(adminId)) {
            throw new IllegalArgumentException("No puedes desactivar tu propio usuario administrador.");
        }
        if (ROL_ADMIN.equalsIgnoreCase(valorSeguro(usuario.getRol()))) {
            throw new IllegalArgumentException("No puedes desactivar otro usuario administrador desde este panel.");
        }

        List<AfiliacionComunidad> afiliacionesActivas = afiliacionComunidadRepository
                .findByUsuarioAndEstadoAfiliacionOrderByUnidoEnAsc(usuario, ESTADO_AFILIACION_ACTIVA);

        boolean esPropietarioDeComunidadActiva = afiliacionesActivas.stream()
                .anyMatch(afiliacion -> ROL_COMUNIDAD_PROPIETARIO.equalsIgnoreCase(afiliacion.getRolComunidad())
                        && afiliacion.getComunidad() != null
                        && Boolean.TRUE.equals(afiliacion.getComunidad().getActivo()));

        if (esPropietarioDeComunidadActiva) {
            throw new IllegalArgumentException("No puedes desactivar un usuario que administra una comunidad activa.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        usuario.setActivo(false);
        usuario.setActualizadoEn(ahora);
        usuarioRepository.save(usuario);

        for (AfiliacionComunidad afiliacion : afiliacionesActivas) {
            afiliacion.setEstadoAfiliacion(ESTADO_AFILIACION_INACTIVA);
        }
        afiliacionComunidadRepository.saveAll(afiliacionesActivas);
    }

    private Usuario buscarUsuarioActivo(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Debes seleccionar un usuario.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario indicado."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("El usuario seleccionado ya no esta activo.");
        }

        return usuario;
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

    private String normalizarEmail(String valor) {
        return normalizarTexto(valor).toLowerCase(Locale.ROOT);
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }
}
