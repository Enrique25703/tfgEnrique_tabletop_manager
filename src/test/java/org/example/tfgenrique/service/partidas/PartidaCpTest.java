package org.example.tfgenrique.service.partidas;

import org.example.tfgenrique.dao.*;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.RondaPartida;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.CreacionListasService;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartidaCpTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PartidaRepository partidas = mock(PartidaRepository.class);
    private final RondaPartidaRepository rondas = mock(RondaPartidaRepository.class);
    private final PartidaService servicio = new PartidaService(usuarios, mock(SistemaJuegoRepository.class),
            partidas, rondas, mock(Deployment40kService.class), mock(Misiones40kService.class),
            mock(Catalogo40kService.class), mock(CreacionListasService.class));
    private final Partida partida = new Partida();

    @BeforeEach
    void prepararPartida() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setActivo(true);
        partida.setId(9L);
        partida.setCreadoPorUsuario(usuario);
        partida.setJugador1Usuario(usuario);
        partida.setJugadorPrimero("USUARIO");
        partida.setMostrarCommandPoints(true);
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        when(partidas.findById(9L)).thenReturn(Optional.of(partida));
        when(rondas.findByPartidaOrderByNumeroRondaAsc(partida)).thenReturn(List.of());
    }

    @Test
    void guardaGanadosYGastadosConLasMisionesYRecuperaElSaldo() {
        servicio.guardarRonda(1L, 9L, 1, Map.of(
                "jugador1CpGanados", "5", "jugador1CpGastados", "2",
                "jugador2CpGanados", "2", "jugador2CpGastados", "1",
                "jugador1Detalle", "[{\"id\":\"mision-1\",\"puntos\":5,\"cumplida\":true}]"));
        var captor = ArgumentCaptor.forClass(RondaPartida.class);
        verify(rondas).save(captor.capture());
        var guardada = captor.getValue();
        assertThat(guardada.getCpJugador1Inicio()).isZero();
        assertThat(guardada.getCpJugador1Fin()).isEqualTo(3);
        assertThat(guardada.getCpJugador2Fin()).isEqualTo(1);
        when(rondas.findByPartidaAndNumeroRonda(partida, 1)).thenReturn(Optional.of(guardada));
        var vista = servicio.prepararRonda(1L, 9L, 1);
        assertThat(vista.izquierda().cpGanados()).isEqualTo(5);
        assertThat(vista.izquierda().cpGastados()).isEqualTo(2);
        assertThat(vista.izquierda().detalle()).contains("mision-1", "\"cumplida\":true");
        assertThat(vista.derecha().cpGanados()).isEqualTo(2);

        var siguiente = servicio.prepararRonda(1L, 9L, 2);
        assertThat(siguiente.izquierda().cpInicio()).isEqualTo(3);
        assertThat(siguiente.izquierda().cpFin()).isEqualTo(3);
        assertThat(siguiente.izquierda().cpGanados()).isZero();
        assertThat(siguiente.izquierda().cpGastados()).isZero();

        servicio.guardarRonda(1L, 9L, 2, Map.of(
                "jugador1CpGanados", "1", "jugador1CpGastados", "3",
                "jugador2CpGanados", "1", "jugador2CpGastados", "0"));
        verify(rondas, times(2)).save(captor.capture());
        assertThat(captor.getValue().getCpJugador1Inicio()).isEqualTo(3);
        assertThat(captor.getValue().getCpJugador1Fin()).isEqualTo(1);
    }

    @Test
    void mantieneRondasAntiguasYOrdenDeJugadores() {
        var guardada = new RondaPartida();
        guardada.setCpJugador1Inicio(2);
        guardada.setCpJugador1Fin(4);
        guardada.setCpJugador2Inicio(5);
        guardada.setCpJugador2Fin(3);
        guardada.setDetalleJugador1("[{\"id\":\"antigua\"}]");
        partida.setJugadorPrimero("RIVAL");
        when(rondas.findByPartidaAndNumeroRonda(partida, 1)).thenReturn(Optional.of(guardada));
        var vista = servicio.prepararRonda(1L, 9L, 1);
        assertThat(vista.izquierda().prefijo()).isEqualTo("jugador2");
        assertThat(vista.izquierda().cpGastados()).isEqualTo(2);
        assertThat(vista.derecha().cpGanados()).isEqualTo(2);
        assertThat(vista.derecha().detalle()).isEqualTo("[{\"id\":\"antigua\"}]");
    }

    @Test
    void rechazaGastosSuperioresAlSaldoYValoresInvalidos() {
        for (String gastados : List.of("4", "-1", "1.5", "2147483648")) {
            assertThatThrownBy(() -> servicio.guardarRonda(1L, 9L, 1,
                    Map.of("jugador1CpGanados", "3", "jugador1CpGastados", gastados)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        verify(rondas, never()).save(any());
    }
}
