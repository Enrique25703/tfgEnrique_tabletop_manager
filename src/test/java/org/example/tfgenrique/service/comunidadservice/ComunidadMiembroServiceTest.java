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

    @Test
    void permiteAlAfiliadoAbandonarLaComunidad() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        AfiliacionComunidadRepository afiliacionRepository = mock(AfiliacionComunidadRepository.class);
        ComunidadMiembroService service = new ComunidadMiembroService(
                usuarioRepository,
                comunidadRepository,
                afiliacionRepository
        );
        Usuario usuario = usuarioActivo(8L);
        Comunidad comunidad = comunidad(14L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacion = afiliacion(comunidad, usuario, ComunidadConstantes.ROL_USUARIO);
        when(usuarioRepository.findById(8L)).thenReturn(Optional.of(usuario));
        when(comunidadRepository.findByIdAndActivoTrue(14L)).thenReturn(Optional.of(comunidad));
        when(afiliacionRepository.findByComunidadAndUsuario(comunidad, usuario)).thenReturn(Optional.of(afiliacion));

        service.abandonarComunidad(8L, 14L);

        assertEquals(ComunidadConstantes.ESTADO_AFILIACION_INACTIVA, afiliacion.getEstadoAfiliacion());
        verify(afiliacionRepository).save(afiliacion);
    }

    @Test
    void eliminaLaComunidadCuandoElUltimoAdministradorEsElUnicoMiembro() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        AfiliacionComunidadRepository afiliacionRepository = mock(AfiliacionComunidadRepository.class);
        ComunidadMiembroService service = new ComunidadMiembroService(
                usuarioRepository,
                comunidadRepository,
                afiliacionRepository
        );
        Usuario administrador = usuarioActivo(9L);
        Comunidad comunidad = comunidad(15L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacion = afiliacion(comunidad, administrador, ComunidadConstantes.ROL_ADMINISTRADOR);
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(administrador));
        when(comunidadRepository.findByIdAndActivoTrue(15L)).thenReturn(Optional.of(comunidad));
        when(afiliacionRepository.findByComunidadAndUsuario(comunidad, administrador)).thenReturn(Optional.of(afiliacion));
        when(afiliacionRepository.findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(
                comunidad, ComunidadConstantes.ESTADO_AFILIACION_ACTIVA)).thenReturn(List.of(afiliacion));

        service.abandonarComunidad(9L, 15L);

        assertEquals(ComunidadConstantes.ESTADO_AFILIACION_INACTIVA, afiliacion.getEstadoAfiliacion());
        assertEquals(false, comunidad.getActivo());
        verify(afiliacionRepository).save(afiliacion);
        verify(comunidadRepository).save(comunidad);
    }

    @Test
    void exigeCederLaPropiedadSiQuedanOtrosMiembros() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        AfiliacionComunidadRepository afiliacionRepository = mock(AfiliacionComunidadRepository.class);
        ComunidadMiembroService service = new ComunidadMiembroService(
                usuarioRepository,
                comunidadRepository,
                afiliacionRepository
        );
        Usuario administrador = usuarioActivo(10L);
        Usuario miembro = usuarioActivo(11L);
        Comunidad comunidad = comunidad(16L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacionAdministrador = afiliacion(
                comunidad, administrador, ComunidadConstantes.ROL_ADMINISTRADOR);
        AfiliacionComunidad afiliacionMiembro = afiliacion(
                comunidad, miembro, ComunidadConstantes.ROL_USUARIO);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(administrador));
        when(comunidadRepository.findByIdAndActivoTrue(16L)).thenReturn(Optional.of(comunidad));
        when(afiliacionRepository.findByComunidadAndUsuario(comunidad, administrador))
                .thenReturn(Optional.of(afiliacionAdministrador));
        when(afiliacionRepository.findByComunidadAndEstadoAfiliacionOrderByRolComunidadAscUnidoEnAsc(
                comunidad, ComunidadConstantes.ESTADO_AFILIACION_ACTIVA))
                .thenReturn(List.of(afiliacionAdministrador, afiliacionMiembro));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.abandonarComunidad(10L, 16L)
        );

        assertTrue(error.getMessage().contains("ceder la propiedad"));
        verify(afiliacionRepository, never()).save(any(AfiliacionComunidad.class));
        verify(comunidadRepository, never()).save(any(Comunidad.class));
    }

    @Test
    void cedeLaPropiedadYOtorgaLaAdministracionAlMiembroElegido() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        AfiliacionComunidadRepository afiliacionRepository = mock(AfiliacionComunidadRepository.class);
        ComunidadMiembroService service = new ComunidadMiembroService(
                usuarioRepository,
                comunidadRepository,
                afiliacionRepository
        );
        Usuario administrador = usuarioActivo(12L);
        Usuario nuevoPropietario = usuarioActivo(13L);
        Comunidad comunidad = comunidad(17L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacionAdministrador = afiliacion(
                comunidad, administrador, ComunidadConstantes.ROL_ADMINISTRADOR);
        AfiliacionComunidad afiliacionNuevoPropietario = afiliacion(
                comunidad, nuevoPropietario, ComunidadConstantes.ROL_USUARIO);
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(administrador));
        when(usuarioRepository.findById(13L)).thenReturn(Optional.of(nuevoPropietario));
        when(comunidadRepository.findByIdAndActivoTrue(17L)).thenReturn(Optional.of(comunidad));
        when(afiliacionRepository.findByComunidadAndUsuario(comunidad, administrador))
                .thenReturn(Optional.of(afiliacionAdministrador));
        when(afiliacionRepository.findByComunidadAndUsuario(comunidad, nuevoPropietario))
                .thenReturn(Optional.of(afiliacionNuevoPropietario));

        service.cederPropiedad(12L, 17L, 13L);

        assertEquals(ComunidadConstantes.ROL_USUARIO, afiliacionAdministrador.getRolComunidad());
        assertEquals(ComunidadConstantes.ROL_ADMINISTRADOR, afiliacionNuevoPropietario.getRolComunidad());
        verify(afiliacionRepository).save(afiliacionAdministrador);
        verify(afiliacionRepository).save(afiliacionNuevoPropietario);
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
