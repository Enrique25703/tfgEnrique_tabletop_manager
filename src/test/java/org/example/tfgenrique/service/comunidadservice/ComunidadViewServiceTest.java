package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.ComunidadRepository;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComunidadViewServiceTest {

    @Test
    void preparaEventosCercanosConLasInscripcionesVigentes() {
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        ComunidadEventoService eventoService = mock(ComunidadEventoService.class);
        ComunidadInvitacionService invitacionService = mock(ComunidadInvitacionService.class);
        ComunidadSolicitudService solicitudService = mock(ComunidadSolicitudService.class);
        ComunidadViewService service = new ComunidadViewService(
                comunidadRepository,
                miembroService,
                eventoService,
                invitacionService,
                solicitudService
        );

        Usuario usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombreUsuario("jugador");

        Usuario organizador = new Usuario();
        organizador.setNombreUsuario("organizador");

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);
        comunidad.setNombre("La Fortaleza");

        SistemaJuego sistema = new SistemaJuego();
        sistema.setCodigo("WH40K_11");

        Evento evento = new Evento();
        evento.setId(21L);
        evento.setTitulo("Torneo de iniciación");
        evento.setInicioEn(LocalDateTime.of(2030, 5, 25, 10, 0));
        evento.setSistemaJuego(sistema);
        evento.setComunidad(comunidad);
        evento.setOrganizadorUsuario(organizador);
        evento.setUbicacion("Calle Acero, 12, Madrid");
        evento.setLatitud(new BigDecimal("40.4168000"));
        evento.setLongitud(new BigDecimal("-3.7038000"));
        evento.setRondasPlanificadas(3);
        evento.setDescripcion("Evento para nuevos jugadores.");

        InscripcionEvento inscripcion = new InscripcionEvento();
        inscripcion.setEvento(evento);

        when(miembroService.buscarUsuario(9L)).thenReturn(usuario);
        when(miembroService.buscarComunidadesUsuario(usuario)).thenReturn(List.of());
        when(comunidadRepository.findAllByActivoTrueOrderByNombreAsc()).thenReturn(List.of());
        when(eventoService.buscarInscripcionesProximas(any(), any())).thenReturn(List.of(inscripcion));
        when(eventoService.obtenerNombreFormato("WH40K_11")).thenReturn("Warhammer 40.000 11a edición");

        ComunidadService.EventosCercanosPaginaView pagina = service.prepararEventosCercanos(9L);

        assertEquals(1, pagina.totalEventosProximos());
        ComunidadService.EventoActualView eventoView = pagina.eventosProximos().get(0);
        assertEquals("Torneo de iniciación", eventoView.getTitulo());
        assertEquals("10:00", eventoView.getHora());
        assertEquals("La Fortaleza", eventoView.getComunidad());
        assertEquals("40.4168000", eventoView.getLatitud());
        assertTrue(eventoView.isTieneCoordenadas());
    }

    @Test
    void preparaDescubrimientoConListadoYDetalleSeparados() {
        ComunidadRepository comunidadRepository = mock(ComunidadRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        ComunidadEventoService eventoService = mock(ComunidadEventoService.class);
        ComunidadInvitacionService invitacionService = mock(ComunidadInvitacionService.class);
        ComunidadSolicitudService solicitudService = mock(ComunidadSolicitudService.class);
        ComunidadViewService service = new ComunidadViewService(
                comunidadRepository,
                miembroService,
                eventoService,
                invitacionService,
                solicitudService
        );
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombreUsuario("jugador");
        Comunidad comunidad = new Comunidad();
        comunidad.setId(13L);
        comunidad.setNombre("La Fortaleza Málaga");
        comunidad.setDescripcion("Torneos y ligas para todos los niveles.");
        comunidad.setPrivacidad("PRIVADA");
        when(miembroService.buscarUsuario(9L)).thenReturn(usuario);
        when(miembroService.buscarComunidadesUsuario(usuario)).thenReturn(List.of());
        when(comunidadRepository.findAllByActivoTrueOrderByNombreAsc()).thenReturn(List.of(comunidad));
        when(miembroService.contarMiembros(comunidad)).thenReturn(128);
        when(eventoService.buscarEventos(comunidad)).thenReturn(List.of());
        when(solicitudService.tieneSolicitudPendiente(comunidad, usuario)).thenReturn(false);

        ComunidadService.DescubrirComunidadesPaginaView pagina = service
                .prepararDescubrirComunidades(9L, 13L, "fortaleza");

        assertEquals(1, pagina.totalComunidades());
        assertNotNull(pagina.seleccionada());
        assertEquals(13L, pagina.seleccionada().comunidad().id());
        assertTrue(pagina.seleccionada().comunidad().privada());
        assertEquals(128, pagina.seleccionada().comunidad().totalMiembros());
    }
}
