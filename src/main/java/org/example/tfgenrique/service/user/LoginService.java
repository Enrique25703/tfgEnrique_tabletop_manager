package org.example.tfgenrique.service.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario autenticar(String usuarioOEmail, String password) {
        if (usuarioOEmail == null || usuarioOEmail.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Debes introducir usuario y contrasena.");
        }

        String credencial = usuarioOEmail.trim();
        Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(credencial);

        if (usuario.isEmpty()) {
            usuario = usuarioRepository.findByEmail(credencial);
        }

        if (usuario.isEmpty()) {
            throw new IllegalArgumentException("Usuario o contrasena incorrectos.");
        }

        Usuario usuarioEncontrado = usuario.get();
        if (!Boolean.TRUE.equals(usuarioEncontrado.getActivo())) {
            throw new IllegalArgumentException("La cuenta no esta activa.");
        }

        if (!passwordEncoder.matches(password, usuarioEncontrado.getContrasenaHash())) {
            throw new IllegalArgumentException("Usuario o contrasena incorrectos.");
        }

        return usuarioEncontrado;
    }

    public String hashearContrasena(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia.");
        }

        return passwordEncoder.encode(password);
    }

    public Usuario registrarUsuario(String nombreUsuario, String email, String password) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre de usuario.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un email.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Debes indicar una contrasena.");
        }

        String nombreNormalizado = nombreUsuario.trim();
        String emailNormalizado = email.trim().toLowerCase();
        String passwordNormalizado = password.trim();

        if (passwordNormalizado.length() < 6) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres.");
        }

        if (usuarioRepository.findByNombreUsuario(nombreNormalizado).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre.");
        }
        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreNormalizado);
        usuario.setEmail(emailNormalizado);
        usuario.setContrasenaHash(hashearContrasena(passwordNormalizado));
        usuario.setRol("USER");
        usuario.setActivo(true);
        usuario.setCreadoEn(ahora);
        usuario.setActualizadoEn(ahora);

        return usuarioRepository.save(usuario);
    }
}
