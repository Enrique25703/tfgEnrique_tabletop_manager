package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.AfiliacionComunidadRepository;
import org.example.tfgenrique.dao.ComunidadRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComunidadMiembroServiceTest {

    @Test
    void creaLaComunidadYConvierteAlCreadorEnAdministrador() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        AfiliacionComunidadRepository afiliacionRepository = mock(AfiliacionComunidadRepository.class);
        ComunidadMiembroService service = new ComunidadMiembroService(
                usuarioRepository,
                comunidadRepository,
                afiliacionRepository
        );
        Usuario creador = usuarioActivo(7L);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(creador));
        when(comunidadRepository.existsByNombreIgnoreCase("Legión de Málaga")).thenReturn(false);
        doAnswer(invocacion -> {
            Comunidad comunidad = invocacion.getArgument(0);
            comunidad.setId(42L);
            return comunidad;
        }).when(comunidadRepository).save(any(Comunidad.class));

        Long comunidadId = service.crearComunidad(7L, "  Legión de Málaga  ", "  Jugadores locales  ");

        assertEquals(42L, comunidadId);
        ArgumentCaptor<Comunidad> comunidadCaptor = ArgumentCaptor.forClass(Comunidad.class);
        verify(comunidadRepository).save(comunidadCaptor.capture());
        assertEquals("Legión de Málaga", comunidadCaptor.getValue().getNombre());
        assertEquals("Jugadores locales", comunidadCaptor.getValue().getDescripcion());
        assertEquals(ComunidadConstantes.PRIVACIDAD_PUBLICA, comunidadCaptor.getValue().getPrivacidad());

        ArgumentCaptor<AfiliacionComunidad> afiliacionCaptor = ArgumentCaptor.forClass(AfiliacionComunidad.class);
        verify(afiliacionRepository).save(afiliacionCaptor.capture());
        assertEquals(creador, afiliacionCaptor.getValue().getUsuario());
        assertEquals(ComunidadConstantes.ROL_ADMINISTRADOR, afiliacionCaptor.getValue().getRolComunidad());
        assertEquals(ComunidadConstantes.ESTADO_AFILIACION_ACTIVA, afiliacionCaptor.getValue().getEstadoAfiliacion());
    }

    @Test
    void impideExpulsarAlUltimoAdministrador() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        AfiliacionComunidadRepository afiliacionRepository = mock(AfiliacionComunidadRepository.class);
        ComunidadMiembroService service = new ComunidadMiembroService(
                usuarioRepository,
                comunidadRepository,
                afiliacionRepository
        );
        Usuario administrador = usuarioActivo(3L);
        Comunidad comunidad = comunidad(11L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacion = afiliacion(comunidad, administrador, ComunidadConstantes.ROL_ADMINISTRADOR);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(administrador));
        when(comunidadRepository.findByIdAndActivoTrue(11L)).thenReturn(Optional.of(comunidad));
        when(afiliacionRepository.findByComunidadAndUsuario(comunidad, administrador)).thenReturn(Optional.of(afiliacion));
        when(afiliacionRepository.findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(
                comunidad, ComunidadConstantes.ESTADO_AFILIACION_ACTIVA)).thenReturn(List.of(afiliacion));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.expulsarMiembro(3L, 11L, 3L)
        );

        assertTrue(error.getMessage().contains("al menos un administrador"));
        verify(afiliacionRepository, never()).save(any(AfiliacionComunidad.class));
    }

    static Usuario usuarioActivo(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setActivo(true);
        usuario.setNombreUsuario("usuario-" + id);
        return usuario;
    }

    static Comunidad comunidad(Long id, String privacidad) {
        Comunidad comunidad = new Comunidad();
        comunidad.setId(id);
        comunidad.setNombre("Comunidad " + id);
        comunidad.setPrivacidad(privacidad);
        comunidad.setActivo(true);
        return comunidad;
    }

    static AfiliacionComunidad afiliacion(Comunidad comunidad, Usuario usuario, String rol) {
        AfiliacionComunidad afiliacion = new AfiliacionComunidad();
        afiliacion.setComunidad(comunidad);
        afiliacion.setUsuario(usuario);
        afiliacion.setRolComunidad(rol);
        afiliacion.setEstadoAfiliacion(ComunidadConstantes.ESTADO_AFILIACION_ACTIVA);
        return afiliacion;
    }
}
