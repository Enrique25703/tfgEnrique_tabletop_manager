package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.EventoRepository;
import org.example.tfgenrique.dao.InscripcionEventoRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComunidadEventoServiceTest {

    @Test
    void rechazaLaEdicionSiNoEsAdministradorNiOrganizador() {
        Dependencias dependencias = dependencias();
        Usuario usuario = ComunidadMiembroServiceTest.usuarioActivo(4L);
        Usuario organizador = ComunidadMiembroServiceTest.usuarioActivo(9L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(2L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacion = ComunidadMiembroServiceTest.afiliacion(
                comunidad, usuario, ComunidadConstantes.ROL_USUARIO);
        Evento evento = evento(20L, comunidad, organizador, dependencias.sistemaJuego());
        prepararContexto(dependencias, usuario, comunidad, afiliacion, evento);

        assertThrows(
                IllegalArgumentException.class,
                () -> dependencias.service().guardarEvento(4L, 2L, 20L, requestValido())
        );

        verify(dependencias.eventoRepository(), never()).save(any(Evento.class));
    }

    @Test
    void permiteEditarAlOrganizadorYConservaElEstadoExistente() {
        Dependencias dependencias = dependencias();
        Usuario organizador = ComunidadMiembroServiceTest.usuarioActivo(9L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(2L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        AfiliacionComunidad afiliacion = ComunidadMiembroServiceTest.afiliacion(
                comunidad, organizador, ComunidadConstantes.ROL_USUARIO);
        Evento evento = evento(20L, comunidad, organizador, dependencias.sistemaJuego());
        evento.setEstado("FINALIZADO");
        prepararContexto(dependencias, organizador, comunidad, afiliacion, evento);
        when(dependencias.inscripcionRepository().buscarInscripcionesParaActualizar(evento)).thenReturn(List.of());

        dependencias.service().guardarEvento(9L, 2L, 20L, requestValido());

        ArgumentCaptor<Evento> captor = ArgumentCaptor.forClass(Evento.class);
        verify(dependencias.eventoRepository()).save(captor.capture());
        assertEquals("FINALIZADO", captor.getValue().getEstado());
        assertEquals("Torneo de prueba", captor.getValue().getTitulo());
    }

    @Test
    void impideInscribirseCuandoElEventoEstaCompleto() {
        Dependencias dependencias = dependencias();
        Usuario usuario = ComunidadMiembroServiceTest.usuarioActivo(4L);
        Usuario organizador = ComunidadMiembroServiceTest.usuarioActivo(9L);
        Comunidad comunidad = ComunidadMiembroServiceTest.comunidad(2L, ComunidadConstantes.PRIVACIDAD_PUBLICA);
        Evento evento = evento(20L, comunidad, organizador, dependencias.sistemaJuego());
        evento.setMaxParticipantes(2);
        when(dependencias.miembroService().buscarUsuario(4L)).thenReturn(usuario);
        when(dependencias.eventoRepository().findByIdForUpdate(20L)).thenReturn(Optional.of(evento));
        InscripcionEvento inscrito = new InscripcionEvento();
        inscrito.setUsuario(organizador);
        when(dependencias.inscripcionRepository().buscarInscripcionesParaActualizar(evento))
                .thenReturn(List.of(inscrito, inscrito));

        assertThrows(IllegalArgumentException.class, () -> dependencias.service().unirseAEvento(4L, 20L));

        verify(dependencias.miembroService()).buscarAfiliacionActiva(usuario, comunidad);
        verify(dependencias.inscripcionRepository(), never()).save(any());
    }

    private Dependencias dependencias() {
        EventoRepository eventoRepository = mock(EventoRepository.class);
        InscripcionEventoRepository inscripcionRepository = mock(InscripcionEventoRepository.class);
        SistemaJuegoRepository sistemaRepository = mock(SistemaJuegoRepository.class);
        ComunidadMiembroService miembroService = mock(ComunidadMiembroService.class);
        SistemaJuego sistema = new SistemaJuego();
        sistema.setCodigo(ComunidadConstantes.FORMATO_40K);
        when(sistemaRepository.findByCodigo(ComunidadConstantes.FORMATO_40K)).thenReturn(Optional.of(sistema));
        return new Dependencias(
                new ComunidadEventoService(eventoRepository, inscripcionRepository, sistemaRepository, miembroService),
                eventoRepository,
                inscripcionRepository,
                miembroService,
                sistema
        );
    }

    private void prepararContexto(
            Dependencias dependencias,
            Usuario usuario,
            Comunidad comunidad,
            AfiliacionComunidad afiliacion,
            Evento evento
    ) {
        when(dependencias.miembroService().buscarUsuario(usuario.getId())).thenReturn(usuario);
        when(dependencias.miembroService().buscarComunidad(comunidad.getId())).thenReturn(comunidad);
        when(dependencias.miembroService().buscarAfiliacionActiva(usuario, comunidad)).thenReturn(afiliacion);
        when(dependencias.eventoRepository().findByIdForUpdate(evento.getId())).thenReturn(Optional.of(evento));
    }

    private Evento evento(Long id, Comunidad comunidad, Usuario organizador, SistemaJuego sistema) {
        Evento evento = new Evento();
        evento.setId(id);
        evento.setComunidad(comunidad);
        evento.setOrganizadorUsuario(organizador);
        evento.setSistemaJuego(sistema);
        evento.setEstado(ComunidadConstantes.ESTADO_EVENTO_ABIERTO);
        evento.setCreadoEn(LocalDateTime.now().minusDays(1));
        return evento;
    }

    private ComunidadService.CrearEventoRequest requestValido() {
        ComunidadService.CrearEventoRequest request = new ComunidadService.CrearEventoRequest();
        request.setTitulo("Torneo de prueba");
        request.setDescripcion("Descripción");
        request.setFecha(LocalDateTime.now().plusDays(7));
        request.setNumeroRondas(3);
        request.setMaxParticipantes(20);
        request.setLugar("Málaga, España");
        request.setLatitud(36.7213);
        request.setLongitud(-4.4214);
        request.setFormatoJuego(ComunidadConstantes.FORMATO_40K);
        return request;
    }

    private record Dependencias(
            ComunidadEventoService service,
            EventoRepository eventoRepository,
            InscripcionEventoRepository inscripcionRepository,
            ComunidadMiembroService miembroService,
            SistemaJuego sistemaJuego
    ) {
    }
}
