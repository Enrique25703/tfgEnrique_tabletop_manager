package org.example.tfgenrique.service;

import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EstadisticasService {
    private static final String FORMATO_40K = "WH40K_11";
    private static final String FORMATO_AOS = "AOS_4";
    private static final List<String> COLORES = List.of(
            "#5b8def",
            "#69c9a3",
            "#f4b860",
            "#ef6f6c",
            "#9b7bff",
            "#58c4dd",
            "#ffd166",
            "#7bd389"
    );

    private final UsuarioRepository usuarioRepository;
    private final PartidaRepository partidaRepository;

    public EstadisticasService(UsuarioRepository usuarioRepository, PartidaRepository partidaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.partidaRepository = partidaRepository;
    }

    @Transactional(readOnly = true)
    public EstadisticasView prepararEstadisticas(Long usuarioId, String juegoSeleccionado) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta de usuario no esta activa.");
        }

        List<Partida> partidas = partidaRepository.findByJugador1UsuarioOrderByCreadoEnAsc(usuario);
        List<Partida> partidasJugadas = partidas.stream()
                .filter(this::esPartidaRegistrada)
                .sorted(Comparator.comparing(this::resolverFechaPartida))
                .toList();
        List<Partida> partidasFinalizadas = partidasJugadas.stream()
                .filter(this::esPartidaFinalizada)
                .toList();

        String juego = normalizarJuego(juegoSeleccionado);
        DistribucionJuegoView distribucion40k = crearDistribucionJuego(FORMATO_40K, "Warhammer 40.000", partidasJugadas, juego);
        DistribucionJuegoView distribucionAos = crearDistribucionJuego(FORMATO_AOS, "Age of Sigmar", partidasJugadas, juego);
        List<SelectorJuegoView> juegos = List.of(
                crearSelectorJuego(FORMATO_40K, "Warhammer 40.000", juego, partidasJugadas),
                crearSelectorJuego(FORMATO_AOS, "Age of Sigmar", juego, partidasJugadas)
        );

        return new EstadisticasView(
                usuario.getNombreUsuario(),
                crearResultados(partidasFinalizadas),
                juegos,
                distribucion40k,
                distribucionAos,
                FORMATO_40K.equals(juego) ? distribucion40k : distribucionAos
        );
    }

    private SelectorJuegoView crearSelectorJuego(
            String codigo,
            String nombre,
            String juegoSeleccionado,
            List<Partida> partidasJugadas
    ) {
        int total = (int) partidasJugadas.stream()
                .filter(partida -> codigo.equals(obtenerCodigoJuego(partida)))
                .count();
        return new SelectorJuegoView(codigo, nombre, codigo.equals(juegoSeleccionado), total);
    }

    private ResultadosPartidasView crearResultados(List<Partida> partidasFinalizadas) {
        int victorias = 0;
        int empates = 0;
        int derrotas = 0;
        for (Partida partida : partidasFinalizadas) {
            if (Boolean.TRUE.equals(partida.getEsEmpate())) empates++;
            else if (esVictoriaUsuario(partida)) victorias++;
            else derrotas++;
        }
        int total = partidasFinalizadas.size();
        String[] etiquetas = {"Victorias", "Empates", "Derrotas"};
        String[] colores = {"#69c9a3", "#f4b860", "#ef6f6c"};
        int[] cantidades = {victorias, empates, derrotas};
        List<SegmentoDistribucionView> segmentos = new ArrayList<>();
        List<String> sectores = new ArrayList<>();
        int acumulado = 0;
        for (int i = 0; i < cantidades.length; i++) {
            double inicio = total == 0 ? 0 : acumulado * 100.0 / total;
            acumulado += cantidades[i];
            double fin = total == 0 ? 0 : acumulado * 100.0 / total;
            segmentos.add(new SegmentoDistribucionView(etiquetas[i], cantidades[i],
                    formatearPorcentaje(total == 0 ? 0 : cantidades[i] * 100.0 / total), colores[i]));
            sectores.add(colores[i] + " " + formatearCss(inicio) + "% " + formatearCss(fin) + "%");
        }
        return new ResultadosPartidasView(total, victorias, derrotas, empates,
                segmentos.get(0).porcentaje(), total == 0 ? "#233246"
                : "conic-gradient(" + String.join(", ", sectores) + ")", List.copyOf(segmentos), total > 0);
    }

    private DistribucionJuegoView crearDistribucionJuego(
            String codigoJuego,
            String nombreJuego,
            List<Partida> partidasJugadas,
            String juegoSeleccionado
    ) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Partida partida : partidasJugadas) {
            if (!codigoJuego.equals(obtenerCodigoJuego(partida))) {
                continue;
            }
            String faccion = normalizarTexto(partida.getJugador1FaccionSnapshot());
            if (faccion.isBlank()) {
                faccion = "Sin faccion registrada";
            }
            conteo.merge(faccion, 1, Integer::sum);
        }

        List<Map.Entry<String, Integer>> ordenadas = conteo.entrySet().stream()
                .sorted((a, b) -> {
                    int comparacion = Integer.compare(b.getValue(), a.getValue());
                    if (comparacion != 0) {
                        return comparacion;
                    }
                    return a.getKey().compareToIgnoreCase(b.getKey());
                })
                .toList();

        int total = ordenadas.stream().mapToInt(Map.Entry::getValue).sum();
        List<SegmentoDistribucionView> segmentos = new ArrayList<>();
        List<String> gradientes = new ArrayList<>();
        double acumulado = 0;

        for (int i = 0; i < ordenadas.size(); i++) {
            Map.Entry<String, Integer> entrada = ordenadas.get(i);
            String color = COLORES.get(i % COLORES.size());
            double porcentaje = total == 0 ? 0 : (entrada.getValue() * 100.0) / total;
            double inicio = acumulado;
            acumulado += porcentaje;
            gradientes.add(color + " " + formatearCss(inicio) + "% " + formatearCss(acumulado) + "%");
            segmentos.add(new SegmentoDistribucionView(
                    entrada.getKey(),
                    entrada.getValue(),
                    formatearPorcentaje(porcentaje),
                    color
            ));
        }

        String gradiente = gradientes.isEmpty()
                ? "conic-gradient(#233246 0% 100%)"
                : "conic-gradient(" + String.join(", ", gradientes) + ")";

        String descripcion = FORMATO_40K.equals(codigoJuego)
                ? "Distribucion de partidas por faccion o ejercito en Warhammer 40.000."
                : "Distribucion de partidas por faccion en Age of Sigmar.";

        return new DistribucionJuegoView(
                codigoJuego,
                nombreJuego,
                codigoJuego.equals(juegoSeleccionado),
                total,
                gradiente,
                segmentos,
                descripcion
        );
    }

    private boolean esPartidaRegistrada(Partida partida) {
        String estado = normalizarTexto(partida.getEstado());
        return "EN_CURSO".equals(estado)
                || "FINALIZADA".equals(estado)
                || partida.getIniciadaEn() != null
                || partida.getFinalizadaEn() != null;
    }

    private boolean esPartidaFinalizada(Partida partida) {
        return "FINALIZADA".equals(normalizarTexto(partida.getEstado())) || partida.getFinalizadaEn() != null;
    }

    private boolean esVictoriaUsuario(Partida partida) {
        return partida.getGanadorUsuario() != null
                && partida.getJugador1Usuario() != null
                && partida.getGanadorUsuario().getId().equals(partida.getJugador1Usuario().getId());
    }

    private LocalDateTime resolverFechaPartida(Partida partida) {
        if (partida.getFinalizadaEn() != null) {
            return partida.getFinalizadaEn();
        }
        if (partida.getIniciadaEn() != null) {
            return partida.getIniciadaEn();
        }
        if (partida.getActualizadoEn() != null) {
            return partida.getActualizadoEn();
        }
        return partida.getCreadoEn();
    }

    private String obtenerCodigoJuego(Partida partida) {
        return partida.getSistemaJuego() == null ? "" : normalizarTexto(partida.getSistemaJuego().getCodigo()).toUpperCase(Locale.ROOT);
    }

    private String normalizarJuego(String juegoSeleccionado) {
        String valor = normalizarTexto(juegoSeleccionado).toUpperCase(Locale.ROOT);
        if (FORMATO_AOS.equals(valor)) {
            return FORMATO_AOS;
        }
        return FORMATO_40K;
    }

    private String formatearPorcentaje(double valor) {
        return String.format(Locale.US, "%.1f%%", valor);
    }

    private String formatearCss(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public record EstadisticasView(
            String nombreUsuario,
            ResultadosPartidasView resultados,
            List<SelectorJuegoView> juegos,
            DistribucionJuegoView distribucion40k,
            DistribucionJuegoView distribucionAos,
            DistribucionJuegoView distribucionSeleccionada
    ) {
    }

    public record ResultadosPartidasView(
            int partidasFinalizadas,
            int victorias,
            int derrotas,
            int empates,
            String porcentajeVictorias,
            String graficaCss,
            List<SegmentoDistribucionView> segmentos,
            boolean tieneDatos
    ) {
    }

    public record SelectorJuegoView(
            String codigo,
            String nombre,
            boolean seleccionado,
            int totalPartidas
    ) {
    }

    public record DistribucionJuegoView(
            String codigo,
            String nombre,
            boolean seleccionado,
            int totalPartidas,
            String graficaCss,
            List<SegmentoDistribucionView> segmentos,
            String descripcion
    ) {
    }

    public record SegmentoDistribucionView(
            String etiqueta,
            int totalPartidas,
            String porcentaje,
            String color
    ) {
    }
}
