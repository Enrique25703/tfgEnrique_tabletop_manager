package org.example.tfgenrique.service.adminservice;

import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private static final String ROL_ADMIN = "ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final AdminUsuarioService adminUsuarioService;
    private final AdminComunidadService adminComunidadService;

    public AdminService(
            UsuarioRepository usuarioRepository,
            AdminUsuarioService adminUsuarioService,
            AdminComunidadService adminComunidadService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.adminUsuarioService = adminUsuarioService;
        this.adminComunidadService = adminComunidadService;
    }

    @Transactional(readOnly = true)
    public AdminPaginaView prepararPagina(
            Long adminId,
            String buscarUsuario,
            String buscarComunidad,
            Long comunidadId
    ) {
        Usuario administrador = asegurarAdministrador(adminId);
        return new AdminPaginaView(
                valorSeguro(administrador.getNombreUsuario()),
                valorSeguro(buscarUsuario),
                valorSeguro(buscarComunidad),
                adminUsuarioService.listarUsuariosActivos(buscarUsuario),
                adminComunidadService.listarComunidadesActivas(buscarComunidad),
                adminComunidadService.obtenerDetalleComunidad(comunidadId)
        );
    }

    @Transactional
    public Usuario actualizarUsuario(Long adminId, Long usuarioId, ActualizarUsuarioRequest request) {
        asegurarAdministrador(adminId);
        return adminUsuarioService.actualizarUsuario(usuarioId, request);
    }

    @Transactional
    public void actualizarContrasenaUsuario(Long adminId, Long usuarioId, String nuevaContrasena) {
        asegurarAdministrador(adminId);
        adminUsuarioService.actualizarContrasena(usuarioId, nuevaContrasena);
    }

    @Transactional
    public void eliminarUsuario(Long adminId, Long usuarioId) {
        asegurarAdministrador(adminId);
        adminUsuarioService.desactivarUsuario(adminId, usuarioId);
    }

    @Transactional
    public ComunidadDetalleAdminView actualizarComunidad(
            Long adminId,
            Long comunidadId,
            ActualizarComunidadRequest request
    ) {
        asegurarAdministrador(adminId);
        adminComunidadService.actualizarComunidad(comunidadId, request);
        return adminComunidadService.obtenerDetalleComunidad(comunidadId);
    }

    @Transactional
    public void eliminarComunidad(Long adminId, Long comunidadId) {
        asegurarAdministrador(adminId);
        adminComunidadService.desactivarComunidad(comunidadId);
    }

    @Transactional
    public void eliminarMiembroComunidad(Long adminId, Long comunidadId, Long usuarioId) {
        asegurarAdministrador(adminId);
        adminComunidadService.expulsarMiembro(comunidadId, usuarioId);
    }

    private Usuario asegurarAdministrador(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("La sesion ha caducado. Inicia sesion de nuevo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta de administrador no esta activa.");
        }
        if (!ROL_ADMIN.equalsIgnoreCase(valorSeguro(usuario.getRol()))) {
            throw new IllegalArgumentException("No tienes permisos para acceder al panel de administracion.");
        }

        return usuario;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    public record AdminPaginaView(
            String nombreAdmin,
            String buscarUsuario,
            String buscarComunidad,
            List<UsuarioAdminView> usuarios,
            List<ComunidadAdminView> comunidades,
            ComunidadDetalleAdminView comunidadSeleccionada
    ) {
    }

    public record UsuarioAdminView(
            Long id,
            String nombreUsuario,
            String email,
            String rol,
            String creadoEn
    ) {
    }

    public record ComunidadAdminView(
            Long id,
            String nombre,
            String logoUrl,
            int totalMiembros,
            int totalEventos,
            int totalInvitaciones
    ) {
    }

    public record ComunidadDetalleAdminView(
            Long id,
            String nombre,
            String logoUrl,
            List<MiembroComunidadAdminView> miembros
    ) {
    }

    public record MiembroComunidadAdminView(
            Long usuarioId,
            String nombreUsuario,
            String rol,
            boolean eliminable
    ) {
    }

    public record ActualizarUsuarioRequest(
            String nombreUsuario,
            String email
    ) {
    }

    public record ActualizarComunidadRequest(
            String nombre,
            String logoUrl
    ) {
    }
}
