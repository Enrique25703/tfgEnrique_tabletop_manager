package org.example.tfgenrique.service.partidas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.RondaPartidaRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.Partida;
import org.example.tfgenrique.entity.RondaPartida;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.CreacionListasService;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartidaService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String FORMATO_40K = "WH40K_11";
    private static final String ESTILO_EQUILIBRADO = "EQUILIBRADO";
    private static final String ESTILO_ASIMETRICO = "ASIMETRICO";
    private static final String ESTADO_CREADA = "CREADA";
    private static final String ESTADO_JUGADORES = "JUGADORES";
    private static final String ESTADO_CONFIGURADA = "CONFIGURADA";
    private static final String ESTADO_EN_CURSO = "EN_CURSO";
    private static final String ESTADO_FINALIZADA = "FINALIZADA";
    private static final String JUGADOR_USUARIO = "USUARIO";
    private static final String JUGADOR_RIVAL = "RIVAL";
    private static final String MISION_CUSTOM = "CUSTOM MISION";

    private final UsuarioRepository usuarioRepository;
    private final SistemaJuegoRepository sistemaJuegoRepository;
    private final PartidaRepository partidaRepository;
    private final RondaPartidaRepository rondaPartidaRepository;
    private final Deployment40kService deployment40kService;
    private final Misiones40kService misiones40kService;
    private final Catalogo40kService catalogo40kService;
    private final CreacionListasService creacionListasService;

    public PartidaService(
            UsuarioRepository usuarioRepository,
            SistemaJuegoRepository sistemaJuegoRepository,
            PartidaRepository partidaRepository,
            RondaPartidaRepository rondaPartidaRepository,
            Deployment40kService deployment40kService,
            Misiones40kService misiones40kService,
            Catalogo40kService catalogo40kService,
            CreacionListasService creacionListasService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sistemaJuegoRepository = sistemaJuegoRepository;
        this.partidaRepository = partidaRepository;
        this.rondaPartidaRepository = rondaPartidaRepository;
        this.deployment40kService = deployment40kService;
        this.misiones40kService = misiones40kService;
        this.catalogo40kService = catalogo40kService;
        this.creacionListasService = creacionListasService;
    }

    public NuevaPartidaView prepararNuevaPartida(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return new NuevaPartidaView(usuario.getNombreUsuario());
    }

    @Transactional
    public Long crearPartida(Long usuarioId, String sistemaJuegoCodigo, String estiloJuego) {
        Usuario usuario = buscarUsuario(usuarioId);
        String codigo = normalizar(sistemaJuegoCodigo);
        if (!FORMATO_40K.equals(codigo)) {
            throw new IllegalArgumentException("Por ahora solo se pueden crear partidas de Warhammer 40k.");
        }

        SistemaJuego sistemaJuego = resolverSistemaJuego(codigo);
        LocalDateTime ahora = LocalDateTime.now();

        Partida partida = new Partida();
        partida.setCreadoPorUsuario(usuario);
        partida.setSistemaJuego(sistemaJuego);
        partida.setJugador1Usuario(usuario);
        partida.setJugador1NombreSnapshot(usuario.getNombreUsuario());
        partida.setJugador1PuntuacionTotal(0);
        partida.setJugador2PuntuacionTotal(0);
        partida.setEsEmpate(false);
        partida.setMostrarCommandPoints(true);
        partida.setUsarCartasGiro(false);
        partida.setJugadorPrimero(JUGADOR_USUARIO);
        partida.setJugadorDefensor(JUGADOR_USUARIO);
        partida.setEstiloJuego(normalizarEstiloJuego(estiloJuego));
        partida.setNombreMision(MISION_CUSTOM);
        partida.setEstado(ESTADO_CREADA);
        partida.setCreadoEn(ahora);
        partida.setActualizadoEn(ahora);
        partidaRepository.save(partida);

        return partida.getId();
    }

    @Transactional(readOnly = true)
    public JugadoresView prepararJugadores(Long usuarioId, Long partidaId) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        Usuario usuario = partida.getCreadoPorUsuario();
        return new JugadoresView(partida, obtenerFacciones(), obtenerListasUsuario(usuario));
    }

    @Transactional
    public void guardarJugadores(Long usuarioId, Long partidaId, JugadoresRequest request) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);

        String jugador1Nombre = normalizar(request.jugador1Nombre());
        String jugador1Faccion = crearTextoEjercito(request.jugador1Faccion(), request.jugador1Ejercito());
        String jugador2Nombre = normalizar(request.jugador2Nombre());
        String jugador2Faccion = crearTextoEjercito(request.jugador2Faccion(), request.jugador2Ejercito());
        TextoLista listaJugador1 = resolverListaJugador1(partida.getCreadoPorUsuario(), request);

        if (jugador1Nombre.isBlank()) {
            jugador1Nombre = partida.getCreadoPorUsuario().getNombreUsuario();
        }
        if (jugador1Faccion.isBlank() || jugador2Nombre.isBlank() || jugador2Faccion.isBlank()) {
            throw new IllegalArgumentException("Debes indicar nombre y faccion de ambos jugadores.");
        }

        partida.setJugador1NombreSnapshot(jugador1Nombre);
        partida.setJugador1FaccionSnapshot(jugador1Faccion);
        partida.setJugador1NombreListaSnapshot(listaJugador1.texto());
        partida.setJugador1PuntosSnapshot(listaJugador1.puntos());
        partida.setJugador2NombreSnapshot(jugador2Nombre);
        partida.setJugador2FaccionSnapshot(jugador2Faccion);
        partida.setJugador2NombreListaSnapshot(textoOpcional(request.jugador2ListaCustom()));
        partida.setJugador2PuntosSnapshot(0);
        partida.setEstado(ESTADO_JUGADORES);
        partida.setActualizadoEn(LocalDateTime.now());
        partidaRepository.save(partida);
    }

    public ConfiguracionView prepararConfiguracion(Long usuarioId, Long partidaId) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        Deployment40kService.CatalogoDesplieguesView catalogo = deployment40kService.obtenerCatalogo();
        return new ConfiguracionView(
                partida,
                crearOpcionesMision(catalogo.desplieguesSimetricos()),
                crearLayoutsConfiguracion(catalogo.layouts()),
                catalogo.desplieguesSimetricos(),
                catalogo.desplieguesMixtos(),
                esJuegoEquilibrado(partida.getEstiloJuego()),
                textoEstiloJuego(partida.getEstiloJuego())
        );
    }

    @Transactional
    public void guardarConfiguracion(Long usuarioId, Long partidaId, ConfiguracionRequest request) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        String estiloJuego = normalizarEstiloJuego(partida.getEstiloJuego());
        String tipoMision = normalizarTipoMision(request.tipoMision());
        Deployment40kService.CatalogoDesplieguesView catalogo = deployment40kService.obtenerCatalogo();
        Misiones40kService.CombinacionMisionView combinacionFija = buscarCombinacionFija(tipoMision);

        String layout = null;
        String despliegue;
        if (esJuegoEquilibrado(estiloJuego)) {
            layout = validarLayoutConfigurado(
                    request.layout(),
                    catalogo.layouts(),
                    combinacionFija
            );
            despliegue = combinacionFija == null
                    ? validarSeleccionVisual(
                            request.despliegue(),
                            catalogo.desplieguesSimetricos(),
                            "Debes elegir un despliegue simetrico."
                    )
                    : resolverDespliegueFijo(
                            combinacionFija.despliegue(),
                            catalogo.desplieguesSimetricos(),
                            "La mision seleccionada requiere un despliegue simetrico valido."
                    );
        } else {
            despliegue = combinacionFija == null
                    ? validarSeleccionVisual(
                            request.despliegue(),
                            catalogo.desplieguesMixtos(),
                            "Debes elegir un despliegue."
                    )
                    : resolverDespliegueFijo(
                            combinacionFija.despliegue(),
                            catalogo.desplieguesMixtos(),
                            "La mision seleccionada requiere un despliegue valido."
                    );
        }

        partida.setLayoutMision(layout);
        partida.setDespliegueMision(despliegue);
        partida.setEstiloJuego(estiloJuego);
        partida.setJugadorDefensor(validarJugador(request.jugadorDefensor()));
        partida.setJugadorPrimero(validarJugador(request.jugadorPrimero()));
        partida.setMostrarCommandPoints(request.mostrarCommandPoints());
        partida.setUsarCartasGiro(request.usarCartasGiro());
        partida.setNombreMision(tipoMision);
        partida.setEstado(ESTADO_CONFIGURADA);
        partida.setIniciadaEn(partida.getIniciadaEn() == null ? LocalDateTime.now() : partida.getIniciadaEn());
        partida.setActualizadoEn(LocalDateTime.now());
        partidaRepository.save(partida);
    }

    public RondaView prepararRonda(Long usuarioId, Long partidaId, int numeroRonda) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        int ronda = ajustarRonda(numeroRonda);
        RondaPartida rondaPartida = rondaPartidaRepository.findByPartidaAndNumeroRonda(partida, ronda).orElse(null);

        RondaPartida anterior = ronda > 1 ? rondaPartidaRepository.findByPartidaAndNumeroRonda(partida, ronda - 1).orElse(null) : null;
        JugadorRondaView jugador1 = crearJugadorRonda(partida, rondaPartida, anterior, JUGADOR_USUARIO, "jugador1");
        JugadorRondaView jugador2 = crearJugadorRonda(partida, rondaPartida, anterior, JUGADOR_RIVAL, "jugador2");
        JugadorRondaView izquierda = JUGADOR_RIVAL.equals(partida.getJugadorPrimero()) ? jugador2 : jugador1;
        JugadorRondaView derecha = JUGADOR_RIVAL.equals(partida.getJugadorPrimero()) ? jugador1 : jugador2;

        return new RondaView(
                partida,
                ronda,
                crearResumenConfiguracion(partida),
                izquierda,
                derecha,
                misiones40kService.obtenerPrimeraMisionPrincipal(),
                misiones40kService.obtenerMisionesSecundarias(),
                rondaPartida
        );
    }

    @Transactional
    public void guardarRonda(Long usuarioId, Long partidaId, int numeroRonda, Map<String, String> params) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        int ronda = ajustarRonda(numeroRonda);

        RondaPartida rondaPartida = rondaPartidaRepository
                .findByPartidaAndNumeroRonda(partida, ronda)
                .orElseGet(RondaPartida::new);

        boolean nuevo = rondaPartida.getId() == null;
        LocalDateTime ahora = LocalDateTime.now();

        rondaPartida.setPartida(partida);
        rondaPartida.setNumeroRonda(ronda);
        rondaPartida.setJugadorConPrioridad(JUGADOR_USUARIO.equals(partida.getJugadorPrimero()) ? partida.getJugador1Usuario() : partida.getJugador2Usuario());
        RondaPartida anterior = ronda > 1 ? rondaPartidaRepository.findByPartidaAndNumeroRonda(partida, ronda - 1).orElse(null) : null;
        var cp1 = prepararCp(params, "jugador1", rondaPartida.getCpJugador1Inicio(), anterior == null ? 0 : valor(anterior.getCpJugador1Fin()));
        var cp2 = prepararCp(params, "jugador2", rondaPartida.getCpJugador2Inicio(), anterior == null ? 0 : valor(anterior.getCpJugador2Fin()));
        rondaPartida.setCpJugador1Inicio(cp1.inicio());
        rondaPartida.setCpJugador1Fin(cp1.fin());
        rondaPartida.setCpJugador2Inicio(cp2.inicio());
        rondaPartida.setCpJugador2Fin(cp2.fin());
        rondaPartida.setPrimariaJugador1(entero(params.get("jugador1Primaria")));
        rondaPartida.setPrimariaJugador2(entero(params.get("jugador2Primaria")));
        rondaPartida.setSecundariaJugador1(entero(params.get("jugador1Secundaria")));
        rondaPartida.setSecundariaJugador2(entero(params.get("jugador2Secundaria")));
        rondaPartida.setBonusJugador1(0);
        rondaPartida.setBonusJugador2(0);
        rondaPartida.setTotalAcumuladoJugador1(0);
        rondaPartida.setTotalAcumuladoJugador2(0);
        rondaPartida.setDetalleJugador1(guardarDetalleRonda(params.get("jugador1Detalle"), cp1));
        rondaPartida.setDetalleJugador2(guardarDetalleRonda(params.get("jugador2Detalle"), cp2));
        rondaPartida.setNotas(textoOpcional(params.get("notas")));
        if (nuevo) {
            rondaPartida.setCreadoEn(ahora);
        }
        rondaPartida.setActualizadoEn(ahora);

        rondaPartidaRepository.save(rondaPartida);
        recalcularTotales(partida);
    }

    public FinalPartidaView prepararFinal(Long usuarioId, Long partidaId) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        List<RondaPartida> rondas = rondaPartidaRepository.findByPartidaOrderByNumeroRondaAsc(partida);
        return new FinalPartidaView(partida, rondas, ganadorTexto(partida));
    }

    @Transactional
    public void finalizarPartida(Long usuarioId, Long partidaId) {
        Partida partida = buscarPartidaUsuario(usuarioId, partidaId);
        recalcularTotales(partida);
        partida.setEstado(ESTADO_FINALIZADA);
        partida.setFinalizadaEn(LocalDateTime.now());
        partida.setActualizadoEn(LocalDateTime.now());
        partidaRepository.save(partida);
    }

    private void recalcularTotales(Partida partida) {
        List<RondaPartida> rondas = rondaPartidaRepository.findByPartidaOrderByNumeroRondaAsc(partida);
        int total1 = 0;
        int total2 = 0;
        for (RondaPartida ronda : rondas) {
            total1 += valor(ronda.getPrimariaJugador1()) + valor(ronda.getSecundariaJugador1()) + valor(ronda.getBonusJugador1());
            total2 += valor(ronda.getPrimariaJugador2()) + valor(ronda.getSecundariaJugador2()) + valor(ronda.getBonusJugador2());
            ronda.setTotalAcumuladoJugador1(total1);
            ronda.setTotalAcumuladoJugador2(total2);
            rondaPartidaRepository.save(ronda);
        }

        partida.setJugador1PuntuacionTotal(total1);
        partida.setJugador2PuntuacionTotal(total2);
        partida.setEsEmpate(total1 == total2);
        if (total1 > total2) {
            partida.setGanadorUsuario(partida.getJugador1Usuario());
        } else {
            partida.setGanadorUsuario(null);
        }
        partida.setEstado(ESTADO_EN_CURSO);
        partida.setActualizadoEn(LocalDateTime.now());
        partidaRepository.save(partida);
    }

    private String ganadorTexto(Partida partida) {
        int total1 = valor(partida.getJugador1PuntuacionTotal());
        int total2 = valor(partida.getJugador2PuntuacionTotal());
        if (total1 == total2) {
            return "Empate";
        }
        return total1 > total2 ? partida.getJugador1NombreSnapshot() : partida.getJugador2NombreSnapshot();
    }

    private JugadorRondaView crearJugadorRonda(Partida partida, RondaPartida ronda, RondaPartida anterior, String tipoJugador, String prefijo) {
        boolean usuario = JUGADOR_USUARIO.equals(tipoJugador);
        String nombre = usuario ? partida.getJugador1NombreSnapshot() : partida.getJugador2NombreSnapshot();
        String faccion = usuario ? partida.getJugador1FaccionSnapshot() : partida.getJugador2FaccionSnapshot();
        String lista = usuario ? partida.getJugador1NombreListaSnapshot() : partida.getJugador2NombreListaSnapshot();
        boolean primero = tipoJugador.equals(partida.getJugadorPrimero());
        boolean defensor = tipoJugador.equals(partida.getJugadorDefensor());
        int cpInicio = anterior == null ? 0 : valor(usuario ? anterior.getCpJugador1Fin() : anterior.getCpJugador2Fin());
        int cpFin = cpInicio;
        int primaria = 0;
        int secundaria = 0;
        String detalle = "";

        if (ronda != null) {
            if (usuario) {
                cpInicio = valor(ronda.getCpJugador1Inicio());
                cpFin = valor(ronda.getCpJugador1Fin());
                primaria = valor(ronda.getPrimariaJugador1());
                secundaria = valor(ronda.getSecundariaJugador1());
                detalle = normalizar(ronda.getDetalleJugador1());
            } else {
                cpInicio = valor(ronda.getCpJugador2Inicio());
                cpFin = valor(ronda.getCpJugador2Fin());
                primaria = valor(ronda.getPrimariaJugador2());
                secundaria = valor(ronda.getSecundariaJugador2());
                detalle = normalizar(ronda.getDetalleJugador2());
            }
        }

        JsonNode datos = leerDetalleRonda(detalle);
        int ganados = datos.path("cp").path("ganados").asInt(Math.max(0, cpFin - cpInicio));
        int gastados = datos.path("cp").path("gastados").asInt(Math.max(0, cpInicio - cpFin));
        detalle = (datos.isArray() ? datos : datos.path("misiones")).toString();

        return new JugadorRondaView(
                prefijo,
                tipoJugador,
                nombre,
                faccion,
                lista,
                primero,
                defensor,
                cpInicio,
                cpFin,
                ganados,
                gastados,
                primaria,
                secundaria,
                detalle
        );
    }

    private record CpRonda(int inicio, int ganados, int gastados, int fin) { }

    private CpRonda prepararCp(Map<String, String> params, String prefijo, Integer inicioGuardado, int saldoAnterior) {
        // Las peticiones antiguas siguen usando los saldos de inicio y fin.
        if (!params.containsKey(prefijo + "CpGanados")) {
            int inicio = entero(params.get(prefijo + "CpInicio"));
            int fin = entero(params.get(prefijo + "CpFin"));
            return new CpRonda(inicio, Math.max(0, fin - inicio), Math.max(0, inicio - fin), fin);
        }
        int inicio = inicioGuardado == null ? saldoAnterior : inicioGuardado;
        int ganados = leerCantidadCp(params.get(prefijo + "CpGanados"));
        int gastados = leerCantidadCp(params.get(prefijo + "CpGastados"));
        long fin = (long) inicio + ganados - gastados;
        if (fin < 0 || fin > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Los CP gastados no pueden superar los disponibles y el saldo debe ser valido.");
        }
        return new CpRonda(inicio, ganados, gastados, (int) fin);
    }

    private int leerCantidadCp(String texto) {
        try {
            int cantidad = Integer.parseInt(texto == null || texto.isBlank() ? "0" : texto);
            if (cantidad >= 0) return cantidad;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Los CP deben ser numeros enteros mayores o iguales a cero.");
    }

    private JsonNode leerDetalleRonda(String detalle) {
        if (detalle == null || detalle.isBlank()) return objectMapper.createArrayNode();
        try {
            JsonNode datos = objectMapper.readTree(detalle);
            if (datos != null && (datos.isArray() || datos.path("misiones").isArray())) return datos;
        } catch (java.io.IOException ignored) { }
        throw new IllegalArgumentException("El detalle de las misiones de la ronda no es valido.");
    }

    private String guardarDetalleRonda(String detalle, CpRonda cp) {
        JsonNode datos = leerDetalleRonda(detalle);
        var resultado = objectMapper.createObjectNode();
        resultado.set("misiones", datos.isArray() ? datos : datos.path("misiones"));
        resultado.putObject("cp").put("ganados", cp.ganados()).put("gastados", cp.gastados());
        return resultado.toString();
    }

    private Partida buscarPartidaUsuario(Long usuarioId, Long partidaId) {
        Usuario usuario = buscarUsuario(usuarioId);
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la partida."));

        if (!partida.getCreadoPorUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("No puedes editar esta partida.");
        }
        return partida;
    }

    private Usuario buscarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("La sesion ha caducado. Inicia sesion de nuevo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("La cuenta de usuario no esta activa.");
        }
        return usuario;
    }

    private SistemaJuego resolverSistemaJuego(String codigo) {
        SistemaJuego sistemaJuego = sistemaJuegoRepository.findByCodigo(codigo).orElse(null);
        if (sistemaJuego != null) {
            return sistemaJuego;
        }

        SistemaJuego nuevo = new SistemaJuego();
        nuevo.setCodigo(codigo);
        nuevo.setNombre("Warhammer 40.000");
        nuevo.setEdicion("11a edicion");
        nuevo.setActivo(true);
        nuevo.setCreadoEn(LocalDateTime.now());
        return sistemaJuegoRepository.save(nuevo);
    }

    private String validarJugador(String valor) {
        String texto = normalizar(valor);
        if (JUGADOR_RIVAL.equals(texto)) {
            return JUGADOR_RIVAL;
        }
        return JUGADOR_USUARIO;
    }

    private String validarSeleccionVisual(
            String codigo,
            List<Deployment40kService.OpcionVisualView> opciones,
            String mensajeError
    ) {
        String valor = normalizar(codigo);
        if (valor.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }
        for (Deployment40kService.OpcionVisualView opcion : opciones) {
            if (valor.equals(opcion.codigo())) {
                return opcion.codigo();
            }
        }
        throw new IllegalArgumentException(mensajeError);
    }

    private String validarLayoutConfigurado(
            String codigo,
            List<Deployment40kService.OpcionVisualView> layouts,
            Misiones40kService.CombinacionMisionView combinacionFija
    ) {
        String layout = validarSeleccionVisual(codigo, layouts, "Debes elegir un layout.");
        if (combinacionFija == null || combinacionFija.layoutsRecomendados().isEmpty()) {
            return layout;
        }

        Deployment40kService.OpcionVisualView layoutSeleccionado = buscarOpcionVisual(layout, layouts);
        Integer numeroLayout = layoutSeleccionado == null ? null : extraerNumeroLayout(layoutSeleccionado.nombre());
        if (numeroLayout == null || !combinacionFija.layoutsRecomendados().contains(numeroLayout)) {
            throw new IllegalArgumentException("La mision seleccionada solo permite los layouts recomendados.");
        }
        return layout;
    }

    private List<MisionOptionView> crearOpcionesMision(List<Deployment40kService.OpcionVisualView> desplieguesSimetricos) {
        List<MisionOptionView> opciones = new ArrayList<>();
        opciones.add(new MisionOptionView(MISION_CUSTOM, MISION_CUSTOM, List.of(), null, null));
        for (Misiones40kService.CombinacionMisionView combinacion : misiones40kService.obtenerCombinacionesPorDefecto()) {
            Deployment40kService.OpcionVisualView despliegueFijo = buscarOpcionVisual(
                    combinacion.despliegue(),
                    desplieguesSimetricos
            );
            opciones.add(new MisionOptionView(
                    "MISION " + combinacion.letra(),
                    "MISION " + combinacion.letra(),
                    combinacion.layoutsRecomendados(),
                    despliegueFijo == null ? null : despliegueFijo.codigo(),
                    despliegueFijo == null ? combinacion.despliegue() : despliegueFijo.nombre()
            ));
        }
        return List.copyOf(opciones);
    }

    private Misiones40kService.CombinacionMisionView buscarCombinacionFija(String tipoMision) {
        if (MISION_CUSTOM.equals(tipoMision)) {
            return null;
        }

        String letra = normalizar(tipoMision).toUpperCase();
        if (letra.startsWith("MISION ")) {
            letra = letra.substring("MISION ".length()).trim();
        }
        if (letra.isBlank()) {
            return null;
        }

        for (Misiones40kService.CombinacionMisionView combinacion : misiones40kService.obtenerCombinacionesPorDefecto()) {
            if (letra.equals(combinacion.letra())) {
                return combinacion;
            }
        }
        throw new IllegalArgumentException("La mision seleccionada no es valida.");
    }

    private String resolverDespliegueFijo(
            String despliegueConfigurado,
            List<Deployment40kService.OpcionVisualView> opciones,
            String mensajeError
    ) {
        Deployment40kService.OpcionVisualView despliegue = buscarOpcionVisual(despliegueConfigurado, opciones);
        if (despliegue == null) {
            throw new IllegalArgumentException(mensajeError);
        }
        return despliegue.codigo();
    }

    private Deployment40kService.OpcionVisualView buscarOpcionVisual(
            String codigoONombre,
            List<Deployment40kService.OpcionVisualView> opciones
    ) {
        String valor = normalizar(codigoONombre);
        if (valor.isBlank()) {
            return null;
        }
        for (Deployment40kService.OpcionVisualView opcion : opciones) {
            if (valor.equals(opcion.codigo()) || valor.equals(opcion.nombre())) {
                return opcion;
            }
        }
        return null;
    }

    private List<LayoutOptionView> crearLayoutsConfiguracion(List<Deployment40kService.OpcionVisualView> layouts) {
        List<LayoutOptionView> resultado = new ArrayList<>();
        for (Deployment40kService.OpcionVisualView layout : layouts) {
            resultado.add(new LayoutOptionView(
                    layout.codigo(),
                    layout.nombre(),
                    layout.imagenUrl(),
                    extraerNumeroLayout(layout.nombre())
            ));
        }
        return List.copyOf(resultado);
    }

    private Integer extraerNumeroLayout(String nombreLayout) {
        String numero = normalizar(nombreLayout).replaceAll("\\D+", "");
        if (numero.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(numero);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ResumenConfiguracionView crearResumenConfiguracion(Partida partida) {
        Deployment40kService.OpcionVisualView layout = deployment40kService.buscarLayout(partida.getLayoutMision());
        Deployment40kService.OpcionVisualView despliegue = deployment40kService.buscarDespliegue(partida.getDespliegueMision());
        return new ResumenConfiguracionView(
                textoEstiloJuego(partida.getEstiloJuego()),
                normalizarTipoMision(partida.getNombreMision()),
                layout == null ? "" : layout.nombre(),
                despliegue == null ? "" : despliegue.nombre()
        );
    }

    private List<FaccionPartidaView> obtenerFacciones() {
        Catalogo40kService.Catalogo40kData catalogo = catalogo40kService.getData();
        if (catalogo == null) {
            catalogo = catalogo40kService.actualizarCatalogo();
        }

        List<FaccionPartidaView> facciones = new ArrayList<>();
        for (Map.Entry<String, Map<String, Catalogo40kService.Ejercito40k>> entrada : catalogo.facciones().entrySet()) {
            facciones.add(new FaccionPartidaView(entrada.getKey(), new ArrayList<>(entrada.getValue().keySet())));
        }
        return facciones;
    }

    private List<ListaPartidaView> obtenerListasUsuario(Usuario usuario) {
        List<ListaPartidaView> listas = new ArrayList<>();
        for (CreacionListasService.ListaGuardadaView lista : creacionListasService.obtenerListasGuardadas(usuario.getNombreUsuario())) {
            listas.add(new ListaPartidaView(
                    lista.listaId(),
                    lista.nombreLista(),
                    lista.faccion(),
                    lista.ejercito(),
                    lista.puntosActuales(),
                    lista.limitePuntos()
            ));
        }
        return listas;
    }

    private TextoLista resolverListaJugador1(Usuario usuario, JugadoresRequest request) {
        String tipo = normalizar(request.jugador1ListaTipo());
        if (tipo.isBlank() || "NONE".equals(tipo)) {
            return new TextoLista(null, 0);
        }
        if ("CUSTOM".equals(tipo)) {
            return new TextoLista(textoOpcional(request.jugador1ListaCustom()), 0);
        }

        try {
            Long listaId = Long.parseLong(tipo);
            CreacionListasService.ListaGuardadaView lista = creacionListasService.obtenerListaGuardadaPorId(usuario.getNombreUsuario(), listaId);
            String texto = lista.nombreLista() + " (" + lista.puntosActuales() + "/" + lista.limitePuntos() + " pts)";
            return new TextoLista(texto, lista.puntosActuales());
        } catch (NumberFormatException ex) {
            return new TextoLista(null, 0);
        }
    }

    private String crearTextoEjercito(String faccion, String ejercito) {
        String textoFaccion = normalizar(faccion);
        String textoEjercito = normalizar(ejercito);
        if (textoFaccion.isBlank()) {
            return "";
        }
        if (textoEjercito.isBlank() || textoFaccion.equals(textoEjercito)) {
            return textoFaccion;
        }
        return textoFaccion + " - " + textoEjercito;
    }

    private int ajustarRonda(int ronda) {
        if (ronda < 1) {
            return 1;
        }
        if (ronda > 5) {
            return 5;
        }
        return ronda;
    }

    private Integer entero(String valor) {
        try {
            return Math.max(0, Integer.parseInt(normalizar(valor)));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }

    private String textoOpcional(String texto) {
        String normalizado = normalizar(texto);
        return normalizado.isBlank() ? null : normalizado;
    }

    private String normalizarEstiloJuego(String estiloJuego) {
        String estilo = normalizar(estiloJuego).toUpperCase();
        if (ESTILO_ASIMETRICO.equals(estilo)) {
            return ESTILO_ASIMETRICO;
        }
        return ESTILO_EQUILIBRADO;
    }

    private boolean esJuegoEquilibrado(String estiloJuego) {
        return ESTILO_EQUILIBRADO.equals(normalizarEstiloJuego(estiloJuego));
    }

    private String textoEstiloJuego(String estiloJuego) {
        return esJuegoEquilibrado(estiloJuego) ? "Juego equilibrado" : "Juego asimetrico";
    }

    private String normalizarTipoMision(String tipoMision) {
        String mision = normalizar(tipoMision);
        return mision.isBlank() ? MISION_CUSTOM : mision;
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public record NuevaPartidaView(String nombreUsuario) {
    }

    public record JugadoresRequest(
            String jugador1Nombre,
            String jugador1Faccion,
            String jugador1Ejercito,
            String jugador1ListaTipo,
            String jugador1ListaCustom,
            String jugador2Nombre,
            String jugador2Faccion,
            String jugador2Ejercito,
            String jugador2ListaCustom
    ) {
    }

    public record ConfiguracionRequest(
            String tipoMision,
            String layout,
            String despliegue,
            String jugadorDefensor,
            String jugadorPrimero,
            boolean mostrarCommandPoints,
            boolean usarCartasGiro
    ) {
    }

    public record FaccionPartidaView(String nombre, List<String> ejercitos) {
    }

    public record ListaPartidaView(
            Long listaId,
            String nombreLista,
            String faccion,
            String ejercito,
            Integer puntosActuales,
            Integer limitePuntos
    ) {
    }

    private record TextoLista(String texto, Integer puntos) {
    }

    public record JugadoresView(
            Partida partida,
            List<FaccionPartidaView> facciones,
            List<ListaPartidaView> listasUsuario
    ) {
    }

    public record MisionOptionView(
            String codigo,
            String nombre,
            List<Integer> layoutsRecomendados,
            String despliegueFijoCodigo,
            String despliegueFijoNombre
    ) {
    }

    public record LayoutOptionView(
            String codigo,
            String nombre,
            String imagenUrl,
            Integer numero
    ) {
    }

    public record ConfiguracionView(
            Partida partida,
            List<MisionOptionView> opcionesMision,
            List<LayoutOptionView> layouts,
            List<Deployment40kService.OpcionVisualView> desplieguesSimetricos,
            List<Deployment40kService.OpcionVisualView> desplieguesMixtos,
            boolean juegoEquilibrado,
            String estiloJuegoTexto
    ) {
    }

    public record ResumenConfiguracionView(
            String estiloJuego,
            String mision,
            String layout,
            String despliegue
    ) {
    }

    public record JugadorRondaView(
            String prefijo,
            String tipoJugador,
            String nombre,
            String faccion,
            String lista,
            boolean primero,
            boolean defensor,
            int cpInicio,
            int cpFin,
            int cpGanados,
            int cpGastados,
            int primaria,
            int secundaria,
            String detalle
    ) {
    }

    public record RondaView(
            Partida partida,
            int numeroRonda,
            ResumenConfiguracionView resumenConfiguracion,
            JugadorRondaView izquierda,
            JugadorRondaView derecha,
            Misiones40kService.MisionView misionPrincipal,
            List<Misiones40kService.MisionView> misionesSecundarias,
            RondaPartida rondaGuardada
    ) {
    }

    public record FinalPartidaView(
            Partida partida,
            List<RondaPartida> rondas,
            String ganadorTexto
    ) {
    }
}
