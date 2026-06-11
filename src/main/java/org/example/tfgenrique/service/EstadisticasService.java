package org.example.tfgenrique.service;

import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EstadisticasService {
    private static final String FORMATO_40K = "WH40K_10";
    private static final String FORMATO_AOS = "AOS_4";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
                crearEvolucionVictorias(usuario, partidasFinalizadas),
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

    private EvolucionVictoriasView crearEvolucionVictorias(Usuario usuario, List<Partida> partidasFinalizadas) {
        LocalDateTime fechaInicio = usuario.getCreadoEn();
        LocalDateTime fechaFin = partidasFinalizadas.isEmpty()
                ? fechaInicio
                : resolverFechaPartida(partidasFinalizadas.get(partidasFinalizadas.size() - 1));

        List<PuntoEvolucionView> puntos = new ArrayList<>();
        puntos.add(new PuntoEvolucionView(
                formatearFecha(fechaInicio),
                "0.0%",
                0,
                0,
                100,
                0,
                0
        ));

        int victorias = 0;
        int derrotas = 0;
        int empates = 0;
        int partidasContadas = 0;

        for (Partida partida : partidasFinalizadas) {
            partidasContadas++;
            if (esVictoriaUsuario(partida)) {
                victorias++;
            } else if (Boolean.TRUE.equals(partida.getEsEmpate())) {
                empates++;
            } else {
                derrotas++;
            }

            double porcentaje = (victorias * 100.0) / partidasContadas;
            double x = calcularPosicionX(fechaInicio, fechaFin, resolverFechaPartida(partida));
            double y = 100 - porcentaje;
            puntos.add(new PuntoEvolucionView(
                    formatearFecha(resolverFechaPartida(partida)),
                    formatearPorcentaje(porcentaje),
                    redondear1Decimal(porcentaje),
                    redondear1Decimal(x),
                    redondear1Decimal(y),
                    victorias,
                    partidasContadas
            ));
        }

        double porcentajeFinal = partidasContadas == 0 ? 0 : (victorias * 100.0) / partidasContadas;
        return new EvolucionVictoriasView(
                partidasContadas,
                victorias,
                derrotas,
                empates,
                formatearPorcentaje(porcentajeFinal),
                construirPolyline(puntos),
                puntos,
                formatearFecha(fechaInicio),
                formatearFecha(fechaFin),
                partidasContadas > 0
        );
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

    private double calcularPosicionX(LocalDateTime inicio, LocalDateTime fin, LocalDateTime actual) {
        long inicioEpoch = inicio.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long finEpoch = fin.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long actualEpoch = actual.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (finEpoch <= inicioEpoch) {
            return 100;
        }
        return ((actualEpoch - inicioEpoch) * 100.0) / (finEpoch - inicioEpoch);
    }

    private String construirPolyline(List<PuntoEvolucionView> puntos) {
        StringBuilder polyline = new StringBuilder();
        for (PuntoEvolucionView punto : puntos) {
            if (!polyline.isEmpty()) {
                polyline.append(' ');
            }
            polyline.append(formatearCss(punto.x())).append(',').append(formatearCss(punto.y()));
        }
        return polyline.toString();
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    private String formatearPorcentaje(double valor) {
        return String.format(Locale.US, "%.1f%%", valor);
    }

    private String formatearCss(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private double redondear1Decimal(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public record EstadisticasView(
            String nombreUsuario,
            EvolucionVictoriasView evolucionVictorias,
            List<SelectorJuegoView> juegos,
            DistribucionJuegoView distribucion40k,
            DistribucionJuegoView distribucionAos,
            DistribucionJuegoView distribucionSeleccionada
    ) {
    }

    public record EvolucionVictoriasView(
            int partidasFinalizadas,
            int victorias,
            int derrotas,
            int empates,
            String porcentajeVictorias,
            String polylinePoints,
            List<PuntoEvolucionView> puntos,
            String fechaInicio,
            String fechaFin,
            boolean tieneDatos
    ) {
    }

    public record PuntoEvolucionView(
            String fecha,
            String porcentaje,
            double porcentajeValor,
            double x,
            double y,
            int victorias,
            int partidas
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
