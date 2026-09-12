package org.example.tfgenrique.service.user;

import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    @Test
    void rechazaEnElRegistroContrasenasDeMenosDeSeisCaracteres() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        LoginService servicio = new LoginService(usuarios);

        assertThatThrownBy(() -> servicio.registrarUsuario("jugador", "jugador@example.com", "1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La contrasena debe tener al menos 6 caracteres.");

        verify(usuarios, never()).save(any());
    }

    @Test
    void registraYHasheaUnaContrasenaValida() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.findByNombreUsuario("jugador")).thenReturn(Optional.empty());
        when(usuarios.findByEmail("jugador@example.com")).thenReturn(Optional.empty());
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        LoginService servicio = new LoginService(usuarios);

        Usuario registrado = servicio.registrarUsuario(" jugador ", "JUGADOR@example.com", " 123456 ");

        assertThat(registrado.getNombreUsuario()).isEqualTo("jugador");
        assertThat(registrado.getEmail()).isEqualTo("jugador@example.com");
        assertThat(new BCryptPasswordEncoder().matches("123456", registrado.getContrasenaHash())).isTrue();
    }
}
