package org.example.tfgenrique.service.user;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

@Service
public class EditorPerfilService {
    private static final String AVATAR_DEFAULT = "/images/default-avatar.svg";
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public EditorPerfilService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.resourceResolver = new PathMatchingResourcePatternResolver();
    }

    @Transactional(readOnly = true)
    public PerfilView prepararPerfil(Long usuarioId) {
        Usuario usuario = buscarUsuarioActivo(usuarioId);
        List<AvatarPerfilView> avataresDisponibles = cargarAvataresDisponibles(usuario.getFotoUrl());
        return new PerfilView(
                usuario.getId(),
                valorSeguro(usuario.getNombreUsuario()),
                valorSeguro(usuario.getEmail()),
                valorSeguro(usuario.getFotoUrl()),
                formatearFotoParaVista(usuario.getFotoUrl()),
                avataresDisponibles
        );
    }

    @Transactional
    public PerfilActualizadoView actualizarPerfil(Long usuarioId, PerfilRequest request) {
        Usuario usuario = buscarUsuarioActivo(usuarioId);

        String nombreUsuario = normalizarTexto(request.nombreUsuario());
        String email = normalizarEmail(request.email());
        String fotoUrl = normalizarFotoUrl(request.fotoUrl(), usuario.getFotoUrl());

        validarDatosBasicos(usuario, nombreUsuario, email);
        validarCambioContrasenaSiAplica(usuario, request);

        usuario.setNombreUsuario(nombreUsuario);
        usuario.setEmail(email);
        usuario.setFotoUrl(fotoUrl);

        String nuevaContrasena = normalizarTexto(request.nuevaContrasena());
        if (!nuevaContrasena.isBlank()) {
            usuario.setContrasenaHash(passwordEncoder.encode(nuevaContrasena));
        }

        usuario.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return new PerfilActualizadoView(
                usuario.getNombreUsuario(),
                valorSeguro(usuario.getEmail()),
                valorSeguro(usuario.getFotoUrl()),
                formatearFotoParaVista(usuario.getFotoUrl())
        );
    }

    private Usuario buscarUsuarioActivo(Long usuarioId) {
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

    private void validarDatosBasicos(Usuario usuario, String nombreUsuario, String email) {
        if (nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre de usuario.");
        }
        if (email.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un correo.");
        }

        Usuario usuarioNombre = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
        if (usuarioNombre != null && !usuarioNombre.getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }

        Usuario usuarioEmail = usuarioRepository.findByEmail(email).orElse(null);
        if (usuarioEmail != null && !usuarioEmail.getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }
    }

    private void validarCambioContrasenaSiAplica(Usuario usuario, PerfilRequest request) {
        String contrasenaActual = normalizarTexto(request.contrasenaActual());
        String nuevaContrasena = normalizarTexto(request.nuevaContrasena());
        String repetirNuevaContrasena = normalizarTexto(request.repetirNuevaContrasena());

        boolean quiereCambiarContrasena = !contrasenaActual.isBlank()
                || !nuevaContrasena.isBlank()
                || !repetirNuevaContrasena.isBlank();

        if (!quiereCambiarContrasena) {
            return;
        }
        if (contrasenaActual.isBlank()) {
            throw new IllegalArgumentException("Debes indicar tu contrasena actual para cambiarla.");
        }
        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("La contrasena actual no es correcta.");
        }
        if (nuevaContrasena.isBlank() || repetirNuevaContrasena.isBlank()) {
            throw new IllegalArgumentException("Debes escribir la nueva contrasena dos veces.");
        }
        if (!nuevaContrasena.equals(repetirNuevaContrasena)) {
            throw new IllegalArgumentException("Las nuevas contrasenas no coinciden.");
        }
        if (nuevaContrasena.length() < 6) {
            throw new IllegalArgumentException("La nueva contrasena debe tener al menos 6 caracteres.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarEmail(String email) {
        return normalizarTexto(email).toLowerCase(Locale.ROOT);
    }

    private String normalizarFotoUrl(String fotoUrl, String fotoActual) {
        String valor = normalizarTexto(fotoUrl);
        if (valor.isBlank()) {
            return null;
        }
        Set<String> urlsPermitidas = cargarUrlsAvataresPermitidas();
        if (!urlsPermitidas.contains(valor)) {
            throw new IllegalArgumentException("La foto seleccionada no pertenece al catalogo de perfiles disponible.");
        }
        return valor;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private String formatearFotoParaVista(String fotoUrl) {
        String valor = valorSeguro(fotoUrl);
        return valor.isBlank() ? AVATAR_DEFAULT : valor;
    }

    private List<AvatarPerfilView> cargarAvataresDisponibles(String fotoActual) {
        try {
            Resource[] recursos = resourceResolver.getResources("classpath:/profilepicks/**/*.*");
            List<AvatarPerfilView> avatares = new ArrayList<>();
            avatares.add(new AvatarPerfilView(AVATAR_DEFAULT, "Avatar por defecto", fotoActual == null || fotoActual.isBlank()));

            for (Resource recurso : recursos) {
                String rutaRelativa = extraerRutaRelativaAvatar(recurso);
                if (rutaRelativa == null || rutaRelativa.isBlank()) {
                    continue;
                }
                String nombreArchivo = recurso.getFilename();
                if (nombreArchivo == null || nombreArchivo.isBlank()) {
                    continue;
                }
                String url = UriUtils.encodePath("/profilepicks/" + rutaRelativa, StandardCharsets.UTF_8);
                avatares.add(new AvatarPerfilView(
                        url,
                        nombreArchivo,
                        url.equals(fotoActual)
                ));
            }

            avatares.sort(Comparator.comparing(AvatarPerfilView::etiqueta, String.CASE_INSENSITIVE_ORDER));
            avatares.sort(Comparator.comparing(avatar -> AVATAR_DEFAULT.equals(avatar.url()) ? 0 : 1));
            return List.copyOf(avatares);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron cargar las fotos de perfil disponibles.", ex);
        }
    }

    private Set<String> cargarUrlsAvataresPermitidas() {
        return cargarAvataresDisponibles(null).stream()
                .map(AvatarPerfilView::url)
                .filter(url -> !AVATAR_DEFAULT.equals(url))
                .collect(java.util.stream.Collectors.toSet());
    }

    private String extraerRutaRelativaAvatar(Resource recurso) throws IOException {
        String[] candidatos = {
                recurso.getURL().toString(),
                recurso.getDescription()
        };
        for (String candidato : candidatos) {
            if (candidato == null || candidato.isBlank()) {
                continue;
            }
            String normalizado = candidato.replace('\\', '/');
            int indice = normalizado.indexOf("profilepicks/");
            if (indice >= 0) {
                return normalizado.substring(indice + "profilepicks/".length());
            }
        }
        return null;
    }

    public record PerfilView(
            Long usuarioId,
            String nombreUsuario,
            String email,
            String fotoUrl,
            String fotoMostrada,
            List<AvatarPerfilView> avataresDisponibles
    ) {
    }

    public record PerfilRequest(
            String nombreUsuario,
            String email,
            String fotoUrl,
            String contrasenaActual,
            String nuevaContrasena,
            String repetirNuevaContrasena
    ) {
    }

    public record PerfilActualizadoView(
            String nombreUsuario,
            String email,
            String fotoUrl,
            String fotoMostrada
    ) {
    }

    public record AvatarPerfilView(
            String url,
            String etiqueta,
            boolean seleccionada
    ) {
    }
}
