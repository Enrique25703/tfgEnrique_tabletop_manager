package org.example.tfgenrique.service;

import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstadisticasServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PartidaRepository partidas = mock(PartidaRepository.class);
    private final EstadisticasService servicio = new EstadisticasService(usuarios, partidas);
    private final Usuario usuario = usuario();

    @Test
    void reparteLosResultadosSoloEntreLasPartidasFinalizadas() {
        var enCurso = partida(false, false);
        enCurso.setEstado("EN_CURSO");
        var resultado = resultados(List.of(partida(true, false), partida(true, false),
                partida(false, true), partida(false, false), enCurso));
        assertEquals(4, resultado.partidasFinalizadas());
        assertEquals(List.of("Victorias", "Empates", "Derrotas"),
                resultado.segmentos().stream().map(EstadisticasService.SegmentoDistribucionView::etiqueta).toList());
        assertEquals(List.of("50.0%", "25.0%", "25.0%"),
                resultado.segmentos().stream().map(EstadisticasService.SegmentoDistribucionView::porcentaje).toList());
        assertEquals("conic-gradient(#69c9a3 0.00% 50.00%, #f4b860 50.00% 75.00%, #ef6f6c 75.00% 100.00%)", resultado.graficaCss());
    }

    @Test
    void sinPartidasMuestraCeroEnTodosLosResultados() {
        var resultado = resultados(List.of());
        assertFalse(resultado.tieneDatos());
        assertEquals("#233246", resultado.graficaCss());
        assertTrue(resultado.segmentos().stream().allMatch(s -> s.porcentaje().equals("0.0%")));
    }

    @Test
    void todosLosEmpatesOcupanElCienPorCien() {
        var resultado = resultados(List.of(partida(false, true)));
        assertTrue(resultado.tieneDatos());
        assertEquals("100.0%", resultado.segmentos().get(1).porcentaje());
        assertEquals(0, resultado.victorias());
        assertEquals(0, resultado.derrotas());
        assertTrue(resultado.graficaCss().contains("#f4b860 0.00% 100.00%"));
    }

    private EstadisticasService.ResultadosPartidasView resultados(List<Partida> datos) {
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidas.findByJugador1UsuarioOrderByCreadoEnAsc(usuario)).thenReturn(datos);
        return servicio.prepararEstadisticas(1L, "WH40K_11").resultados();
    }

    private Partida partida(boolean victoria, boolean empate) {
        var partida = new Partida();
        partida.setEstado("FINALIZADA");
        partida.setCreadoEn(LocalDateTime.of(2026, 9, 1, 12, 0));
        partida.setJugador1Usuario(usuario);
        partida.setEsEmpate(empate);
        if (victoria) partida.setGanadorUsuario(usuario);
        return partida;
    }

    private Usuario usuario() {
        var usuario = new Usuario();
        usuario.setId(1L);
        usuario.setActivo(true);
        usuario.setNombreUsuario("Prueba");
        return usuario;
    }
}
