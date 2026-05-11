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
public class Catalogo40kService {
    // ZIP publicado por BSData con los catalogos de Warhammer 40k 10a edicion.
    private static final String URL_CATALOGO = "https://codeload.github.com/BSData/wh40k-10e/zip/refs/heads/main";

    // Estos nombres se usan para distinguir "roles" de unidad del resto de category links.
    private static final Set<String> NOMBRES_ROL_UNIDAD = Set.of(
            "Epic Hero", "Character", "Battleline", "Dedicated Transport", "Infantry", "Mounted",
            "Vehicle", "Monster", "Walker", "Aircraft", "Fortification", "Beast", "Swarm"
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

            // Segundo indice: id de catalogo -> archivoCatalogo.
            // Hace falta para resolver imports entre catalogos.
            Map<String, ArchivoCatalogo> archivosCatalogoPorId = new HashMap<>();
            for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
                archivosCatalogoPorId.put(archivoCatalogo.id(), archivoCatalogo);
                for (Element unidadRaiz : archivoCatalogo.unidadesRaiz()) {
                    unidadesRaizPorId.put(unidadRaiz.getAttribute("id"), unidadRaiz);
                }
            }

            // Una vez indexado todo, se compone cada ejercito resolviendo sus imports y enlaces.
            for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
                Ejercito40k ejercito = crearEjercito(archivoCatalogo, unidadesRaizPorId, archivosCatalogoPorId);
                if (ejercito != null) {
                    catalogoPorFaccion
                            .computeIfAbsent(ejercito.faccion(), clave -> new TreeMap<>())
                            .put(ejercito.nombre(), ejercito);
                }
            }

            // Se guarda fecha/hora para saber cuando se refresco el cache.
            datos = new Catalogo40kData(catalogoPorFaccion, LocalDateTime.now());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo actualizar el catalogo de Warhammer 40k", ex);
        }
        return datos;
    }

    public Catalogo40kData getData() {
        // Devuelve el ultimo catalogo cargado, aunque la descarga mas reciente haya fallado.
        return datos;
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
                obtenerIdsCatalogosImportados(catalogo)
        );
    }

    private Ejercito40k crearEjercito(
            ArchivoCatalogo archivoCatalogo,
            Map<String, Element> unidadesRaizPorId,
            Map<String, ArchivoCatalogo> archivosCatalogoPorId
    ) {
        // Las librerias son catalogos auxiliares y no deben salir como ejercitos seleccionables.
        if (!esCatalogoSeleccionable(archivoCatalogo)) {
            return null;
        }

        // Muchos catalogos vienen con formato "Faccion - NombreEjercito".
        // Si no lo cumplen, se meten en "Otros".
        String[] partesNombre = archivoCatalogo.nombre().split(" - ", 2);
        String faccion = partesNombre.length == 2 ? partesNombre[0].trim() : "Otros";
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
                .map(this::leerUnidad)
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
        // BSData mezcla ejercitos jugables con librerias de datos compartidos.
        // Este filtro intenta excluir esas librerias auxiliares.
        String nombre = archivoCatalogo.nombre();
        return !archivoCatalogo.esLibreria()
                && !nombre.endsWith(" Library")
                && !nombre.contains(" - Library")
                && !nombre.startsWith("Library -");
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

    private Unidad40k leerUnidad(Element entradaUnidad) {
        // Una unidad puede tener varios costes repetidos o variantes.
        // Aqui se recogen los puntos positivos, sin duplicados, y ordenados.
        List<Integer> puntos = obtenerElementosDescendientes(entradaUnidad, "cost").stream()
                .filter(coste -> "pts".equalsIgnoreCase(coste.getAttribute("name")))
                .map(coste -> parsearEntero(coste.getAttribute("value")))
                .filter(Objects::nonNull)
                .filter(valor -> valor > 0)
                .distinct()
                .sorted()
                .toList();

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
        Set<String> habilidades = new LinkedHashSet<>();
        Set<String> armas = new LinkedHashSet<>();

        // Los perfiles contienen tanto estadisticas de unidad como habilidades y armas.
        for (Element perfil : obtenerElementosDescendientes(entradaUnidad, "profile")) {
            String tipoPerfil = perfil.getAttribute("typeName");
            String nombrePerfil = perfil.getAttribute("name");
            if (nombrePerfil == null || nombrePerfil.isBlank()) {
                continue;
            }

            if ("Unit".equalsIgnoreCase(tipoPerfil)) {
                perfilesUnidad.add(nombrePerfil + " " + leerCaracteristicas(perfil));
            } else if ("Abilities".equalsIgnoreCase(tipoPerfil)) {
                habilidades.add(nombrePerfil);
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
                unirValoresLimitados(habilidades, 8),
                unirValoresLimitados(armas, 10)
        );
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

    public record Catalogo40kData(Map<String, Map<String, Ejercito40k>> facciones, LocalDateTime actualizadoEn) {
        public Ejercito40k buscarEjercito(String faccion, String ejercito) {
            // Acceso seguro para la vista: si falta algo, devuelve null en vez de fallar.
            if (faccion == null || ejercito == null) {
                return null;
            }

            Map<String, Ejercito40k> ejercitos = facciones.get(faccion);
            return ejercitos == null ? null : ejercitos.get(ejercito);
        }
    }

    public record Ejercito40k(String faccion, String nombre, List<Unidad40k> unidades) {
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
            List<String> idsCatalogosImportados
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
            String armas
    ) {
    }
}
