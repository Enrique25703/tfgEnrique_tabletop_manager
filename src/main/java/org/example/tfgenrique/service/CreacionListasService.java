package org.example.tfgenrique.service;

import org.example.tfgenrique.dao.FuenteCatalogoRepository;
import org.example.tfgenrique.dao.ListaEjercitoRepository;
import org.example.tfgenrique.dao.ListaEjercitoConsultaRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.dao.VersionListaEjercitoRepository;
import org.example.tfgenrique.entity.FuenteCatalogo;
import org.example.tfgenrique.entity.ListaEjercito;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.entity.VersionListaEjercito;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CreacionListasService {

    private static final String VERSION_ESQUEMA_JSON = "1.0";
    private static final String FORMATO_40K = "WH40K_11";
    private static final String FORMATO_AOS = "AOS_4";
    private static final String NOMBRE_FORMATO_40K = "Warhammer 40,000";
    private static final String NOMBRE_FORMATO_AOS = "Age of Sigmar";
    private static final String EDICION_FORMATO_40K = "11a edicion";
    private static final String EDICION_FORMATO_AOS = "4a edicion";
    private static final String FUENTE_CATALOGO_40K = "BSData";
    private static final String FUENTE_CATALOGO_AOS = "BSData";
    private static final String TIPO_FUENTE_40K = "GIT";
    private static final String TIPO_FUENTE_AOS = "GIT";
    private static final String URL_FUENTE_40K = "https://github.com/BSData/wh40k-11e";
    private static final String URL_FUENTE_AOS = "https://github.com/BSData/age-of-sigmar-4th";
    private static final String RAMA_FUENTE_40K = "main";
    private static final String RAMA_FUENTE_AOS = "main";
    private static final int LIMITE_PUNTOS_POR_DEFECTO = 2000;
    private static final String CATEGORIA_DISPOSICION = "disposicion";
    private static final String CATEGORIA_PERSONAJES = "personajes";
    private static final String CATEGORIA_LINEA = "linea";
    private static final String CATEGORIA_TRANSPORTE = "transporte";
    private static final String CATEGORIA_PESADO = "pesado";
    private static final String CATEGORIA_OTROS = "otros";

    private final UsuarioRepository usuarioRepository;
    private final SistemaJuegoRepository sistemaJuegoRepository;
    private final FuenteCatalogoRepository fuenteCatalogoRepository;
    private final ListaEjercitoRepository listaEjercitoRepository;
    private final VersionListaEjercitoRepository versionListaEjercitoRepository;
    private final ListaEjercitoConsultaRepository listaEjercitoConsultaRepository;

    public CreacionListasService(
            UsuarioRepository usuarioRepository,
            SistemaJuegoRepository sistemaJuegoRepository,
            FuenteCatalogoRepository fuenteCatalogoRepository,
            ListaEjercitoRepository listaEjercitoRepository,
            VersionListaEjercitoRepository versionListaEjercitoRepository,
            ListaEjercitoConsultaRepository listaEjercitoConsultaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sistemaJuegoRepository = sistemaJuegoRepository;
        this.fuenteCatalogoRepository = fuenteCatalogoRepository;
        this.listaEjercitoRepository = listaEjercitoRepository;
        this.versionListaEjercitoRepository = versionListaEjercitoRepository;
        this.listaEjercitoConsultaRepository = listaEjercitoConsultaRepository;
    }

    @Transactional
    public GuardadoListaResultado guardarLista(String nombreUsuarioSesion, GuardarListaRequest request) {
        if (nombreUsuarioSesion == null || nombreUsuarioSesion.isBlank()) {
            throw new IllegalArgumentException("No hay un usuario autenticado para guardar la lista.");
        }
        if (request == null) {
            throw new IllegalArgumentException("No se han recibido datos de la lista.");
        }

        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuarioSesion)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));

        String formatoJuego = normalizarTexto(request.formatoJuego());
        if (formatoJuego.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el formato de juego.");
        }

        SistemaJuego sistemaJuego = resolverSistemaJuego(formatoJuego);
        FuenteCatalogo fuenteCatalogo = resolverFuenteCatalogo(sistemaJuego);

        String nombreLista = normalizarTexto(request.nombreLista());
        if (nombreLista.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre para la lista.");
        }

        int limitePuntos = request.limitePuntos() == null || request.limitePuntos() <= 0
                ? LIMITE_PUNTOS_POR_DEFECTO
                : request.limitePuntos();

        int puntosTotales = request.puntosTotales() == null ? 0 : request.puntosTotales();
        if (puntosTotales <= 0) {
            throw new IllegalArgumentException("La lista debe tener puntos validos para guardarse.");
        }
        if (request.datosListaJson() == null || request.datosListaJson().isBlank()) {
            throw new IllegalArgumentException("No se han recibido las unidades de la lista.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        ListaEjercito listaEjercito = listaEjercitoRepository
                .findForUpdate(usuario, sistemaJuego, nombreLista)
                .orElseGet(() -> crearListaEjercito(usuario, sistemaJuego, nombreLista, ahora));

        int numeroVersion = (listaEjercito.getNumeroVersionActual() == null ? 0 : listaEjercito.getNumeroVersionActual()) + 1;

        String faccion = normalizarTexto(request.faccion());
        listaEjercito.setNombreFaccionSnapshot(faccion);
        listaEjercito.setLimitePuntos(limitePuntos);
        listaEjercito.setPuntosActuales(puntosTotales);
        listaEjercito.setNumeroVersionActual(numeroVersion);
        listaEjercito.setActualizadoEn(ahora);
        listaEjercitoRepository.save(listaEjercito);

        String datosListaJson = request.datosListaJson().trim();

        VersionListaEjercito version = new VersionListaEjercito();
        version.setListaEjercito(listaEjercito);
        version.setNumeroVersion(numeroVersion);
        version.setFuenteCatalogo(fuenteCatalogo);
        version.setCommitCatalogoHash(null);
        version.setRevisionCatalogo(request.revisionCatalogo());
        version.setVersionEsquemaJson(VERSION_ESQUEMA_JSON);
        version.setChecksumDatos(calcularChecksum(datosListaJson));
        version.setPuntosTotales(puntosTotales);
        version.setDatosLista(datosListaJson);
        version.setCreadoEn(ahora);
        versionListaEjercitoRepository.save(version);

        return new GuardadoListaResultado(listaEjercito.getId(), version.getId(), numeroVersion);
    }

    @Transactional(readOnly = true)
    public CreadorLista40kView prepararCreadorLista40k(
            String formatoJuego,
            String faccion,
            String ejercito,
            String nombreLista,
            Integer limitePuntos,
            Catalogo40kService.Catalogo40kData catalogo
    ) {
        Catalogo40kService.Ejercito40k ejercitoData = catalogo == null ? null : catalogo.buscarEjercito(faccion, ejercito);
        Catalogo40kService.ReglasEjercito40k reglasEjercito = catalogo == null
                ? Catalogo40kService.ReglasEjercito40k.vacias()
                : catalogo.buscarReglasEjercito(faccion, ejercito);
        Map<String, CategoriaCreadorListaView> categorias = crearCategoriasCreador();
        if (ejercitoData != null) {
            for (Catalogo40kService.Unidad40k unidad : ejercitoData.unidades()) {
                String categoria = resolverCategoriaUnidad(unidad.roles());
                Catalogo40kService.VinculosUnidad40k vinculos = reglasEjercito.buscarVinculos(unidad.nombre());
                categorias.get(categoria).unidades().add(new UnidadCatalogoView(
                        valorSeguroVista(unidad.nombre()),
                        valorSeguroVista(unidad.roles()),
                        valorSeguroVista(unidad.puntos()),
                        obtenerPuntosMinimos(unidad.puntos()),
                        categoria,
                        valorSeguroVista(unidad.armas()),
                        serializarPerfilesArmas(unidad.armasDetalle()),
                        valorSeguroVista(unidad.habilidades()),
                        valorSeguroVista(unidad.palabrasClaveFaccion()),
                        valorSeguroVista(unidad.palabrasClave()),
                        serializarConfiguracionUnidad(unidad.gruposMiniaturas(), unidad.opcionesComposicion()),
                        esUnidadLegend40k(unidad),
                        esUnidadFortificacion40k(unidad),
                        esUnidadAliada40k(unidad),
                        esUnidadBattleline40k(unidad),
                        esUnidadEpicHero40k(unidad),
                        esUnidadCharacter40k(unidad),
                        vinculos.leader(),
                        vinculos.support(),
                        serializarListaTextosComoJson(vinculos.unidadesCompatibles())
                ));
            }
        }

        return new CreadorLista40kView(
                normalizarTexto(formatoJuego),
                normalizarTexto(nombreLista),
                normalizarTexto(faccion),
                normalizarTexto(ejercito),
                limitePuntos == null || limitePuntos <= 0 ? LIMITE_PUNTOS_POR_DEFECTO : limitePuntos,
                List.copyOf(categorias.values()),
                List.of(),
                reglasEjercito.destacamentos(),
                List.of()
        );
    }

    private ListaEjercito crearListaEjercito(Usuario usuario, SistemaJuego sistemaJuego, String nombreLista, LocalDateTime ahora) {
        ListaEjercito listaEjercito = new ListaEjercito();
        listaEjercito.setPropietarioUsuario(usuario);
        listaEjercito.setSistemaJuego(sistemaJuego);
        listaEjercito.setNombre(nombreLista);
        listaEjercito.setLimitePuntos(LIMITE_PUNTOS_POR_DEFECTO);
        listaEjercito.setPuntosActuales(0);
        listaEjercito.setVisibilidad("PRIVADA");
        listaEjercito.setNumeroVersionActual(0);
        listaEjercito.setArchivada(false);
        listaEjercito.setCreadoEn(ahora);
        listaEjercito.setActualizadoEn(ahora);
        return listaEjercitoRepository.save(listaEjercito);
    }

    private Map<String, CategoriaCreadorListaView> crearCategoriasCreador() {
        Map<String, CategoriaCreadorListaView> categorias = new LinkedHashMap<>();
        categorias.put(CATEGORIA_DISPOSICION, new CategoriaCreadorListaView(CATEGORIA_DISPOSICION, "Disposicion", new ArrayList<>()));
        categorias.put(CATEGORIA_PERSONAJES, new CategoriaCreadorListaView(CATEGORIA_PERSONAJES, "Personajes", new ArrayList<>()));
        categorias.put(CATEGORIA_LINEA, new CategoriaCreadorListaView(CATEGORIA_LINEA, "Linea", new ArrayList<>()));
        categorias.put(CATEGORIA_TRANSPORTE, new CategoriaCreadorListaView(CATEGORIA_TRANSPORTE, "Transporte", new ArrayList<>()));
        categorias.put(CATEGORIA_PESADO, new CategoriaCreadorListaView(CATEGORIA_PESADO, "Vehiculos y monstruos", new ArrayList<>()));
        categorias.put(CATEGORIA_OTROS, new CategoriaCreadorListaView(CATEGORIA_OTROS, "Otras unidades", new ArrayList<>()));
        return categorias;
    }

    private Map<String, CategoriaCreadorListaView> crearCategoriasCreadorAos() {
        Map<String, CategoriaCreadorListaView> categorias = new LinkedHashMap<>();
        categorias.put(CATEGORIA_PERSONAJES, new CategoriaCreadorListaView(CATEGORIA_PERSONAJES, "Heroes", new ArrayList<>()));
        categorias.put(CATEGORIA_LINEA, new CategoriaCreadorListaView(CATEGORIA_LINEA, "Battleline", new ArrayList<>()));
        categorias.put(CATEGORIA_TRANSPORTE, new CategoriaCreadorListaView(CATEGORIA_TRANSPORTE, "Apoyo y maquinas", new ArrayList<>()));
        categorias.put(CATEGORIA_PESADO, new CategoriaCreadorListaView(CATEGORIA_PESADO, "Monstruos y behemoths", new ArrayList<>()));
        categorias.put(CATEGORIA_OTROS, new CategoriaCreadorListaView(CATEGORIA_OTROS, "Otras unidades", new ArrayList<>()));
        return categorias;
    }

    private SistemaJuego resolverSistemaJuego(String formatoJuego) {
        SistemaJuego sistemaJuego = sistemaJuegoRepository.findByCodigo(formatoJuego).orElse(null);
        if (sistemaJuego != null) {
            return sistemaJuego;
        }

        String nombreSistema = obtenerNombreSistema(formatoJuego);
        sistemaJuego = sistemaJuegoRepository.findByNombre(nombreSistema).orElse(null);
        if (sistemaJuego != null) {
            return sistemaJuego;
        }

        return crearSistemaJuego(formatoJuego);
    }

    private SistemaJuego crearSistemaJuego(String formatoJuego) {
        LocalDateTime ahora = LocalDateTime.now();
        SistemaJuego sistemaJuego = new SistemaJuego();
        sistemaJuego.setCodigo(formatoJuego);
        sistemaJuego.setNombre(obtenerNombreSistema(formatoJuego));
        sistemaJuego.setEdicion(obtenerEdicionSistema(formatoJuego));

        sistemaJuego.setActivo(true);
        sistemaJuego.setCreadoEn(ahora);
        return sistemaJuegoRepository.save(sistemaJuego);
    }

    private String obtenerNombreSistema(String formatoJuego) {
        if (FORMATO_40K.equals(formatoJuego)) {
            return NOMBRE_FORMATO_40K;
        }
        if (FORMATO_AOS.equals(formatoJuego)) {
            return NOMBRE_FORMATO_AOS;
        }
        return formatoJuego;
    }

    private String obtenerEdicionSistema(String formatoJuego) {
        if (FORMATO_40K.equals(formatoJuego)) {
            return EDICION_FORMATO_40K;
        }
        if (FORMATO_AOS.equals(formatoJuego)) {
            return EDICION_FORMATO_AOS;
        }
        return null;
    }

    private FuenteCatalogo resolverFuenteCatalogo(SistemaJuego sistemaJuego) {
        return fuenteCatalogoRepository.findFirstBySistemaJuegoAndActivoTrue(sistemaJuego)
                .orElseGet(() -> crearFuenteCatalogoPorDefecto(sistemaJuego));
    }

    private FuenteCatalogo crearFuenteCatalogoPorDefecto(SistemaJuego sistemaJuego) {
        FuenteCatalogo fuenteCatalogo = new FuenteCatalogo();
        fuenteCatalogo.setSistemaJuego(sistemaJuego);
        fuenteCatalogo.setNombreFuente(obtenerNombreFuenteCatalogo(sistemaJuego.getCodigo()));
        fuenteCatalogo.setTipoFuente(obtenerTipoFuenteCatalogo(sistemaJuego.getCodigo()));
        fuenteCatalogo.setUrlRepositorio(obtenerUrlFuenteCatalogo(sistemaJuego.getCodigo()));
        fuenteCatalogo.setRamaPorDefecto(obtenerRamaFuenteCatalogo(sistemaJuego.getCodigo()));
        fuenteCatalogo.setActivo(true);
        fuenteCatalogo.setCreadoEn(LocalDateTime.now());
        return fuenteCatalogoRepository.save(fuenteCatalogo);
    }

    private String obtenerNombreFuenteCatalogo(String formatoJuego) {
        if (FORMATO_AOS.equals(formatoJuego)) {
            return FUENTE_CATALOGO_AOS;
        }
        return FUENTE_CATALOGO_40K;
    }

    private String obtenerTipoFuenteCatalogo(String formatoJuego) {
        if (FORMATO_AOS.equals(formatoJuego)) {
            return TIPO_FUENTE_AOS;
        }
        return TIPO_FUENTE_40K;
    }

    private String obtenerUrlFuenteCatalogo(String formatoJuego) {
        if (FORMATO_AOS.equals(formatoJuego)) {
            return URL_FUENTE_AOS;
        }
        return URL_FUENTE_40K;
    }

    private String obtenerRamaFuenteCatalogo(String formatoJuego) {
        if (FORMATO_AOS.equals(formatoJuego)) {
            return RAMA_FUENTE_AOS;
        }
        return RAMA_FUENTE_40K;
    }

    private String calcularChecksum(String datosListaJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datosListaJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se ha podido calcular el checksum de la lista.", ex);
        }
    }

    private String resolverCategoriaUnidad(String roles) {
        String texto = normalizarTexto(roles).toLowerCase(Locale.ROOT);
        if (texto.contains("character") || texto.contains("epic hero")) {
            return CATEGORIA_PERSONAJES;
        }
        if (texto.contains("battleline")) {
            return CATEGORIA_LINEA;
        }
        if (texto.contains("dedicated transport")) {
            return CATEGORIA_TRANSPORTE;
        }
        if (texto.contains("vehicle") || texto.contains("monster") || texto.contains("walker") || texto.contains("aircraft")) {
            return CATEGORIA_PESADO;
        }
        return CATEGORIA_OTROS;
    }

    private String resolverCategoriaUnidadAos(String roles) {
        String texto = normalizarTexto(roles).toLowerCase(Locale.ROOT);
        if (texto.contains("hero") || texto.contains("wizard") || texto.contains("priest")) {
            return CATEGORIA_PERSONAJES;
        }
        if (texto.contains("battleline")) {
            return CATEGORIA_LINEA;
        }
        if (texto.contains("war machine") || texto.contains("artillery") || texto.contains("terrain")) {
            return CATEGORIA_TRANSPORTE;
        }
        if (texto.contains("monster") || texto.contains("behemoth")) {
            return CATEGORIA_PESADO;
        }
        return CATEGORIA_OTROS;
    }

    private int obtenerPuntosMinimos(String textoPuntos) {
        if (textoPuntos == null || textoPuntos.isBlank()) {
            return 0;
        }

        Integer minimo = null;
        for (String trozo : textoPuntos.split(",")) {
            try {
                int numero = Integer.parseInt(trozo.trim());
                if (minimo == null || numero < minimo) {
                    minimo = numero;
                }
            } catch (NumberFormatException ignored) {
                // Si el texto no es numerico, se ignora ese fragmento.
            }
        }

        return minimo == null ? 0 : minimo;
    }

    private boolean esUnidadLegend40k(Catalogo40kService.Unidad40k unidad) {
        return unirTextoUnidad40k(unidad).contains("legends");
    }

    private boolean esUnidadFortificacion40k(Catalogo40kService.Unidad40k unidad) {
        String texto = unirTextoUnidad40k(unidad);
        return texto.contains("fortification") || texto.contains("fortificacion");
    }

    private boolean esUnidadAliada40k(Catalogo40kService.Unidad40k unidad) {
        String texto = unirTextoUnidad40k(unidad);
        return texto.contains("imperial agents")
                || texto.contains("agents of the imperium")
                || texto.contains("agent of the imperium")
                || texto.contains("freeblade")
                || texto.contains("chaos daemons")
                || texto.contains("chaos demons")
                || texto.contains("allied unit")
                || texto.contains("allies");
    }

    private boolean esUnidadBattleline40k(Catalogo40kService.Unidad40k unidad) {
        return valorSeguroONulo(unidad.roles()).toLowerCase(Locale.ROOT).contains("battleline");
    }

    private boolean esUnidadEpicHero40k(Catalogo40kService.Unidad40k unidad) {
        return valorSeguroONulo(unidad.roles()).toLowerCase(Locale.ROOT).contains("epic hero");
    }

    private boolean esUnidadCharacter40k(Catalogo40kService.Unidad40k unidad) {
        String roles = valorSeguroONulo(unidad.roles()).toLowerCase(Locale.ROOT);
        return roles.contains("character") || roles.contains("epic hero");
    }

    private String unirTextoUnidad40k(Catalogo40kService.Unidad40k unidad) {
        return (
                valorSeguroONulo(unidad.nombre()) + " "
                        + valorSeguroONulo(unidad.roles()) + " "
                        + valorSeguroONulo(unidad.palabrasClaveFaccion()) + " "
                        + valorSeguroONulo(unidad.palabrasClave()) + " "
                        + valorSeguroONulo(unidad.habilidades())
        ).toLowerCase(Locale.ROOT);
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String valorSeguroONulo(String texto) {
        return texto == null ? "" : texto;
    }

    private String serializarConfiguracionUnidad(
            List<Catalogo40kService.GrupoMiniaturas40k> gruposMiniaturas,
            List<Catalogo40kService.OpcionComposicion40k> opcionesComposicion
    ) {
        StringBuilder json = new StringBuilder();
        json.append("{\"gruposMiniaturas\":[");
        for (int indiceGrupo = 0; indiceGrupo < gruposMiniaturas.size(); indiceGrupo++) {
            Catalogo40kService.GrupoMiniaturas40k grupo = gruposMiniaturas.get(indiceGrupo);
            if (indiceGrupo > 0) {
                json.append(',');
            }
            serializarGrupoMiniaturas(json, grupo);
        }
        json.append("],\"opcionesComposicion\":[");
        for (int indiceOpcion = 0; indiceOpcion < opcionesComposicion.size(); indiceOpcion++) {
            Catalogo40kService.OpcionComposicion40k opcion = opcionesComposicion.get(indiceOpcion);
            if (indiceOpcion > 0) {
                json.append(',');
            }

            json.append('{')
                    .append("\"id\":\"").append(escaparJson(opcion.id())).append("\",")
                    .append("\"nombre\":\"").append(escaparJson(opcion.nombre())).append("\",")
                    .append("\"puntos\":").append(opcion.puntos()).append(',')
                    .append("\"seleccionPorDefecto\":").append(opcion.seleccionPorDefecto()).append(',')
                    .append("\"gruposMiniaturas\":[");

            for (int indiceGrupo = 0; indiceGrupo < opcion.gruposMiniaturas().size(); indiceGrupo++) {
                if (indiceGrupo > 0) {
                    json.append(',');
                }
                serializarGrupoMiniaturas(json, opcion.gruposMiniaturas().get(indiceGrupo));
            }

            json.append("]}");
        }
        json.append("]}");
        return json.toString();
    }

    private String serializarConfiguracionUnidadAos(
            List<CatalogoAosService.GrupoMiniaturas40k> gruposMiniaturas,
            List<CatalogoAosService.OpcionComposicion40k> opcionesComposicion
    ) {
        StringBuilder json = new StringBuilder();
        json.append("{\"gruposMiniaturas\":[");
        for (int indiceGrupo = 0; indiceGrupo < gruposMiniaturas.size(); indiceGrupo++) {
            CatalogoAosService.GrupoMiniaturas40k grupo = gruposMiniaturas.get(indiceGrupo);
            if (indiceGrupo > 0) {
                json.append(',');
            }
            serializarGrupoMiniaturasAos(json, grupo);
        }
        json.append("],\"opcionesComposicion\":[");
        for (int indiceOpcion = 0; indiceOpcion < opcionesComposicion.size(); indiceOpcion++) {
            CatalogoAosService.OpcionComposicion40k opcion = opcionesComposicion.get(indiceOpcion);
            if (indiceOpcion > 0) {
                json.append(',');
            }

            json.append('{')
                    .append("\"id\":\"").append(escaparJson(opcion.id())).append("\",")
                    .append("\"nombre\":\"").append(escaparJson(opcion.nombre())).append("\",")
                    .append("\"puntos\":").append(opcion.puntos()).append(',')
                    .append("\"seleccionPorDefecto\":").append(opcion.seleccionPorDefecto()).append(',')
                    .append("\"gruposMiniaturas\":[");

            for (int indiceGrupo = 0; indiceGrupo < opcion.gruposMiniaturas().size(); indiceGrupo++) {
                if (indiceGrupo > 0) {
                    json.append(',');
                }
                serializarGrupoMiniaturasAos(json, opcion.gruposMiniaturas().get(indiceGrupo));
            }

            json.append("]}");
        }
        json.append("]}");
        return json.toString();
    }

    private void serializarGrupoMiniaturas(StringBuilder json, Catalogo40kService.GrupoMiniaturas40k grupo) {
        json.append('{')
                .append("\"id\":\"").append(escaparJson(grupo.id())).append("\",")
                .append("\"nombre\":\"").append(escaparJson(grupo.nombre())).append("\",")
                .append("\"minimo\":").append(grupo.minimo()).append(',')
                .append("\"maximo\":").append(grupo.maximo()).append(',')
                .append("\"modelos\":[");

        for (int indiceModelo = 0; indiceModelo < grupo.modelos().size(); indiceModelo++) {
            Catalogo40kService.ModeloUnidad40k modelo = grupo.modelos().get(indiceModelo);
            if (indiceModelo > 0) {
                json.append(',');
            }

            json.append('{')
                    .append("\"id\":\"").append(escaparJson(modelo.id())).append("\",")
                    .append("\"nombre\":\"").append(escaparJson(modelo.nombre())).append("\",")
                    .append("\"minimo\":").append(modelo.minimo()).append(',')
                    .append("\"maximo\":").append(modelo.maximo()).append(',')
                    .append("\"equipamientoFijo\":");
            serializarListaTextos(json, modelo.equipamientoFijo());
            json.append(",\"gruposEquipamiento\":[");

            for (int indiceEquipamiento = 0; indiceEquipamiento < modelo.gruposEquipamiento().size(); indiceEquipamiento++) {
                Catalogo40kService.GrupoEquipamiento40k grupoEquipamiento = modelo.gruposEquipamiento().get(indiceEquipamiento);
                if (indiceEquipamiento > 0) {
                    json.append(',');
                }

                json.append('{')
                        .append("\"id\":\"").append(escaparJson(grupoEquipamiento.id())).append("\",")
                        .append("\"nombre\":\"").append(escaparJson(grupoEquipamiento.nombre())).append("\",")
                        .append("\"minimo\":").append(grupoEquipamiento.minimo()).append(',')
                        .append("\"maximo\":").append(grupoEquipamiento.maximo()).append(',')
                        .append("\"opciones\":[");

                for (int indiceOpcion = 0; indiceOpcion < grupoEquipamiento.opciones().size(); indiceOpcion++) {
                    Catalogo40kService.OpcionEquipamiento40k opcion = grupoEquipamiento.opciones().get(indiceOpcion);
                    if (indiceOpcion > 0) {
                        json.append(',');
                    }
                    json.append('{')
                            .append("\"id\":\"").append(escaparJson(opcion.id())).append("\",")
                            .append("\"nombre\":\"").append(escaparJson(opcion.nombre())).append("\",")
                            .append("\"seleccionPorDefecto\":").append(opcion.seleccionPorDefecto()).append(',')
                            .append("\"detalleEquipamiento\":");
                    serializarListaTextos(json, opcion.detalleEquipamiento());
                    json.append('}');
                }

                json.append("]}");
            }

            json.append("]}");
        }

        json.append("],\"subgrupos\":[");

        for (int indiceSubgrupo = 0; indiceSubgrupo < grupo.subgrupos().size(); indiceSubgrupo++) {
            if (indiceSubgrupo > 0) {
                json.append(',');
            }
            serializarGrupoMiniaturas(json, grupo.subgrupos().get(indiceSubgrupo));
        }

        json.append("]}");
    }

    private void serializarGrupoMiniaturasAos(StringBuilder json, CatalogoAosService.GrupoMiniaturas40k grupo) {
        json.append('{')
                .append("\"id\":\"").append(escaparJson(grupo.id())).append("\",")
                .append("\"nombre\":\"").append(escaparJson(grupo.nombre())).append("\",")
                .append("\"minimo\":").append(grupo.minimo()).append(',')
                .append("\"maximo\":").append(grupo.maximo()).append(',')
                .append("\"modelos\":[");

        for (int indiceModelo = 0; indiceModelo < grupo.modelos().size(); indiceModelo++) {
            CatalogoAosService.ModeloUnidad40k modelo = grupo.modelos().get(indiceModelo);
            if (indiceModelo > 0) {
                json.append(',');
            }

            json.append('{')
                    .append("\"id\":\"").append(escaparJson(modelo.id())).append("\",")
                    .append("\"nombre\":\"").append(escaparJson(modelo.nombre())).append("\",")
                    .append("\"minimo\":").append(modelo.minimo()).append(',')
                    .append("\"maximo\":").append(modelo.maximo()).append(',')
                    .append("\"equipamientoFijo\":");
            serializarListaTextos(json, modelo.equipamientoFijo());
            json.append(",\"gruposEquipamiento\":[");

            for (int indiceEquipamiento = 0; indiceEquipamiento < modelo.gruposEquipamiento().size(); indiceEquipamiento++) {
                CatalogoAosService.GrupoEquipamiento40k grupoEquipamiento = modelo.gruposEquipamiento().get(indiceEquipamiento);
                if (indiceEquipamiento > 0) {
                    json.append(',');
                }

                json.append('{')
                        .append("\"id\":\"").append(escaparJson(grupoEquipamiento.id())).append("\",")
                        .append("\"nombre\":\"").append(escaparJson(grupoEquipamiento.nombre())).append("\",")
                        .append("\"minimo\":").append(grupoEquipamiento.minimo()).append(',')
                        .append("\"maximo\":").append(grupoEquipamiento.maximo()).append(',')
                        .append("\"opciones\":[");

                for (int indiceOpcion = 0; indiceOpcion < grupoEquipamiento.opciones().size(); indiceOpcion++) {
                    CatalogoAosService.OpcionEquipamiento40k opcion = grupoEquipamiento.opciones().get(indiceOpcion);
                    if (indiceOpcion > 0) {
                        json.append(',');
                    }
                    json.append('{')
                            .append("\"id\":\"").append(escaparJson(opcion.id())).append("\",")
                            .append("\"nombre\":\"").append(escaparJson(opcion.nombre())).append("\",")
                            .append("\"seleccionPorDefecto\":").append(opcion.seleccionPorDefecto()).append(',')
                            .append("\"detalleEquipamiento\":");
                    serializarListaTextos(json, opcion.detalleEquipamiento());
                    json.append('}');
                }

                json.append("]}");
            }

            json.append("]}");
        }

        json.append("],\"subgrupos\":[");

        for (int indiceSubgrupo = 0; indiceSubgrupo < grupo.subgrupos().size(); indiceSubgrupo++) {
            if (indiceSubgrupo > 0) {
                json.append(',');
            }
            serializarGrupoMiniaturasAos(json, grupo.subgrupos().get(indiceSubgrupo));
        }

        json.append("]}");
    }

    private void serializarListaTextos(StringBuilder json, List<String> textos) {
        json.append('[');
        for (int indice = 0; indice < textos.size(); indice++) {
            if (indice > 0) {
                json.append(',');
            }
            json.append('"').append(escaparJson(textos.get(indice))).append('"');
        }
        json.append(']');
    }

    private String serializarListaTextosComoJson(List<String> textos) {
        StringBuilder json = new StringBuilder();
        serializarListaTextos(json, textos == null ? List.of() : textos);
        return json.toString();
    }

    private String serializarPerfilesArmas(List<Catalogo40kService.PerfilArma40k> perfiles) {
        StringBuilder json = new StringBuilder("[");
        List<Catalogo40kService.PerfilArma40k> perfilesSeguros = perfiles == null ? List.of() : perfiles;
        for (int indice = 0; indice < perfilesSeguros.size(); indice++) {
            Catalogo40kService.PerfilArma40k perfil = perfilesSeguros.get(indice);
            if (indice > 0) {
                json.append(',');
            }
            json.append('{')
                    .append("\"nombre\":\"").append(escaparJson(perfil.nombre())).append("\",")
                    .append("\"tipo\":\"").append(escaparJson(perfil.tipo())).append("\",")
                    .append("\"rango\":\"").append(escaparJson(valorPerfilArma(perfil, "Range", "Rango", "Alcance"))).append("\",")
                    .append("\"ataques\":\"").append(escaparJson(valorPerfilArma(perfil, "A", "Attacks", "Ataques"))).append("\",")
                    .append("\"impacta\":\"").append(escaparJson(valorPerfilArma(perfil, "BS", "Ballistic Skill", "HP", "Hit", "WS"))).append("\",")
                    .append("\"fuerza\":\"").append(escaparJson(valorPerfilArma(perfil, "S", "Strength", "Fuerza"))).append("\",")
                    .append("\"penetracion\":\"").append(escaparJson(valorPerfilArma(perfil, "AP", "Armour Penetration", "Armor Penetration", "FP"))).append("\",")
                    .append("\"dano\":\"").append(escaparJson(valorPerfilArma(perfil, "D", "Damage", "Daño"))).append("\"")
                    .append('}');
        }
        return json.append(']').toString();
    }

    private String valorPerfilArma(Catalogo40kService.PerfilArma40k perfil, String... nombres) {
        if (perfil.estadisticas() == null) {
            return "";
        }
        for (String nombre : nombres) {
            for (Catalogo40kService.Estadistica40k estadistica : perfil.estadisticas()) {
                if (nombre.equalsIgnoreCase(valorSeguroONulo(estadistica.nombre()))
                        && !valorSeguroONulo(estadistica.valor()).isBlank()) {
                    return estadistica.valor().trim();
                }
            }
        }
        return "";
    }

    private String escaparJson(String texto) {
        String valor = texto == null ? "" : texto;
        StringBuilder escapado = new StringBuilder(valor.length() + 8);
        for (int indice = 0; indice < valor.length(); indice++) {
            char caracter = valor.charAt(indice);
            switch (caracter) {
                case '\\' -> escapado.append("\\\\");
                case '"' -> escapado.append("\\\"");
                case '\n' -> escapado.append("\\n");
                case '\r' -> escapado.append("\\r");
                case '\t' -> escapado.append("\\t");
                default -> escapado.append(caracter);
            }
        }
        return escapado.toString();
    }

    public record GuardarListaRequest(
            String formatoJuego,
            String nombreLista,
            String faccion,
            String ejercito,
            Integer limitePuntos,
            Integer puntosTotales,
            String revisionCatalogo,
            String datosListaJson
    ) {
    }

    public record GuardadoListaResultado(Long listaId, Long versionId, Integer numeroVersion) {
    }

    public record CreadorLista40kView(
            String formatoJuego,
            String nombreLista,
            String faccion,
            String ejercito,
            Integer limitePuntos,
            List<CategoriaCreadorListaView> categorias,
            List<?> tamanosBatalla,
            List<Catalogo40kService.Destacamento40k> destacamentos,
            List<?> mejoras
    ) {
    }

    public record CategoriaCreadorListaView(
            String id,
            String titulo,
            List<UnidadCatalogoView> unidades
    ) {
    }

    public record UnidadCatalogoView(
            String nombre,
            String roles,
            String puntos,
            Integer puntosBase,
            String categoria,
            String armas,
            String perfilesArmasJson,
            String habilidades,
            String palabrasClaveFaccion,
            String palabrasClave,
            String configuracionJson,
            boolean legend,
            boolean fortificacion,
            boolean aliada,
            boolean battleline,
            boolean epicHero,
            boolean character,
            boolean leader,
            boolean support,
            String compatiblesJson
    ) {
    }

    @Transactional(readOnly = true)
    public CreadorLista40kView prepararCreadorListaAos(
            String formatoJuego,
            String faccion,
            String ejercito,
            String nombreLista,
            Integer limitePuntos,
            CatalogoAosService.Ejercito40k ejercitoData
    ) {
        Map<String, CategoriaCreadorListaView> categorias = crearCategoriasCreadorAos();
        if (ejercitoData != null) {
            for (CatalogoAosService.Unidad40k unidad : ejercitoData.unidades()) {
                String categoria = resolverCategoriaUnidadAos(unidad.roles());
                String rolesNormalizados = normalizarTexto(unidad.roles()).toLowerCase(Locale.ROOT);
                boolean esHeroe = rolesNormalizados.contains("hero");
                categorias.get(categoria).unidades().add(new UnidadCatalogoView(
                        valorSeguroVista(unidad.nombre()),
                        valorSeguroVista(unidad.roles()),
                        valorSeguroVista(unidad.puntos()),
                        obtenerPuntosMinimos(unidad.puntos()),
                        categoria,
                        valorSeguroVista(unidad.armas()),
                        "[]",
                        valorSeguroVista(unidad.habilidades()),
                        valorSeguroVista(unidad.palabrasClaveFaccion()),
                        valorSeguroVista(unidad.palabrasClave()),
                        serializarConfiguracionUnidadAos(unidad.gruposMiniaturas(), unidad.opcionesComposicion()),
                        rolesNormalizados.contains("legends"),
                        rolesNormalizados.contains("faction terrain"),
                        false,
                        rolesNormalizados.contains("battleline"),
                        rolesNormalizados.contains("unique"),
                        esHeroe,
                        esHeroe,
                        false,
                        ""
                ));
            }
        }

        return new CreadorLista40kView(
                normalizarTexto(formatoJuego),
                normalizarTexto(nombreLista),
                normalizarTexto(faccion),
                normalizarTexto(ejercito),
                limitePuntos == null || limitePuntos <= 0 ? LIMITE_PUNTOS_POR_DEFECTO : limitePuntos,
                List.copyOf(categorias.values()),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public MisListasView prepararMisListasView(
            String nombreUsuario,
            List<ListaGuardadaView> listasGuardadas,
            String formatoJuegoSeleccionado
    ) {
        String formatoSeleccionadoNormalizado = normalizarFiltroFormato(formatoJuegoSeleccionado);
        List<ListaResumenView> listas = new ArrayList<>();
        for (ListaGuardadaView lista : listasGuardadas) {
            if (!"TODOS".equals(formatoSeleccionadoNormalizado)
                    && !formatoSeleccionadoNormalizado.equals(valorSeguroVista(lista.formatoJuego()))) {
                continue;
            }
            listas.add(new ListaResumenView(
                    lista.listaId(),
                    valorSeguroVista(lista.formatoJuego()),
                    valorSeguroVista(lista.nombreLista()),
                    obtenerNombreFormatoJuego(lista.formatoJuego()),
                    textoMostrable(lista.faccion(), "Sin faccion"),
                    textoMostrable(lista.ejercito(), "Sin ejercito"),
                    (lista.puntosActuales() == null ? 0 : lista.puntosActuales()) + " / "
                            + (lista.limitePuntos() == null ? LIMITE_PUNTOS_POR_DEFECTO : lista.limitePuntos()),
                    lista.numeroVersion() == null ? 0 : lista.numeroVersion()
            ));
        }

        return new MisListasView(
                valorSeguroVista(nombreUsuario),
                formatoSeleccionadoNormalizado,
                List.of(
                        new FormatoFiltroView("TODOS", "Todos los juegos"),
                        new FormatoFiltroView(FORMATO_40K, "Warhammer 40,000"),
                        new FormatoFiltroView(FORMATO_AOS, "Age of Sigmar")
                ),
                List.copyOf(listas)
        );
    }

    public DetalleLista40kView prepararDetalleLista40kView(ListaGuardadaView lista) {
        if (lista == null) {
            return null;
        }

        List<UnidadListaDetalleView> unidades = new ArrayList<>();
        for (UnidadGuardadaView unidad : lista.unidades()) {
            unidades.add(new UnidadListaDetalleView(
                    valorSeguroVista(unidad.nombreUnidad()),
                    textoMostrable(unidad.roles(), "Sin rol"),
                    unidad.puntosBase() == null ? 0 : unidad.puntosBase(),
                    textoMostrable(unidad.categoria(), "Sin categoria")
            ));
        }

        return new DetalleLista40kView(
                lista.listaId(),
                valorSeguroVista(lista.nombreLista()),
                valorSeguroVista(lista.faccion()),
                valorSeguroVista(lista.ejercito()),
                (lista.puntosActuales() == null ? 0 : lista.puntosActuales()) + " / "
                        + (lista.limitePuntos() == null ? LIMITE_PUNTOS_POR_DEFECTO : lista.limitePuntos()),
                lista.numeroVersion() == null ? 0 : lista.numeroVersion(),
                List.copyOf(unidades)
        );
    }

    @Transactional(readOnly = true)
    public java.util.List<ListaGuardadaView> obtenerListasGuardadas(String nombreUsuarioSesion) {
        if (nombreUsuarioSesion == null || nombreUsuarioSesion.isBlank()) {
            throw new IllegalArgumentException("No hay un usuario autenticado.");
        }

        java.util.List<ListaEjercitoConsultaRepository.ListaGuardadaProjection> listas =
                listaEjercitoConsultaRepository.buscarListasActualesPorUsuario(nombreUsuarioSesion);
        java.util.List<ListaEjercitoConsultaRepository.UnidadListaGuardadaProjection> unidades =
                listaEjercitoConsultaRepository.buscarUnidadesDeListasActualesPorUsuario(nombreUsuarioSesion);

        java.util.Map<Long, java.util.List<UnidadGuardadaView>> unidadesPorLista = new java.util.LinkedHashMap<>();
        for (ListaEjercitoConsultaRepository.UnidadListaGuardadaProjection unidad : unidades) {
            unidadesPorLista
                    .computeIfAbsent(unidad.getListaId(), clave -> new java.util.ArrayList<>())
                    .add(new UnidadGuardadaView(
                            valorSeguroVista(unidad.getNombreUnidad()),
                            valorSeguroVista(unidad.getRoles()),
                            unidad.getPuntosBase() == null ? 0 : unidad.getPuntosBase(),
                            valorSeguroVista(unidad.getCategoria())
                    ));
        }

        java.util.List<ListaGuardadaView> resultado = new java.util.ArrayList<>();
        for (ListaEjercitoConsultaRepository.ListaGuardadaProjection lista : listas) {
            resultado.add(new ListaGuardadaView(
                    lista.getListaId(),
                    valorSeguroVista(lista.getNombreLista()),
                    valorSeguroVista(lista.getFormatoJuego()),
                    valorSeguroVista(lista.getFaccion()),
                    valorSeguroVista(lista.getEjercito()),
                    lista.getPuntosActuales() == null ? 0 : lista.getPuntosActuales(),
                    lista.getLimitePuntos() == null ? LIMITE_PUNTOS_POR_DEFECTO : lista.getLimitePuntos(),
                    lista.getNumeroVersion() == null ? 0 : lista.getNumeroVersion(),
                    unidadesPorLista.getOrDefault(lista.getListaId(), java.util.List.of())
            ));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public ListaGuardadaView obtenerListaGuardadaPorId(String nombreUsuarioSesion, Long listaId) {
        if (listaId == null) {
            throw new IllegalArgumentException("Debes indicar una lista.");
        }

        return obtenerListasGuardadas(nombreUsuarioSesion).stream()
                .filter(lista -> listaId.equals(lista.listaId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la lista solicitada."));
    }

    @Transactional(readOnly = true)
    public ListaExportacionView obtenerListaParaExportar(String nombreUsuarioSesion, Long listaId) {
        if (nombreUsuarioSesion == null || nombreUsuarioSesion.isBlank()) {
            throw new IllegalArgumentException("No hay un usuario autenticado.");
        }
        if (listaId == null) {
            throw new IllegalArgumentException("Debes indicar una lista.");
        }

        ListaEjercitoConsultaRepository.ListaExportacionProjection lista =
                listaEjercitoConsultaRepository.buscarListaActualParaExportar(nombreUsuarioSesion, listaId)
                        .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la lista solicitada."));

        return new ListaExportacionView(
                lista.getListaId(),
                valorSeguroVista(lista.getNombreLista()),
                valorSeguroVista(lista.getFormatoJuego()),
                valorSeguroVista(lista.getFaccion()),
                valorSeguroVista(lista.getEjercito()),
                lista.getPuntosActuales() == null ? 0 : lista.getPuntosActuales(),
                lista.getLimitePuntos() == null ? LIMITE_PUNTOS_POR_DEFECTO : lista.getLimitePuntos(),
                lista.getNumeroVersion() == null ? 0 : lista.getNumeroVersion(),
                valorSeguroVista(lista.getDatosListaJson())
        );
    }

    private String valorSeguroVista(String texto) {
        return texto == null ? "" : texto;
    }

    private String textoMostrable(String texto, String valorPorDefecto) {
        return texto == null || texto.isBlank() ? valorPorDefecto : texto;
    }

    private String normalizarFiltroFormato(String formatoJuego) {
        String valor = valorSeguroVista(formatoJuego);
        if (FORMATO_40K.equals(valor) || FORMATO_AOS.equals(valor)) {
            return valor;
        }
        return "TODOS";
    }

    private String obtenerNombreFormatoJuego(String formatoJuego) {
        if (FORMATO_AOS.equals(formatoJuego)) {
            return "Age of Sigmar";
        }
        if (FORMATO_40K.equals(formatoJuego)) {
            return "Warhammer 40,000";
        }
        return valorSeguroVista(formatoJuego);
    }

    public record ListaGuardadaView(
            Long listaId,
            String nombreLista,
            String formatoJuego,
            String faccion,
            String ejercito,
            Integer puntosActuales,
            Integer limitePuntos,
            Integer numeroVersion,
            java.util.List<UnidadGuardadaView> unidades
    ) {
    }

    public record UnidadGuardadaView(
            String nombreUnidad,
            String roles,
            Integer puntosBase,
            String categoria
    ) {
    }

    public record MisListasView(
            String nombreUsuario,
            String formatoJuegoSeleccionado,
            List<FormatoFiltroView> formatosDisponibles,
            List<ListaResumenView> listas
    ) {
    }

    public record FormatoFiltroView(String codigo, String nombre) {
    }

    public record ListaResumenView(
            Long listaId,
            String codigoFormatoJuego,
            String nombreLista,
            String formatoJuego,
            String faccion,
            String ejercito,
            String puntos,
            Integer numeroVersion
    ) {
    }

    public record ListaExportacionView(
            Long listaId,
            String nombreLista,
            String formatoJuego,
            String faccion,
            String ejercito,
            Integer puntosActuales,
            Integer limitePuntos,
            Integer numeroVersion,
            String datosListaJson
    ) {
    }

    public record DetalleLista40kView(
            Long listaId,
            String nombreLista,
            String faccion,
            String ejercito,
            String puntos,
            Integer numeroVersion,
            List<UnidadListaDetalleView> unidades
    ) {
    }

    public record UnidadListaDetalleView(
            String nombreUnidad,
            String roles,
            Integer puntosBase,
            String categoria
    ) {
    }
}
