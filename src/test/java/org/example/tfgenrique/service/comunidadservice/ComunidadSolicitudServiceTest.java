package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.SolicitudComunidadRepository;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.SolicitudComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComunidadSolicitudServiceTest {

    @Test
    void creaSolicitudPendienteParaUnaComunidadPrivada() {
        SolicitudComunidadRepository repository = mock(SolicitudComunidadRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        ComunidadSolicitudService service = new ComunidadSolicitudService(repository, miembroService);
        Usuario usuario = ComunidadMiembroServiceTest.usuarioActivo(5L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(8L, ComunidadConstantes.PRIVACIDAD_PRIVADA);
        when(miembroService.buscarUsuario(5L)).thenReturn(usuario);
        when(miembroService.buscarComunidad(8L)).thenReturn(comunidad);
        when(miembroService.perteneceActivo(usuario, comunidad)).thenReturn(false);
        when(repository.findByComunidadAndUsuario(comunidad, usuario)).thenReturn(Optional.empty());

        ComunidadSolicitudService.ResultadoUnion resultado = service.solicitarOUnirse(5L, 8L);

        assertFalse(resultado.unidoDirectamente());
        ArgumentCaptor<SolicitudComunidad> captor = ArgumentCaptor.forClass(SolicitudComunidad.class);
        verify(repository).save(captor.capture());
        assertEquals(ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE, captor.getValue().getEstado());
        assertEquals(usuario, captor.getValue().getUsuario());
        assertEquals(comunidad, captor.getValue().getComunidad());
    }

    @Test
    void uneDirectamenteEnUnaComunidadPublica() {
        SolicitudComunidadRepository repository = mock(SolicitudComunidadRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        ComunidadSolicitudService service = new ComunidadSolicitudService(repository, miembroService);
        Usuario usuario = ComunidadMiembroServiceTest.usuarioActivo(5L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(8L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        when(miembroService.buscarUsuario(5L)).thenReturn(usuario);
        when(miembroService.buscarComunidad(8L)).thenReturn(comunidad);

        ComunidadSolicitudService.ResultadoUnion resultado = service.solicitarOUnirse(5L, 8L);

        assertTrue(resultado.unidoDirectamente());
        verify(miembroService).activarAfiliacion(usuario, comunidad);
        verify(repository, never()).save(any(SolicitudComunidad.class));
    }

    @Test
    void evitaDuplicarUnaSolicitudPendiente() {
        SolicitudComunidadRepository repository = mock(SolicitudComunidadRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        ComunidadSolicitudService service = new ComunidadSolicitudService(repository, miembroService);
        Usuario usuario = ComunidadMiembroServiceTest.usuarioActivo(5L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(8L, ComunidadConstantes.PRIVACIDAD_PRIVADA);
        SolicitudComunidad existente = new SolicitudComunidad();
        existente.setEstado(ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE);
        when(miembroService.buscarUsuario(5L)).thenReturn(usuario);
        when(miembroService.buscarComunidad(8L)).thenReturn(comunidad);
        when(repository.findByComunidadAndUsuario(comunidad, usuario)).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.solicitarOUnirse(5L, 8L));
        verify(repository, never()).save(any(SolicitudComunidad.class));
    }

    @Test
    void aceptarSolicitudCompruebaElRolYActivaLaAfiliacion() {
        SolicitudComunidadRepository repository = mock(SolicitudComunidadRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        ComunidadSolicitudService service = new ComunidadSolicitudService(repository, miembroService);
        Usuario administrador = ComunidadMiembroServiceTest.usuarioActivo(1L);
        Usuario solicitante = ComunidadMiembroServiceTest.usuarioActivo(5L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(8L, ComunidadConstantes.PRIVACIDAD_PRIVADA);
        SolicitudComunidad solicitud = new SolicitudComunidad();
        solicitud.setId(14L);
        solicitud.setComunidad(comunidad);
        solicitud.setUsuario(solicitante);
        solicitud.setEstado(ComunidadConstantes.ESTADO_SOLICITUD_PENDIENTE);
        when(miembroService.buscarUsuario(1L)).thenReturn(administrador);
        when(miembroService.buscarComunidad(8L)).thenReturn(comunidad);
        when(repository.findById(14L)).thenReturn(Optional.of(solicitud));

        service.aceptar(1L, 8L, 14L);

        verify(miembroService).exigirAdministrador(administrador, comunidad);
        verify(miembroService).activarAfiliacion(solicitante, comunidad);
        assertEquals(ComunidadConstantes.ESTADO_SOLICITUD_ACEPTADA, solicitud.getEstado());
        verify(repository).save(solicitud);
    }
}
