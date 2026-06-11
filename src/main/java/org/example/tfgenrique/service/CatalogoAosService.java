package org.example.tfgenrique.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class CatalogoAosService {
    // ZIP publicado por BSData con los catalogos de Age of Sigmar 4a edicion.
    private static final String URL_CATALOGO = "https://codeload.github.com/BSData/age-of-sigmar-4th/zip/refs/heads/main";

    // Estos nombres se usan para distinguir "roles" de unidad del resto de category links.
    private static final Set<String> NOMBRES_ROL_UNIDAD = Set.of(
            "Hero", "Wizard", "Priest", "Infantry", "Cavalry", "Monster", "War Machine",
            "Behemoth", "Faction Terrain", "Unique", "Companion", "Fight", "Fly", "Champion", "Battleline"
    );

    // Cliente HTTP reutilizable para descargar el ZIP remoto.
    private final HttpClient clienteHttp = HttpClient.newHttpClient();

    // Ultima version del catalogo cargada en memoria. Sirve como cache simple.
    private Catalogo40kData datos;

    public synchronized Catalogo40kData actualizarCatalogo() {
        try {
            // Descarga el ZIP completo con todos los .cat del repositorio.
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(URL_CATALOGO)).GET().build();
            HttpResponse<InputStream> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofInputStream());
            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
                throw new IllegalStateException("GitHub respondio con estado " + respuesta.statusCode());
            }

            // Aqui iremos guardando cada .cat parseado a una estructura intermedia.
            List<ArchivoCatalogo> archivosCatalogo = new ArrayList<>();

            // El resultado final se organiza como:
            // faccion -> (nombreEjercito -> ejercito)
            Map<String, Map<String, Ejercito40k>> catalogoPorFaccion = new TreeMap<>();

            // El ZIP contiene muchos .cat; cada uno representa un catalogo o una libreria auxiliar.
            try (ZipInputStream flujoZip = new ZipInputStream(respuesta.body())) {
                ZipEntry entradaZip;
                while ((entradaZip = flujoZip.getNextEntry()) != null) {
                    if (!entradaZip.isDirectory() && entradaZip.getName().endsWith(".cat")) {
                        // readAllBytes consume solo la entrada actual del ZIP.
                        ArchivoCatalogo archivoCatalogo = leerArchivoCatalogo(new ByteArrayInputStream(flujoZip.readAllBytes()));
                        if (archivoCatalogo != null) {
                            archivosCatalogo.add(archivoCatalogo);
                        }
                    }
                    flujoZip.closeEntry();
                }
            }

            // Primer indice: id de unidad raiz -> nodo XML de esa unidad.
            Map<String, Element> unidadesRaizPorId = new HashMap<>();
            Map<String, Element> selectionEntriesPorId = new HashMap<>();
            Map<String, Element> selectionEntryGroupsPorId = new HashMap<>();

            // Segundo indice: id de catalogo -> archivoCatalogo.
            // Hace falta para resolver imports entre catalogos.
            Map<String, ArchivoCatalogo> archivosCatalogoPorId = new HashMap<>();
            for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
                archivosCatalogoPorId.put(archivoCatalogo.id(), archivoCatalogo);
                for (Element unidadRaiz : archivoCatalogo.unidadesRaiz()) {
                    unidadesRaizPorId.put(unidadRaiz.getAttribute("id"), unidadRaiz);
                }
                selectionEntriesPorId.putAll(archivoCatalogo.selectionEntriesPorId());
                selectionEntryGroupsPorId.putAll(archivoCatalogo.selectionEntryGroupsPorId());
            }

            // Una vez indexado todo, se compone cada ejercito resolviendo sus imports y enlaces.
            for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
                Ejercito40k ejercito = crearEjercito(
                        archivoCatalogo,
                        unidadesRaizPorId,
                        archivosCatalogoPorId,
                        selectionEntriesPorId,
                        selectionEntryGroupsPorId
                );
                if (ejercito != null) {
                    catalogoPorFaccion
                            .computeIfAbsent(ejercito.faccion(), clave -> new TreeMap<>())
                            .put(ejercito.nombre(), ejercito);
                }
            }

            // Se guarda fecha/hora para saber cuando se refresco el cache.
            datos = new Catalogo40kData(catalogoPorFaccion, LocalDateTime.now());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo actualizar el catalogo de Age of Sigmar", ex);
        }
        return datos;
    }

    public Catalogo40kData getData() {
        // Devuelve el ultimo catalogo cargado, aunque la descarga mas reciente haya fallado.
        return datos;
    }

    public MenuPrincipalView prepararMenuPrincipal(
            String nombreUsuario,
            Catalogo40kData catalogo,
            String errorCatalogo
    ) {
        List<FaccionMenuView> facciones = new ArrayList<>();
        Map<String, Map<String, Ejercito40k>> faccionesCatalogo = catalogo == null ? Map.of() : catalogo.facciones();
        for (Map.Entry<String, Map<String, Ejercito40k>> entrada : faccionesCatalogo.entrySet()) {
            facciones.add(new FaccionMenuView(
                    valorSeguro(entrada.getKey()),
                    new ArrayList<>(entrada.getValue().keySet())
            ));
        }

        return new MenuPrincipalView(valorSeguro(nombreUsuario), valorSeguro(errorCatalogo), List.copyOf(facciones));
    }

    public Catalogo40kPaginaView prepararPaginaCatalogo(
            Catalogo40kData catalogo,
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            String errorCatalogo
    ) {
        Map<String, Map<String, Ejercito40k>> faccionesCatalogo = catalogo == null ? Map.of() : catalogo.facciones();
        List<FaccionCatalogoView> facciones = new ArrayList<>();
        List<EjercitoOpcionView> ejercitosDisponibles = new ArrayList<>();

        for (Map.Entry<String, Map<String, Ejercito40k>> entradaFaccion : faccionesCatalogo.entrySet()) {
            String nombreFaccion = valorSeguro(entradaFaccion.getKey());
            facciones.add(new FaccionCatalogoView(nombreFaccion));

            if (nombreFaccion.equals(valorSeguro(faccionSeleccionada))) {
                for (String nombreEjercito : entradaFaccion.getValue().keySet()) {
                    ejercitosDisponibles.add(new EjercitoOpcionView(valorSeguro(nombreEjercito)));
                }
            }
        }

        Ejercito40k ejercito = catalogo == null ? null : catalogo.buscarEjercito(faccionSeleccionada, ejercitoSeleccionado);
        EjercitoCatalogoDetalleView detalle = null;
        if (ejercito != null) {
            List<UnidadCatalogoResumenView> unidades = new ArrayList<>();
            for (Unidad40k unidad : ejercito.unidades()) {
                unidades.add(new UnidadCatalogoResumenView(valorSeguro(unidad.nombre())));
            }
            detalle = new EjercitoCatalogoDetalleView(
                    valorSeguro(ejercito.faccion()),
                    valorSeguro(ejercito.nombre()),
                    unidades.size(),
                    List.copyOf(unidades)
            );
        }

        return new Catalogo40kPaginaView(
                valorSeguro(errorCatalogo),
                valorSeguro(faccionSeleccionada),
                valorSeguro(ejercitoSeleccionado),
                List.copyOf(facciones),
                List.copyOf(ejercitosDisponibles),
                detalle
        );
    }

    public InfoUnidad40kView prepararInfoUnidad(
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            Unidad40k unidad
    ) {
        if (unidad == null) {
            return null;
        }

        List<EstadisticaUnidadView> estadisticas = new ArrayList<>();
        for (Estadistica40k estadistica : unidad.estadisticas()) {
            estadisticas.add(new EstadisticaUnidadView(
                    valorSeguro(estadistica.nombre()),
                    valorSeguro(estadistica.valor())
            ));
        }

        List<HabilidadUnidadView> habilidades = new ArrayList<>();
        for (Habilidad40k habilidad : unidad.habilidadesDetalle()) {
            habilidades.add(new HabilidadUnidadView(
                    valorSeguro(habilidad.nombre()),
                    valorSeguroONulo(habilidad.descripcion()).isBlank() ? "Sin descripcion" : habilidad.descripcion().trim()
            ));
        }

        String perfiles = valorSeguroONulo(unidad.perfiles()).isBlank() ? "Sin equipamiento registrado" : unidad.perfiles().trim();
        String armas = valorSeguroONulo(unidad.armas()).isBlank() ? "Sin armas registradas" : unidad.armas().trim();

        return new InfoUnidad40kView(
                valorSeguro(faccionSeleccionada),
                valorSeguro(ejercitoSeleccionado),
                valorSeguro(unidad.nombre()),
                perfiles,
                armas,
                List.copyOf(estadisticas),
                List.copyOf(habilidades)
        );
    }

    private ArchivoCatalogo leerArchivoCatalogo(InputStream flujoEntrada) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        // BattleScribe usa XML con namespaces; localName funciona bien con esto activado.
        fabrica.setNamespaceAware(true);
        // Bloquea DOCTYPE para evitar problemas de seguridad al parsear XML externo.
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        Document documento = fabrica.newDocumentBuilder()
                .parse(new InputSource(new InputStreamReader(flujoEntrada, StandardCharsets.UTF_8)));

        // Cada .cat valido deberia tener como raiz "catalogue".
        Element catalogo = documento.getDocumentElement();
        if (!"catalogue".equals(catalogo.getLocalName())) {
            return null;
        }

        // Sin nombre no tiene sentido mostrarlo ni indexarlo como ejercito/catalogo.
        String nombreCatalogo = catalogo.getAttribute("name");
        if (nombreCatalogo == null || nombreCatalogo.isBlank()) {
            return null;
        }

        // Este record no es todavia el resultado final. Solo recoge lo necesario
        // para luego montar ejercitos resolviendo imports y enlaces.
        return new ArchivoCatalogo(
                catalogo.getAttribute("id"),
                nombreCatalogo,
                Boolean.parseBoolean(catalogo.getAttribute("library")),
                obtenerUnidadesRaiz(catalogo),
                obtenerEnlacesRaiz(catalogo),
                obtenerIdsCatalogosImportados(catalogo),
                indexarElementosPorId(catalogo, "selectionEntry"),
                indexarElementosPorId(catalogo, "selectionEntryGroup")
        );
    }

    private Ejercito40k crearEjercito(
            ArchivoCatalogo archivoCatalogo,
            Map<String, Element> unidadesRaizPorId,
            Map<String, ArchivoCatalogo> archivosCatalogoPorId,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        // Las librerias son catalogos auxiliares y no deben salir como ejercitos seleccionables.
        if (!esCatalogoSeleccionable(archivoCatalogo)) {
            return null;
        }

        // En AoS el catalogo principal suele ser "Cities of Sigmar.cat" y los subcatalogos
        // usan "Faccion - Variante". Si no hay separador, se usa el mismo nombre como faccion y ejercito.
        String[] partesNombre = archivoCatalogo.nombre().split(" - ", 2);
        String faccion = partesNombre[0].trim();
        String nombreEjercito = partesNombre.length == 2 ? partesNombre[1].trim() : archivoCatalogo.nombre().trim();

        // LinkedHashMap mantiene orden de insercion y permite deduplicar por id.
        Map<String, Element> elementosUnidad = new LinkedHashMap<>();
        agregarUnidadesDeCatalogo(
                archivoCatalogo,
                unidadesRaizPorId,
                archivosCatalogoPorId,
                elementosUnidad,
                new LinkedHashSet<>()
        );

        // Una vez reunidos los nodos XML de unidad, se transforman a objetos de vista.
        List<Unidad40k> unidades = elementosUnidad.values().stream()
                .map(unidad -> leerUnidad(unidad, selectionEntriesPorId, selectionEntryGroupsPorId))
                .sorted(Comparator.comparing(Unidad40k::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new Ejercito40k(faccion, nombreEjercito, unidades);
    }

    private void agregarUnidadesDeCatalogo(
            ArchivoCatalogo archivoCatalogo,
            Map<String, Element> unidadesRaizPorId,
            Map<String, ArchivoCatalogo> archivosCatalogoPorId,
            Map<String, Element> elementosUnidad,
            Set<String> idsCatalogoVisitados
    ) {
        // Evita ciclos cuando varios catalogos se importan entre si.
        if (!idsCatalogoVisitados.add(archivoCatalogo.id())) {
            return;
        }

        // Primero añade las unidades declaradas directamente dentro del catalogo actual.
        for (Element unidadRaiz : archivoCatalogo.unidadesRaiz()) {
            if (esUnidadVisible(unidadRaiz)) {
                elementosUnidad.put(unidadRaiz.getAttribute("id"), unidadRaiz);
            }
        }

        // Luego resuelve entryLinks, que son referencias a unidades definidas en otro sitio.
        for (Element enlaceUnidad : archivoCatalogo.enlacesRaiz()) {
            Element unidadDestino = unidadesRaizPorId.get(enlaceUnidad.getAttribute("targetId"));
            if (unidadDestino != null && esUnidadVisible(unidadDestino)) {
                elementosUnidad.put(unidadDestino.getAttribute("id"), unidadDestino);
            }
        }

        // Por ultimo entra recursivamente en los catalogos importados.
        for (String idCatalogoImportado : archivoCatalogo.idsCatalogosImportados()) {
            ArchivoCatalogo catalogoImportado = archivosCatalogoPorId.get(idCatalogoImportado);
            if (catalogoImportado != null) {
                agregarUnidadesDeCatalogo(
                        catalogoImportado,
                        unidadesRaizPorId,
                        archivosCatalogoPorId,
                        elementosUnidad,
                        idsCatalogoVisitados
                );
            }
        }
    }

    private boolean esCatalogoSeleccionable(ArchivoCatalogo archivoCatalogo) {
        String nombre = archivoCatalogo.nombre();
        return !archivoCatalogo.esLibreria()
                && !nombre.endsWith(" Library")
                && !nombre.contains(" - Library")
                && !nombre.startsWith("Library -")
                && !"Lores".equals(nombre)
                && !"Regiments of Renown".equals(nombre)
                && !nombre.startsWith("Path to Glory");
    }

    private boolean esUnidadVisible(Element entrada) {
        // Solo nos interesan unidades/modelos visibles para mostrarlos en la tabla.
        String tipo = entrada.getAttribute("type");
        return ("unit".equals(tipo) || "model".equals(tipo))
                && !Boolean.parseBoolean(entrada.getAttribute("hidden"));
    }

    private List<Element> obtenerUnidadesRaiz(Element catalogo) {
        List<Element> unidades = new ArrayList<>();

        // BattleScribe puede definir entradas propias y compartidas.
        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries")) {
            Element contenedor = primerHijoDirecto(catalogo, nombreContenedor);
            if (contenedor != null) {
                for (Element entrada : hijosDirectos(contenedor, "selectionEntry")) {
                    if (esUnidadVisible(entrada)) {
                        unidades.add(entrada);
                    }
                }
            }
        }
        return unidades;
    }

    private List<Element> obtenerEnlacesRaiz(Element catalogo) {
        // entryLinks son accesos indirectos a selectionEntries ya definidas en otro catalogo.
        Element contenedor = primerHijoDirecto(catalogo, "entryLinks");
        if (contenedor == null) {
            return List.of();
        }

        return hijosDirectos(contenedor, "entryLink").stream()
                .filter(enlace -> "selectionEntry".equals(enlace.getAttribute("type")))
                .filter(enlace -> !Boolean.parseBoolean(enlace.getAttribute("hidden")))
                .toList();
    }

    private List<String> obtenerIdsCatalogosImportados(Element catalogo) {
        // Solo se importan los catalogos que piden traer sus entradas raiz.
        Element contenedor = primerHijoDirecto(catalogo, "catalogueLinks");
        if (contenedor == null) {
            return List.of();
        }

        return hijosDirectos(contenedor, "catalogueLink").stream()
                .filter(enlaceCatalogo -> "true".equals(enlaceCatalogo.getAttribute("importRootEntries")))
                .map(enlaceCatalogo -> enlaceCatalogo.getAttribute("targetId"))
                .filter(idDestino -> idDestino != null && !idDestino.isBlank())
                .toList();
    }

    private Unidad40k leerUnidad(
            Element entradaUnidad,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        // Una unidad puede tener varios costes repetidos o variantes.
        // Aqui se recogen los puntos positivos, sin duplicados, y ordenados.
        List<Integer> puntos = leerPuntosUnidad(entradaUnidad);

        Set<String> roles = new LinkedHashSet<>();
        Set<String> palabrasClave = new LinkedHashSet<>();
        Set<String> palabrasClaveFaccion = new LinkedHashSet<>();

        // categoryLink mezcla roles, keywords normales y faction keywords.
        // Aqui se separan en tres grupos para mostrarlos por columnas.
        for (Element categoria : obtenerElementosDescendientes(entradaUnidad, "categoryLink")) {
            String nombre = categoria.getAttribute("name");
            if (nombre == null || nombre.isBlank()) {
                continue;
            }

            if (nombre.startsWith("Faction:")) {
                palabrasClaveFaccion.add(nombre.substring("Faction:".length()).trim());
            } else if (NOMBRES_ROL_UNIDAD.contains(nombre)) {
                roles.add(nombre);
            } else {
                palabrasClave.add(nombre);
            }
        }

        Set<String> perfilesUnidad = new LinkedHashSet<>();
        Set<String> nombresHabilidades = new LinkedHashSet<>();
        Set<String> armas = new LinkedHashSet<>();
        List<Estadistica40k> estadisticas = new ArrayList<>();
        List<Habilidad40k> habilidades = new ArrayList<>();
        List<OpcionComposicion40k> opcionesComposicion = leerOpcionesComposicion(
                entradaUnidad,
                puntos,
                selectionEntriesPorId,
                selectionEntryGroupsPorId
        );
        List<GrupoMiniaturas40k> gruposMiniaturas = opcionesComposicion.isEmpty() ? leerGruposMiniaturas(
                entradaUnidad,
                selectionEntriesPorId,
                selectionEntryGroupsPorId
        ) : List.of();

        // Los perfiles contienen tanto estadisticas de unidad como habilidades y armas.
        for (Element perfil : obtenerElementosDescendientes(entradaUnidad, "profile")) {
            String tipoPerfil = perfil.getAttribute("typeName");
            String nombrePerfil = perfil.getAttribute("name");
            if (nombrePerfil == null || nombrePerfil.isBlank()) {
                continue;
            }

            if ("Unit".equalsIgnoreCase(tipoPerfil)) {
                perfilesUnidad.add(nombrePerfil + " " + leerCaracteristicas(perfil));
                if (estadisticas.isEmpty()) {
                    estadisticas = leerEstadisticas(perfil);
                }
            } else if ("Abilities".equalsIgnoreCase(tipoPerfil)) {
                nombresHabilidades.add(nombrePerfil);
                habilidades.add(new Habilidad40k(nombrePerfil, leerDescripcionPerfil(perfil)));
            } else if (tipoPerfil != null && tipoPerfil.toLowerCase(Locale.ROOT).contains("weapons")) {
                armas.add(nombrePerfil);
            }
        }

        // El record final ya contiene solo texto listo para pintar en la JSP.
        return new Unidad40k(
                entradaUnidad.getAttribute("name"),
                puntos.isEmpty() ? "Sin coste directo" : unirEnteros(puntos),
                String.join(", ", roles),
                unirValoresLimitados(palabrasClaveFaccion, 12),
                unirValoresLimitados(palabrasClave, 12),
                unirValoresLimitados(perfilesUnidad, 4),
                unirValoresLimitados(nombresHabilidades, 8),
                unirValoresLimitados(armas, 10),
                estadisticas,
                habilidades,
                gruposMiniaturas,
                opcionesComposicion
        );
    }

    private List<OpcionComposicion40k> leerOpcionesComposicion(
            Element entradaUnidad,
            List<Integer> puntosUnidad,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        List<OpcionComposicion40k> opciones = new ArrayList<>();
        List<Element> gruposDirectos = new ArrayList<>();
        Element contenedorGrupos = primerHijoDirecto(entradaUnidad, "selectionEntryGroups");
        if (contenedorGrupos != null) {
            gruposDirectos.addAll(hijosDirectos(contenedorGrupos, "selectionEntryGroup"));
        }

        for (Element grupo : gruposDirectos) {
            if (!esGrupoComposicion(grupo)) {
                continue;
            }

            String idSeleccionPorDefecto = leerIdSeleccionPorDefecto(grupo, grupo);
            for (Element entrada : obtenerSelectionEntriesContenidas(grupo)) {
                GrupoMiniaturas40k grupoPrincipal = leerGrupoJerarquico(
                        entrada,
                        entrada,
                        selectionEntriesPorId,
                        selectionEntryGroupsPorId,
                        new LinkedHashSet<>()
                );
                if (grupoPrincipal == null) {
                    continue;
                }

                int puntos = puntosUnidad.isEmpty() ? 0 : puntosUnidad.get(Math.min(opciones.size(), puntosUnidad.size() - 1));
                opciones.add(new OpcionComposicion40k(
                        valorSeguroONulo(entrada.getAttribute("id")),
                        valorSeguroONulo(entrada.getAttribute("name")),
                        puntos,
                        valorSeguroONulo(entrada.getAttribute("id")).equals(idSeleccionPorDefecto),
                        List.of(grupoPrincipal)
                ));
            }
        }

        return opciones;
    }

    private List<Integer> leerPuntosUnidad(Element entradaUnidad) {
        List<Element> costesPuntos = obtenerElementosDescendientes(entradaUnidad, "cost").stream()
                .filter(coste -> "pts".equalsIgnoreCase(coste.getAttribute("name")))
                .toList();

        Set<String> fieldsPuntos = new LinkedHashSet<>();
        List<Integer> puntos = new ArrayList<>();
        for (Element coste : costesPuntos) {
            String typeId = valorSeguroONulo(coste.getAttribute("typeId"));
            if (!typeId.isBlank()) {
                fieldsPuntos.add(typeId);
            }
            Integer valor = parsearEntero(coste.getAttribute("value"));
            if (valor != null && valor > 0) {
                puntos.add(valor);
            }
        }

        Element contenedorModificadores = primerHijoDirecto(entradaUnidad, "modifiers");
        if (contenedorModificadores != null) {
            for (Element modificador : hijosDirectos(contenedorModificadores, "modifier")) {
                if (!fieldsPuntos.contains(modificador.getAttribute("field"))) {
                    continue;
                }
                if (!"set".equals(modificador.getAttribute("type"))) {
                    continue;
                }

                Integer valor = parsearEntero(modificador.getAttribute("value"));
                if (valor != null && valor > 0) {
                    puntos.add(valor);
                }
            }
        }

        return puntos.stream()
                .distinct()
                .sorted()
                .toList();
    }

    private List<GrupoMiniaturas40k> leerGruposMiniaturas(
            Element entradaUnidad,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        List<GrupoMiniaturas40k> grupos = new ArrayList<>();
        for (GroupNode grupo : obtenerGruposDirectos(entradaUnidad, selectionEntryGroupsPorId)) {
            GrupoMiniaturas40k grupoMiniaturas = leerGrupoJerarquico(
                    grupo.definicion(),
                    grupo.origen(),
                    selectionEntriesPorId,
                    selectionEntryGroupsPorId,
                    new LinkedHashSet<>()
            );
            if (grupoMiniaturas != null) {
                grupos.add(grupoMiniaturas);
            }
        }

        List<ModelNode> modelosDirectos = obtenerModelosDirectos(entradaUnidad, selectionEntriesPorId);
        if (grupos.isEmpty() && !modelosDirectos.isEmpty()) {
            grupos.add(crearGrupoMiniaturasSintetico(entradaUnidad, modelosDirectos, selectionEntriesPorId, selectionEntryGroupsPorId));
        }

        return grupos;
    }

    private GrupoMiniaturas40k leerGrupoJerarquico(
            Element elemento,
            Element origen,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId,
            Set<String> idsVisitados
    ) {
        String idElemento = valorSeguroONulo(elemento.getAttribute("id"));
        if (!idElemento.isBlank() && !idsVisitados.add("miniaturas:" + idElemento)) {
            return null;
        }

        List<ModelNode> modelosDirectos = obtenerModelosDirectos(elemento, selectionEntriesPorId);
        List<GrupoMiniaturas40k> subgrupos = new ArrayList<>();

        for (GroupNode grupo : obtenerGruposDirectos(elemento, selectionEntryGroupsPorId)) {
            GrupoMiniaturas40k subgrupo = leerGrupoJerarquico(
                    grupo.definicion(),
                    grupo.origen(),
                    selectionEntriesPorId,
                    selectionEntryGroupsPorId,
                    idsVisitados
            );
            if (subgrupo != null) {
                subgrupos.add(subgrupo);
            }
        }

        for (Element entrada : obtenerSelectionEntriesContenidas(elemento)) {
            GrupoMiniaturas40k subgrupo = leerGrupoJerarquico(
                    entrada,
                    entrada,
                    selectionEntriesPorId,
                    selectionEntryGroupsPorId,
                    idsVisitados
            );
            if (subgrupo != null) {
                subgrupos.add(subgrupo);
            }
        }

        if (modelosDirectos.isEmpty() && subgrupos.isEmpty()) {
            return null;
        }

        return crearGrupoMiniaturas(origen, elemento, modelosDirectos, subgrupos, selectionEntriesPorId, selectionEntryGroupsPorId);
    }

    private GrupoMiniaturas40k crearGrupoMiniaturas(
            Element origen,
            Element grupo,
            List<ModelNode> modelosDirectos,
            List<GrupoMiniaturas40k> subgrupos,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        List<ModeloUnidad40k> modelos = modelosDirectos.stream()
                .map(modelo -> leerModeloUnidad(modelo, selectionEntriesPorId, selectionEntryGroupsPorId))
                .toList();

        int minimo = leerRestriccionNumerica(origen, "min", "parent", "unit");
        if (minimo <= 0) {
            minimo = leerRestriccionNumerica(grupo, "min", "parent", "unit");
        }
        int maximo = leerRestriccionNumerica(origen, "max", "parent", "unit");
        if (maximo <= 0) {
            maximo = leerRestriccionNumerica(grupo, "max", "parent", "unit");
        }
        if (maximo <= 0) {
            maximo = sumarMaximosModelos(modelos) + sumarMaximosSubgrupos(subgrupos);
        }
        if (minimo <= 0) {
            minimo = sumarMinimosModelos(modelos) + sumarMinimosSubgrupos(subgrupos);
        }

        return new GrupoMiniaturas40k(
                valorSeguroONulo(origen.getAttribute("id")).isBlank()
                        ? valorSeguroONulo(grupo.getAttribute("id"))
                        : valorSeguroONulo(origen.getAttribute("id")),
                valorSeguroONulo(origen.getAttribute("name")).isBlank()
                        ? valorSeguroONulo(grupo.getAttribute("name"))
                        : valorSeguroONulo(origen.getAttribute("name")),
                minimo,
                maximo,
                modelos,
                subgrupos
        );
    }

    private GrupoMiniaturas40k crearGrupoMiniaturasSintetico(
            Element entradaUnidad,
            List<ModelNode> modelosDirectos,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        List<ModeloUnidad40k> modelos = modelosDirectos.stream()
                .map(modelo -> leerModeloUnidad(modelo, selectionEntriesPorId, selectionEntryGroupsPorId))
                .toList();

        return new GrupoMiniaturas40k(
                valorSeguroONulo(entradaUnidad.getAttribute("id")) + "-miniaturas",
                "Miniaturas",
                sumarMinimosModelos(modelos),
                sumarMaximosModelos(modelos),
                modelos,
                List.of()
        );
    }

    private int sumarMinimosSubgrupos(List<GrupoMiniaturas40k> subgrupos) {
        int total = 0;
        for (GrupoMiniaturas40k subgrupo : subgrupos) {
            total += Math.max(subgrupo.minimo(), 0);
        }
        return total;
    }

    private int sumarMaximosSubgrupos(List<GrupoMiniaturas40k> subgrupos) {
        int total = 0;
        for (GrupoMiniaturas40k subgrupo : subgrupos) {
            total += Math.max(subgrupo.maximo(), 0);
        }
        return total;
    }

    private int sumarMinimosModelos(List<ModeloUnidad40k> modelos) {
        int total = 0;
        for (ModeloUnidad40k modelo : modelos) {
            total += Math.max(modelo.minimo(), 0);
        }
        return total;
    }

    private int sumarMaximosModelos(List<ModeloUnidad40k> modelos) {
        int total = 0;
        for (ModeloUnidad40k modelo : modelos) {
            total += Math.max(modelo.maximo(), 0);
        }
        return total;
    }

    private List<ModelNode> obtenerModelosDirectos(Element padre, Map<String, Element> selectionEntriesPorId) {
        List<ModelNode> modelos = new ArrayList<>();
        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries")) {
            Element contenedor = primerHijoDirecto(padre, nombreContenedor);
            if (contenedor == null) {
                continue;
            }

            for (Element entrada : hijosDirectos(contenedor, "selectionEntry")) {
                if ("model".equals(entrada.getAttribute("type")) && !Boolean.parseBoolean(entrada.getAttribute("hidden"))) {
                    modelos.add(new ModelNode(entrada, entrada));
                }
            }
        }

        Element contenedorLinks = primerHijoDirecto(padre, "entryLinks");
        if (contenedorLinks != null) {
            for (Element enlace : hijosDirectos(contenedorLinks, "entryLink")) {
                if (Boolean.parseBoolean(enlace.getAttribute("hidden"))) {
                    continue;
                }
                if (!"selectionEntry".equals(enlace.getAttribute("type"))) {
                    continue;
                }

                Element destino = selectionEntriesPorId.get(enlace.getAttribute("targetId"));
                if (destino != null && "model".equals(destino.getAttribute("type")) && !Boolean.parseBoolean(destino.getAttribute("hidden"))) {
                    modelos.add(new ModelNode(destino, enlace));
                }
            }
        }
        return modelos;
    }

    private ModeloUnidad40k leerModeloUnidad(
            ModelNode modelo,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        Element entradaModelo = modelo.definicion();
        Element origenModelo = modelo.origen();

        int minimo = leerRestriccionNumerica(origenModelo, "min", "parent", "unit");
        if (minimo <= 0) {
            minimo = leerRestriccionNumerica(entradaModelo, "min", "parent", "unit");
        }

        int maximo = leerRestriccionNumerica(origenModelo, "max", "parent", "unit");
        if (maximo <= 0) {
            maximo = leerRestriccionNumerica(entradaModelo, "max", "parent", "unit");
        }
        if (maximo <= 0) {
            maximo = Math.max(minimo, 1);
        }

        List<String> equipamientoFijo = new ArrayList<>(leerEquipamientoFijoDirecto(entradaModelo));
        List<GrupoEquipamiento40k> gruposEquipamiento = new ArrayList<>();

        for (GroupNode grupo : obtenerGruposDirectos(entradaModelo, selectionEntryGroupsPorId)) {
            String nombreGrupo = valorSeguroONulo(grupo.origen().getAttribute("name")).isBlank()
                    ? valorSeguroONulo(grupo.definicion().getAttribute("name"))
                    : valorSeguroONulo(grupo.origen().getAttribute("name"));
            if (debeIgnorarGrupoAnidado(nombreGrupo)) {
                continue;
            }

            GrupoEquipamiento40k grupoEquipamiento = leerGrupoEquipamiento(
                    grupo.definicion(),
                    grupo.origen(),
                    selectionEntriesPorId,
                    selectionEntryGroupsPorId
            );
            if (grupoEquipamiento == null || grupoEquipamiento.opciones().isEmpty()) {
                continue;
            }

            if (grupoEquipamiento.opciones().size() == 1
                    && grupoEquipamiento.minimo() == 1
                    && grupoEquipamiento.maximo() == 1) {
                equipamientoFijo.add(grupoEquipamiento.opciones().get(0).nombre());
                continue;
            }

            gruposEquipamiento.add(grupoEquipamiento);
        }

        return new ModeloUnidad40k(
                valorSeguroONulo(origenModelo.getAttribute("id")).isBlank()
                        ? valorSeguroONulo(entradaModelo.getAttribute("id"))
                        : valorSeguroONulo(origenModelo.getAttribute("id")),
                valorSeguroONulo(origenModelo.getAttribute("name")).isBlank()
                        ? valorSeguroONulo(entradaModelo.getAttribute("name"))
                        : valorSeguroONulo(origenModelo.getAttribute("name")),
                minimo,
                maximo,
                List.copyOf(equipamientoFijo),
                List.copyOf(gruposEquipamiento)
        );
    }

    private List<GroupNode> obtenerGruposDirectos(Element padre, Map<String, Element> selectionEntryGroupsPorId) {
        List<GroupNode> grupos = new ArrayList<>();

        Element contenedorGrupos = primerHijoDirecto(padre, "selectionEntryGroups");
        if (contenedorGrupos != null) {
            for (Element grupo : hijosDirectos(contenedorGrupos, "selectionEntryGroup")) {
                if (!Boolean.parseBoolean(grupo.getAttribute("hidden"))) {
                    grupos.add(new GroupNode(grupo, grupo));
                }
            }
        }

        Element contenedorLinks = primerHijoDirecto(padre, "entryLinks");
        if (contenedorLinks != null) {
            for (Element enlace : hijosDirectos(contenedorLinks, "entryLink")) {
                if (Boolean.parseBoolean(enlace.getAttribute("hidden"))) {
                    continue;
                }
                if (!"selectionEntryGroup".equals(enlace.getAttribute("type"))) {
                    continue;
                }

                Element destino = selectionEntryGroupsPorId.get(enlace.getAttribute("targetId"));
                if (destino != null && !Boolean.parseBoolean(destino.getAttribute("hidden"))) {
                    grupos.add(new GroupNode(destino, enlace));
                }
            }
        }

        return grupos;
    }

    private List<Element> obtenerSelectionEntriesContenidas(Element padre) {
        List<Element> entradas = new ArrayList<>();
        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries")) {
            Element contenedor = primerHijoDirecto(padre, nombreContenedor);
            if (contenedor == null) {
                continue;
            }

            for (Element entrada : hijosDirectos(contenedor, "selectionEntry")) {
                if (!Boolean.parseBoolean(entrada.getAttribute("hidden"))) {
                    entradas.add(entrada);
                }
            }
        }
        return entradas;
    }

    private List<String> leerEquipamientoFijoDirecto(Element entrada) {
        List<String> equipamiento = new ArrayList<>();

        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries", "entryLinks")) {
            Element contenedor = primerHijoDirecto(entrada, nombreContenedor);
            if (contenedor == null) {
                continue;
            }

            for (Element hijo : hijosDirectos(contenedor, "selectionEntry")) {
                if (!Boolean.parseBoolean(hijo.getAttribute("hidden"))) {
                    String nombre = valorSeguroONulo(hijo.getAttribute("name"));
                    if (!nombre.isBlank()) {
                        equipamiento.add(nombre);
                    }
                }
            }

            for (Element enlace : hijosDirectos(contenedor, "entryLink")) {
                if (Boolean.parseBoolean(enlace.getAttribute("hidden"))) {
                    continue;
                }
                if ("selectionEntryGroup".equals(enlace.getAttribute("type"))) {
                    continue;
                }

                String nombre = valorSeguroONulo(enlace.getAttribute("name"));
                if (!nombre.isBlank()) {
                    equipamiento.add(nombre);
                }
            }
        }

        return equipamiento.stream()
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .distinct()
                .toList();
    }

    private GrupoEquipamiento40k leerGrupoEquipamiento(
            Element grupo,
            Element origen,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        List<OpcionEquipamiento40k> opciones = new ArrayList<>();
        recopilarOpcionesEquipamiento(grupo, opciones, selectionEntriesPorId, selectionEntryGroupsPorId, new LinkedHashSet<>());
        int minimo = leerRestriccionNumerica(origen, "min", "parent", "unit");
        if (minimo <= 0) {
            minimo = leerRestriccionNumerica(grupo, "min", "parent", "unit");
        }
        int maximo = leerRestriccionNumerica(origen, "max", "parent", "unit");
        if (maximo <= 0) {
            maximo = leerRestriccionNumerica(grupo, "max", "parent", "unit");
        }
        if (maximo <= 0) {
            maximo = opciones.size() <= 1 ? 1 : opciones.size();
        }

        String idSeleccionPorDefecto = leerIdSeleccionPorDefecto(origen, grupo);
        opciones = opciones.stream()
                .filter(opcion -> opcion.nombre() != null && !opcion.nombre().isBlank())
                .distinct()
                .map(opcion -> new OpcionEquipamiento40k(
                        opcion.id(),
                        opcion.nombre(),
                        opcion.id().equals(idSeleccionPorDefecto),
                        opcion.detalleEquipamiento()
                ))
                .toList();

        return new GrupoEquipamiento40k(
                valorSeguroONulo(origen.getAttribute("id")).isBlank()
                        ? valorSeguroONulo(grupo.getAttribute("id"))
                        : valorSeguroONulo(origen.getAttribute("id")),
                valorSeguroONulo(origen.getAttribute("name")).isBlank()
                        ? valorSeguroONulo(grupo.getAttribute("name"))
                        : valorSeguroONulo(origen.getAttribute("name")),
                minimo,
                maximo,
                List.copyOf(opciones)
        );
    }

    private void recopilarOpcionesEquipamiento(
            Element grupo,
            List<OpcionEquipamiento40k> opciones,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId,
            Set<String> idsVisitados
    ) {
        String idGrupo = valorSeguroONulo(grupo.getAttribute("id"));
        if (!idGrupo.isBlank() && !idsVisitados.add("grupo:" + idGrupo)) {
            return;
        }

        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries", "entryLinks")) {
            Element contenedor = primerHijoDirecto(grupo, nombreContenedor);
            if (contenedor == null) {
                continue;
            }

            for (Element entrada : hijosDirectos(contenedor, "selectionEntry")) {
                if (Boolean.parseBoolean(entrada.getAttribute("hidden"))) {
                    continue;
                }
                String nombre = valorSeguroONulo(entrada.getAttribute("name"));
                if (!nombre.isBlank()) {
                    opciones.add(new OpcionEquipamiento40k(
                            valorSeguroONulo(entrada.getAttribute("id")),
                            nombre,
                            false,
                            leerEquipamientoOpcion(entrada)
                    ));
                }
            }

            for (Element enlace : hijosDirectos(contenedor, "entryLink")) {
                if (Boolean.parseBoolean(enlace.getAttribute("hidden"))) {
                    continue;
                }
                if ("selectionEntryGroup".equals(enlace.getAttribute("type"))) {
                    Element grupoResuelto = selectionEntryGroupsPorId.get(enlace.getAttribute("targetId"));
                    String nombreGrupo = valorSeguroONulo(enlace.getAttribute("name"));
                    if (grupoResuelto != null && !debeIgnorarGrupoAnidado(nombreGrupo)) {
                        recopilarOpcionesEquipamiento(
                                grupoResuelto,
                                opciones,
                                selectionEntriesPorId,
                                selectionEntryGroupsPorId,
                                idsVisitados
                        );
                    }
                    continue;
                }

                String nombre = valorSeguroONulo(enlace.getAttribute("name"));
                if (!nombre.isBlank()) {
                    Element entradaResuelta = selectionEntriesPorId.get(enlace.getAttribute("targetId"));
                    opciones.add(new OpcionEquipamiento40k(
                            valorSeguroONulo(enlace.getAttribute("id")),
                            nombre,
                            false,
                            entradaResuelta == null ? List.of() : leerEquipamientoOpcion(entradaResuelta)
                    ));
                }
            }
        }
    }

    private boolean esGrupoComposicion(Element grupo) {
        String nombreGrupo = valorSeguroONulo(grupo.getAttribute("name")).toLowerCase(Locale.ROOT);
        return nombreGrupo.contains("composition");
    }

    private String leerIdSeleccionPorDefecto(Element origen, Element definicion) {
        String id = valorSeguroONulo(origen.getAttribute("defaultSelectionEntryId"));
        if (!id.isBlank()) {
            return id;
        }
        return valorSeguroONulo(definicion.getAttribute("defaultSelectionEntryId"));
    }

    private List<String> leerEquipamientoOpcion(Element entrada) {
        return leerEquipamientoFijoDirecto(entrada).stream()
                .filter(texto -> texto != null && !texto.isBlank())
                .distinct()
                .toList();
    }

    private boolean debeIgnorarGrupoAnidado(String nombreGrupo) {
        String texto = valorSeguroONulo(nombreGrupo).toLowerCase(Locale.ROOT);
        return texto.contains("weapon modifications")
                || texto.contains("weapon upgrades")
                || texto.contains("crusade")
                || texto.contains("battle traits")
                || texto.contains("battle scars")
                || texto.contains("enhancements");
    }

    private Map<String, Element> indexarElementosPorId(Element catalogo, String nombreLocal) {
        Map<String, Element> elementosPorId = new HashMap<>();
        for (Element elemento : obtenerElementosDescendientes(catalogo, nombreLocal)) {
            String id = elemento.getAttribute("id");
            if (id != null && !id.isBlank()) {
                elementosPorId.put(id, elemento);
            }
        }
        return elementosPorId;
    }

    private int leerRestriccionNumerica(Element entrada, String tipo, String... scopesValidos) {
        Element contenedor = primerHijoDirecto(entrada, "constraints");
        if (contenedor == null) {
            return 0;
        }

        for (Element restriccion : hijosDirectos(contenedor, "constraint")) {
            if (!tipo.equals(restriccion.getAttribute("type"))) {
                continue;
            }
            if (!"selections".equals(restriccion.getAttribute("field"))) {
                continue;
            }

            String scope = restriccion.getAttribute("scope");
            boolean scopeValido = false;
            for (String scopeEsperado : scopesValidos) {
                if (scopeEsperado.equals(scope)) {
                    scopeValido = true;
                    break;
                }
            }
            if (!scopeValido) {
                continue;
            }

            Integer valor = parsearEntero(restriccion.getAttribute("value"));
            if (valor != null) {
                return valor;
            }
        }
        return 0;
    }

    private String leerCaracteristicas(Element perfil) {
        // Convierte una lista de characteristics XML en texto tipo:
        // (M: 6", T: 4, Sv: 3+)
        Map<String, String> valores = new LinkedHashMap<>();
        for (Element caracteristica : obtenerElementosDescendientes(perfil, "characteristic")) {
            String nombre = caracteristica.getAttribute("name");
            String valor = caracteristica.getTextContent();
            if (nombre != null && !nombre.isBlank() && valor != null && !valor.isBlank()) {
                valores.put(nombre, valor.trim());
            }
        }

        if (valores.isEmpty()) {
            return "";
        }

        List<String> partes = new ArrayList<>();
        valores.forEach((clave, valor) -> partes.add(clave + ": " + valor));
        return "(" + String.join(", ", partes) + ")";
    }

    private List<Estadistica40k> leerEstadisticas(Element perfil) {
        List<Estadistica40k> estadisticas = new ArrayList<>();
        for (Element caracteristica : obtenerElementosDescendientes(perfil, "characteristic")) {
            String nombre = caracteristica.getAttribute("name");
            String valor = caracteristica.getTextContent();
            if (nombre != null && !nombre.isBlank() && valor != null && !valor.isBlank()) {
                estadisticas.add(new Estadistica40k(nombre.trim(), valor.trim()));
            }
        }
        return estadisticas;
    }

    private String leerDescripcionPerfil(Element perfil) {
        List<String> partes = new ArrayList<>();
        for (Element caracteristica : obtenerElementosDescendientes(perfil, "characteristic")) {
            String nombre = caracteristica.getAttribute("name");
            String valor = caracteristica.getTextContent();
            if (valor == null || valor.isBlank()) {
                continue;
            }

            String texto = valor.trim();
            if (nombre != null && !nombre.isBlank() && !"Ability".equalsIgnoreCase(nombre.trim())) {
                partes.add(nombre.trim() + ": " + texto);
            } else {
                partes.add(texto);
            }
        }
        return String.join(" ", partes);
    }

    private List<Element> obtenerElementosDescendientes(Element padre, String nombreLocal) {
        // Busca cualquier descendiente con ese nombre local, sin depender de una URL concreta de namespace.
        NodeList nodos = padre.getElementsByTagNameNS("*", nombreLocal);
        List<Element> elementos = new ArrayList<>();
        for (int indice = 0; indice < nodos.getLength(); indice++) {
            Node nodo = nodos.item(indice);
            if (nodo instanceof Element elemento) {
                elementos.add(elemento);
            }
        }
        return elementos;
    }

    private Element primerHijoDirecto(Element padre, String nombreLocal) {
        // Util para leer la estructura inmediata del XML sin bajar a nietos/bisnietos.
        for (Element hijo : hijosDirectos(padre, nombreLocal)) {
            return hijo;
        }
        return null;
    }

    private List<Element> hijosDirectos(Element padre, String nombreLocal) {
        // A diferencia de getElementsByTagNameNS, este metodo no recorre todo el subarbol.
        // Solo mira los hijos inmediatos del nodo.
        List<Element> hijos = new ArrayList<>();
        NodeList nodos = padre.getChildNodes();
        for (int indice = 0; indice < nodos.getLength(); indice++) {
            Node nodo = nodos.item(indice);
            if (nodo instanceof Element elemento && nombreLocal.equals(elemento.getLocalName())) {
                hijos.add(elemento);
            }
        }
        return hijos;
    }

    private Integer parsearEntero(String valor) {
        try {
            // Algunos valores vienen como texto decimal; se redondean a entero.
            return Math.round(Float.parseFloat(valor));
        } catch (NumberFormatException ex) {
            // Si el XML trae algo no numerico, se ignora ese valor.
            return null;
        }
    }

    private String unirEnteros(List<Integer> valores) {
        // Une una lista de enteros como "75, 90, 105".
        return valores.stream()
                .map(String::valueOf)
                .reduce((izquierda, derecha) -> izquierda + ", " + derecha)
                .orElse("");
    }

    private String unirValoresLimitados(Set<String> valores, int limite) {
        // Limita el numero de textos mostrados para que la tabla no se dispare en anchura.
        List<String> lista = valores.stream()
                .filter(valor -> valor != null && !valor.isBlank())
                .limit(limite)
                .toList();
        return String.join(", ", lista);
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String valorSeguroONulo(String texto) {
        return texto == null ? "" : texto;
    }

    public record Catalogo40kData(Map<String, Map<String, Ejercito40k>> facciones, LocalDateTime actualizadoEn) {
        public Ejercito40k buscarEjercito(String faccion, String ejercito) {
            // Acceso seguro para la vista: si falta algo, devuelve null en vez de fallar.
            if (faccion == null || ejercito == null) {
                return null;
            }

            Map<String, Ejercito40k> ejercitos = facciones.get(faccion);
            return ejercitos == null ? null : ejercitos.get(ejercito);
        }

        public Unidad40k buscarUnidad(String faccion, String ejercito, String nombreUnidad) {
            Ejercito40k ejercitoEncontrado = buscarEjercito(faccion, ejercito);
            if (ejercitoEncontrado == null || nombreUnidad == null) {
                return null;
            }

            return ejercitoEncontrado.unidades().stream()
                    .filter(unidad -> nombreUnidad.equals(unidad.nombre()))
                    .findFirst()
                    .orElse(null);
        }
    }

    public record Ejercito40k(String faccion, String nombre, List<Unidad40k> unidades) {
    }

    public record MenuPrincipalView(
            String nombreUsuario,
            String errorCatalogo,
            List<FaccionMenuView> facciones
    ) {
    }

    public record FaccionMenuView(
            String nombre,
            List<String> ejercitos
    ) {
    }

    public record Catalogo40kPaginaView(
            String errorCatalogo,
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            List<FaccionCatalogoView> facciones,
            List<EjercitoOpcionView> ejercitosDisponibles,
            EjercitoCatalogoDetalleView ejercito
    ) {
    }

    public record FaccionCatalogoView(String nombre) {
    }

    public record EjercitoOpcionView(String nombre) {
    }

    public record EjercitoCatalogoDetalleView(
            String faccion,
            String nombre,
            int totalUnidades,
            List<UnidadCatalogoResumenView> unidades
    ) {
    }

    public record UnidadCatalogoResumenView(String nombre) {
    }

    public record InfoUnidad40kView(
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            String nombreUnidad,
            String perfiles,
            String armas,
            List<EstadisticaUnidadView> estadisticas,
            List<HabilidadUnidadView> habilidades
    ) {
    }

    public record EstadisticaUnidadView(String nombre, String valor) {
    }

    public record HabilidadUnidadView(String nombre, String descripcion) {
    }

    private record ArchivoCatalogo(
            String id,
            String nombre,
            boolean esLibreria,
            // selectionEntries y sharedSelectionEntries visibles del catalogo.
            List<Element> unidadesRaiz,
            // Referencias a unidades raiz definidas fuera del catalogo actual.
            List<Element> enlacesRaiz,
            // ids de otros catalogos cuyos root entries deben incorporarse.
            List<String> idsCatalogosImportados,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
    }

    public record Unidad40k(
            String nombre,
            String puntos,
            String roles,
            String palabrasClaveFaccion,
            String palabrasClave,
            String perfiles,
            String habilidades,
            String armas,
            List<Estadistica40k> estadisticas,
            List<Habilidad40k> habilidadesDetalle,
            List<GrupoMiniaturas40k> gruposMiniaturas,
            List<OpcionComposicion40k> opcionesComposicion
    ) {
    }

    public record OpcionComposicion40k(
            String id,
            String nombre,
            int puntos,
            boolean seleccionPorDefecto,
            List<GrupoMiniaturas40k> gruposMiniaturas
    ) {
    }

    public record GrupoMiniaturas40k(
            String id,
            String nombre,
            int minimo,
            int maximo,
            List<ModeloUnidad40k> modelos,
            List<GrupoMiniaturas40k> subgrupos
    ) {
    }

    public record ModeloUnidad40k(
            String id,
            String nombre,
            int minimo,
            int maximo,
            List<String> equipamientoFijo,
            List<GrupoEquipamiento40k> gruposEquipamiento
    ) {
    }

    public record GrupoEquipamiento40k(
            String id,
            String nombre,
            int minimo,
            int maximo,
            List<OpcionEquipamiento40k> opciones
    ) {
    }

    public record OpcionEquipamiento40k(
            String id,
            String nombre,
            boolean seleccionPorDefecto,
            List<String> detalleEquipamiento
    ) {
    }

    private record ModelNode(
            Element definicion,
            Element origen
    ) {
    }

    private record GroupNode(
            Element definicion,
            Element origen
    ) {
    }

    public record Estadistica40k(String nombre, String valor) {
    }

    public record Habilidad40k(String nombre, String descripcion) {
    }
}
