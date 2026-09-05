package org.example.tfgenrique.service.catalogo40k;

import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Ejercito40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Estadistica40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.GrupoMiniaturas40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Habilidad40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.OpcionComposicion40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilArma40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilUnidad40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Unidad40k;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LectorCatalogo40k {
    private final LectorComposicion40k lectorComposicion = new LectorComposicion40k();
    private final LectorReglasConstruccion40k lectorReglas = new LectorReglasConstruccion40k();
    private static final String ESPACIO_NOMBRES_CATALOGO =
            "http://www.battlescribe.net/schema/catalogueSchema";
    private static final ObjectMapper MAPEADOR_JSON = new ObjectMapper();
    private static final Map<String, String> NOMBRES_ELEMENTOS_JSON = Map.ofEntries(
            Map.entry("catalogueLinks", "catalogueLink"),
            Map.entry("categoryEntries", "categoryEntry"),
            Map.entry("categoryLinks", "categoryLink"),
            Map.entry("characteristics", "characteristic"),
            Map.entry("conditions", "condition"),
            Map.entry("conditionGroups", "conditionGroup"),
            Map.entry("constraints", "constraint"),
            Map.entry("costs", "cost"),
            Map.entry("costTypes", "costType"),
            Map.entry("entryLinks", "entryLink"),
            Map.entry("infoLinks", "infoLink"),
            Map.entry("modifiers", "modifier"),
            Map.entry("profiles", "profile"),
            Map.entry("profileTypes", "profileType"),
            Map.entry("rules", "rule"),
            Map.entry("selectionEntries", "selectionEntry"),
            Map.entry("selectionEntryGroups", "selectionEntryGroup"),
            Map.entry("sharedProfiles", "profile"),
            Map.entry("sharedRules", "rule"),
            Map.entry("sharedSelectionEntries", "selectionEntry"),
            Map.entry("sharedSelectionEntryGroups", "selectionEntryGroup")
    );
    private static final Set<String> NOMBRES_ROL_UNIDAD = Set.of(
            "Epic Hero", "Character", "Battleline", "Dedicated Transport", "Infantry", "Mounted",
            "Vehicle", "Monster", "Walker", "Aircraft", "Fortification", "Beast", "Swarm"
    );

    public Catalogo40kData leerCatalogo(InputStream flujoEntrada) throws Exception {
        List<ArchivoCatalogo> archivosCatalogo = new ArrayList<>();
        List<JsonNode> documentosJson = new ArrayList<>();
        Map<String, Map<String, Ejercito40k>> catalogoPorFaccion = new TreeMap<>();

        try (ZipInputStream flujoZip = new ZipInputStream(flujoEntrada)) {
            ZipEntry entradaZip;
            while ((entradaZip = flujoZip.getNextEntry()) != null) {
                String nombreEntrada = entradaZip.getName().toLowerCase(Locale.ROOT);
                if (!entradaZip.isDirectory() && nombreEntrada.endsWith(".json")) {
                    byte[] contenido = flujoZip.readAllBytes();
                    JsonNode documentoJson = MAPEADOR_JSON.readTree(new ByteArrayInputStream(contenido));
                    documentosJson.add(documentoJson);
                    ArchivoCatalogo archivoCatalogo = leerArchivoCatalogoJson(documentoJson);
                    if (archivoCatalogo != null) {
                        archivosCatalogo.add(archivoCatalogo);
                    }
                }
                flujoZip.closeEntry();
            }
        }

        if (archivosCatalogo.isEmpty()) {
            throw new IllegalArgumentException("El ZIP no contiene catalogos .json reconocibles");
        }

        Map<String, Element> unidadesRaizPorId = new HashMap<>();
        Map<String, Element> selectionEntriesPorId = new HashMap<>();
        Map<String, Element> selectionEntryGroupsPorId = new HashMap<>();
        Map<String, Element> perfilesPorId = new HashMap<>();
        Map<String, ArchivoCatalogo> archivosCatalogoPorId = new HashMap<>();

        for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
            archivosCatalogoPorId.put(archivoCatalogo.id(), archivoCatalogo);
            for (Element unidadRaiz : archivoCatalogo.unidadesRaiz()) {
                unidadesRaizPorId.put(unidadRaiz.getAttribute("id"), unidadRaiz);
            }
            selectionEntriesPorId.putAll(archivoCatalogo.selectionEntriesPorId());
            selectionEntryGroupsPorId.putAll(archivoCatalogo.selectionEntryGroupsPorId());
            perfilesPorId.putAll(archivoCatalogo.perfilesPorId());
        }

        for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
            Ejercito40k ejercito = crearEjercito(
                    archivoCatalogo,
                    unidadesRaizPorId,
                    archivosCatalogoPorId,
                    selectionEntriesPorId,
                    selectionEntryGroupsPorId,
                    perfilesPorId
            );
            if (ejercito != null) {
                catalogoPorFaccion
                        .computeIfAbsent(ejercito.faccion(), clave -> new TreeMap<>())
                        .put(ejercito.nombre(), ejercito);
            }
        }

        LectorReglasConstruccion40k.ResultadoReglas resultadoReglas = lectorReglas.leer(documentosJson);
        return new Catalogo40kData(
                catalogoPorFaccion,
                resultadoReglas.reglasEjercitos(),
                resultadoReglas.tamanosBatalla(),
                LocalDateTime.now()
        );
    }

    private ArchivoCatalogo leerArchivoCatalogoJson(JsonNode raiz) throws Exception {
        JsonNode catalogoJson = raiz.path("catalogue");
        if (!catalogoJson.isObject()) {
            return null;
        }

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        Document documento = fabrica.newDocumentBuilder().newDocument();
        Element catalogo = crearElementoDesdeJson(documento, "catalogue", catalogoJson);
        documento.appendChild(catalogo);
        return crearArchivoCatalogo(catalogo);
    }

    private Element crearElementoDesdeJson(Document documento, String nombre, JsonNode objeto) {
        Element elemento = documento.createElementNS(ESPACIO_NOMBRES_CATALOGO, nombre);

        for (Map.Entry<String, JsonNode> propiedad : objeto.properties()) {
            String nombrePropiedad = propiedad.getKey();
            JsonNode valor = propiedad.getValue();

            if ("$text".equals(nombrePropiedad)) {
                elemento.appendChild(documento.createTextNode(valor.asText("")));
            } else if (valor.isArray()) {
                Element contenedor = documento.createElementNS(ESPACIO_NOMBRES_CATALOGO, nombrePropiedad);
                String nombreHijo = NOMBRES_ELEMENTOS_JSON.getOrDefault(
                        nombrePropiedad,
                        singularizarNombreJson(nombrePropiedad)
                );
                for (JsonNode hijo : valor) {
                    if (hijo.isObject()) {
                        contenedor.appendChild(crearElementoDesdeJson(documento, nombreHijo, hijo));
                    } else if (!hijo.isNull()) {
                        Element elementoHijo = documento.createElementNS(ESPACIO_NOMBRES_CATALOGO, nombreHijo);
                        elementoHijo.setTextContent(hijo.asText(""));
                        contenedor.appendChild(elementoHijo);
                    }
                }
                elemento.appendChild(contenedor);
            } else if (valor.isObject()) {
                elemento.appendChild(crearElementoDesdeJson(documento, nombrePropiedad, valor));
            } else if (!valor.isNull() && !"xmlns".equals(nombrePropiedad)) {
                elemento.setAttribute(nombrePropiedad, valor.asText(""));
            }
        }
        return elemento;
    }

    private String singularizarNombreJson(String nombre) {
        if (nombre.endsWith("ies")) {
            return nombre.substring(0, nombre.length() - 3) + "y";
        }
        if (nombre.endsWith("s")) {
            return nombre.substring(0, nombre.length() - 1);
        }
        return nombre;
    }

    private ArchivoCatalogo crearArchivoCatalogo(Element catalogo) {
        String nombreCatalogo = catalogo.getAttribute("name");
        if (nombreCatalogo == null || nombreCatalogo.isBlank()) {
            return null;
        }

        return new ArchivoCatalogo(
                catalogo.getAttribute("id"),
                nombreCatalogo,
                Boolean.parseBoolean(catalogo.getAttribute("library")),
                obtenerUnidadesRaiz(catalogo),
                obtenerEnlacesRaiz(catalogo),
                obtenerIdsCatalogosImportados(catalogo),
                indexarElementosPorId(catalogo, "selectionEntry"),
                indexarElementosPorId(catalogo, "selectionEntryGroup"),
                indexarElementosPorId(catalogo, "profile")
        );
    }

    private Ejercito40k crearEjercito(
            ArchivoCatalogo archivoCatalogo,
            Map<String, Element> unidadesRaizPorId,
            Map<String, ArchivoCatalogo> archivosCatalogoPorId,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId,
            Map<String, Element> perfilesPorId
    ) {
        if (!esCatalogoSeleccionable(archivoCatalogo)) {
            return null;
        }

        String[] partesNombre = archivoCatalogo.nombre().split(" - ", 2);
        String faccion = partesNombre.length == 2 ? partesNombre[0].trim() : "Otros";
        String nombreEjercito = partesNombre.length == 2 ? partesNombre[1].trim() : archivoCatalogo.nombre().trim();

        Map<String, Element> elementosUnidad = new LinkedHashMap<>();
        agregarUnidadesDeCatalogo(
                archivoCatalogo,
                unidadesRaizPorId,
                archivosCatalogoPorId,
                elementosUnidad,
                new LinkedHashSet<>()
        );

        List<Unidad40k> unidades = elementosUnidad.values().stream()
                .map(unidad -> leerUnidad(unidad, selectionEntriesPorId, selectionEntryGroupsPorId, perfilesPorId))
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
        if (!idsCatalogoVisitados.add(archivoCatalogo.id())) {
            return;
        }

        for (Element unidadRaiz : archivoCatalogo.unidadesRaiz()) {
            if (esUnidadVisible(unidadRaiz)) {
                elementosUnidad.put(unidadRaiz.getAttribute("id"), unidadRaiz);
            }
        }

        for (Element enlaceUnidad : archivoCatalogo.enlacesRaiz()) {
            Element unidadDestino = unidadesRaizPorId.get(enlaceUnidad.getAttribute("targetId"));
            if (unidadDestino != null && esUnidadVisible(unidadDestino)) {
                elementosUnidad.put(unidadDestino.getAttribute("id"), unidadDestino);
            }
        }

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
                && !nombre.startsWith("Library -");
    }

    private boolean esUnidadVisible(Element entrada) {
        String tipo = entrada.getAttribute("type");
        return ("unit".equals(tipo) || "model".equals(tipo))
                && !Boolean.parseBoolean(entrada.getAttribute("hidden"));
    }

    private List<Element> obtenerUnidadesRaiz(Element catalogo) {
        List<Element> unidades = new ArrayList<>();

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
            Map<String, Element> selectionEntryGroupsPorId,
            Map<String, Element> perfilesPorId
    ) {
        List<Integer> puntos = lectorComposicion.leerPuntosUnidad(entradaUnidad);

        Set<String> roles = new LinkedHashSet<>();
        Set<String> palabrasClave = new LinkedHashSet<>();
        Set<String> palabrasClaveFaccion = new LinkedHashSet<>();

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
        List<PerfilUnidad40k> perfilesDetalle = new ArrayList<>();
        List<PerfilArma40k> armasDetalle = new ArrayList<>();
        List<OpcionComposicion40k> opcionesComposicion = lectorComposicion.leerOpcionesComposicion(
                entradaUnidad,
                puntos,
                selectionEntriesPorId,
                selectionEntryGroupsPorId
        );
        List<GrupoMiniaturas40k> gruposMiniaturas = opcionesComposicion.isEmpty() ? lectorComposicion.leerGruposMiniaturas(
                entradaUnidad,
                selectionEntriesPorId,
                selectionEntryGroupsPorId
        ) : List.of();
        lectorComposicion.agregarNombresEquipamiento(armas, gruposMiniaturas, opcionesComposicion);

        for (Element perfil : obtenerElementosDescendientes(entradaUnidad, "profile")) {
            String tipoPerfil = perfil.getAttribute("typeName");
            String nombrePerfil = perfil.getAttribute("name");
            if (nombrePerfil == null || nombrePerfil.isBlank()) {
                continue;
            }

            if ("Unit".equalsIgnoreCase(tipoPerfil)) {
                perfilesUnidad.add(nombrePerfil + " " + leerCaracteristicas(perfil));
                List<Estadistica40k> estadisticasPerfil = leerEstadisticas(perfil);
                agregarPerfilUnidad(perfilesDetalle, nombrePerfil, estadisticasPerfil);
                if (estadisticas.isEmpty()) {
                    estadisticas = estadisticasPerfil;
                }
            } else if ("Abilities".equalsIgnoreCase(tipoPerfil)) {
                nombresHabilidades.add(nombrePerfil);
                habilidades.add(new Habilidad40k(nombrePerfil, leerDescripcionPerfil(perfil)));
            } else if (tipoPerfil.toLowerCase(Locale.ROOT).contains("weapon")) {
                armas.add(nombrePerfil);
                agregarPerfilArma(armasDetalle, nombrePerfil, tipoPerfil, leerEstadisticas(perfil));
            }
        }

        // Las armas también pueden vivir en bibliotecas y enlazarse desde el equipo.
        recopilarArmasEnlazadas(entradaUnidad, selectionEntriesPorId, selectionEntryGroupsPorId,
                perfilesPorId, new LinkedHashSet<>(), armasDetalle);
        armasDetalle.forEach(perfil -> armas.add(perfil.nombre()));

        return new Unidad40k(
                entradaUnidad.getAttribute("name"),
                puntos.isEmpty() ? "Sin coste directo" : unirEnteros(puntos),
                String.join(", ", roles),
                unirValores(palabrasClaveFaccion),
                unirValores(palabrasClave),
                unirValoresLimitados(perfilesUnidad, 4),
                unirValoresLimitados(nombresHabilidades, 8),
                unirValoresLimitados(armas, 10),
                estadisticas,
                habilidades,
                perfilesDetalle,
                armasDetalle,
                gruposMiniaturas,
                opcionesComposicion
        );
    }

    private void recopilarArmasEnlazadas(
            Element elemento, Map<String, Element> entradas, Map<String, Element> grupos,
            Map<String, Element> perfiles, Set<Element> visitados, List<PerfilArma40k> resultado
    ) {
        if (!visitados.add(elemento)) {
            return;
        }
        String nombreLocal = elemento.getLocalName();
        if ("profile".equals(nombreLocal)
                && elemento.getAttribute("typeName").toLowerCase(Locale.ROOT).contains("weapon")
                && !elemento.getAttribute("name").isBlank()) {
            agregarPerfilArma(resultado, elemento.getAttribute("name"), elemento.getAttribute("typeName"),
                    leerEstadisticas(elemento));
        }
        if ("entryLink".equals(nombreLocal) || "infoLink".equals(nombreLocal)) {
            String destino = elemento.getAttribute("targetId");
            Element enlazado = switch (elemento.getAttribute("type")) {
                case "selectionEntry" -> entradas.get(destino);
                case "selectionEntryGroup" -> grupos.get(destino);
                case "profile" -> perfiles.get(destino);
                default -> null;
            };
            if (enlazado != null) {
                recopilarArmasEnlazadas(enlazado, entradas, grupos, perfiles, visitados, resultado);
            }
        }
        for (Node hijo = elemento.getFirstChild(); hijo != null; hijo = hijo.getNextSibling()) {
            if (hijo instanceof Element elementoHijo) {
                recopilarArmasEnlazadas(elementoHijo, entradas, grupos, perfiles, visitados, resultado);
            }
        }
    }

    private void agregarPerfilUnidad(
            List<PerfilUnidad40k> perfiles,
            String nombre,
            List<Estadistica40k> estadisticas
    ) {
        boolean existe = perfiles.stream().anyMatch(perfil -> perfil.nombre().equalsIgnoreCase(nombre));
        if (!existe) {
            perfiles.add(new PerfilUnidad40k(nombre, List.copyOf(estadisticas)));
        }
    }

    private void agregarPerfilArma(
            List<PerfilArma40k> perfiles,
            String nombre,
            String tipo,
            List<Estadistica40k> estadisticas
    ) {
        boolean existe = perfiles.stream().anyMatch(perfil ->
                perfil.nombre().equalsIgnoreCase(nombre) && perfil.tipo().equalsIgnoreCase(tipo)
        );
        if (!existe) {
            perfiles.add(new PerfilArma40k(nombre, tipo, List.copyOf(estadisticas)));
        }
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

    private String leerCaracteristicas(Element perfil) {
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
        for (Element hijo : hijosDirectos(padre, nombreLocal)) {
            return hijo;
        }
        return null;
    }

    private List<Element> hijosDirectos(Element padre, String nombreLocal) {
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

    private String unirEnteros(List<Integer> valores) {
        return valores.stream()
                .map(String::valueOf)
                .reduce((izquierda, derecha) -> izquierda + ", " + derecha)
                .orElse("");
    }

    private String unirValoresLimitados(Set<String> valores, int limite) {
        List<String> lista = valores.stream()
                .filter(valor -> valor != null && !valor.isBlank())
                .limit(limite)
                .toList();
        return String.join(", ", lista);
    }

    private String unirValores(Set<String> valores) {
        List<String> lista = valores.stream()
                .filter(valor -> valor != null && !valor.isBlank())
                .toList();
        return String.join(", ", lista);
    }

    private record ArchivoCatalogo(
            String id,
            String nombre,
            boolean esLibreria,
            List<Element> unidadesRaiz,
            List<Element> enlacesRaiz,
            List<String> idsCatalogosImportados,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId,
            Map<String, Element> perfilesPorId
    ) {
    }

}
