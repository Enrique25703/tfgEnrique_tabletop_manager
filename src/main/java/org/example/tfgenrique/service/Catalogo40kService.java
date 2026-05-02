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
    private static final String CATALOG_URL = "https://codeload.github.com/BSData/wh40k-10e/zip/refs/heads/main";
    private static final String BATTLESCRIBE_NAMESPACE = "http://www.battlescribe.net/schema/catalogueSchema";
    private static final Set<String> UNIT_ROLE_NAMES = Set.of(
            "Epic Hero", "Character", "Battleline", "Dedicated Transport", "Infantry", "Mounted",
            "Vehicle", "Monster", "Walker", "Aircraft", "Fortification", "Beast", "Swarm"
    );

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private Catalogo40kData data = new Catalogo40kData(Map.of(), LocalDateTime.MIN);

    public synchronized Catalogo40kData actualizarCatalogo() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(CATALOG_URL)).GET().build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("GitHub respondio con estado " + response.statusCode());
            }

            List<CatalogFile> catalogFiles = new ArrayList<>();
            Map<String, Map<String, Ejercito40k>> catalogo = new TreeMap<>();
            try (ZipInputStream zip = new ZipInputStream(response.body())) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".cat")) {
                        CatalogFile catalogFile = leerCatalogFile(new ByteArrayInputStream(zip.readAllBytes()));
                        if (catalogFile != null) {
                            catalogFiles.add(catalogFile);
                        }
                    }
                    zip.closeEntry();
                }
            }

            Map<String, Element> rootUnitsById = new HashMap<>();
            Map<String, CatalogFile> catalogFilesById = new HashMap<>();
            for (CatalogFile catalogFile : catalogFiles) {
                catalogFilesById.put(catalogFile.id(), catalogFile);
                for (Element unit : catalogFile.rootUnits()) {
                    rootUnitsById.put(unit.getAttribute("id"), unit);
                }
            }

            for (CatalogFile catalogFile : catalogFiles) {
                Ejercito40k ejercito = crearEjercito(catalogFile, rootUnitsById, catalogFilesById);
                if (ejercito != null) {
                    catalogo.computeIfAbsent(ejercito.faccion(), key -> new TreeMap<>())
                            .put(ejercito.nombre(), ejercito);
                }
            }
            data = new Catalogo40kData(catalogo, LocalDateTime.now());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo actualizar el catalogo de Warhammer 40k", ex);
        }
        return data;
    }

    public Catalogo40kData getData() {
        return data;
    }

    private CatalogFile leerCatalogFile(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder()
                .parse(new InputSource(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));

        Element catalogue = document.getDocumentElement();
        if (!"catalogue".equals(catalogue.getLocalName())) {
            return null;
        }

        String catalogueName = catalogue.getAttribute("name");
        if (catalogueName == null || catalogueName.isBlank()) {
            return null;
        }

        return new CatalogFile(
                catalogue.getAttribute("id"),
                catalogueName,
                Boolean.parseBoolean(catalogue.getAttribute("library")),
                rootSelectionEntries(catalogue),
                rootEntryLinks(catalogue),
                importedCatalogueIds(catalogue)
        );
    }

    private Ejercito40k crearEjercito(
            CatalogFile catalogFile,
            Map<String, Element> rootUnitsById,
            Map<String, CatalogFile> catalogFilesById
    ) {
        if (!esCatalogoSeleccionable(catalogFile)) {
            return null;
        }

        String[] nameParts = catalogFile.name().split(" - ", 2);
        String faccion = nameParts.length == 2 ? nameParts[0].trim() : "Otros";
        String nombreEjercito = nameParts.length == 2 ? nameParts[1].trim() : catalogFile.name().trim();

        Map<String, Element> unitElements = new LinkedHashMap<>();
        añadirUnidadesDeCatalogo(catalogFile, rootUnitsById, catalogFilesById, unitElements, new LinkedHashSet<>());

        List<Unidad40k> unidades = unitElements.values().stream()
                .map(this::leerUnidad)
                .sorted(Comparator.comparing(Unidad40k::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new Ejercito40k(faccion, nombreEjercito, unidades);
    }

    private void añadirUnidadesDeCatalogo(
            CatalogFile catalogFile,
            Map<String, Element> rootUnitsById,
            Map<String, CatalogFile> catalogFilesById,
            Map<String, Element> unitElements,
            Set<String> visitedCatalogIds
    ) {
        if (!visitedCatalogIds.add(catalogFile.id())) {
            return;
        }

        for (Element unit : catalogFile.rootUnits()) {
            if (esUnidadVisible(unit)) {
                unitElements.put(unit.getAttribute("id"), unit);
            }
        }

        for (Element entryLink : catalogFile.rootEntryLinks()) {
            Element targetUnit = rootUnitsById.get(entryLink.getAttribute("targetId"));
            if (targetUnit != null && esUnidadVisible(targetUnit)) {
                unitElements.put(targetUnit.getAttribute("id"), targetUnit);
            }
        }

        for (String importedCatalogueId : catalogFile.importedCatalogueIds()) {
            CatalogFile importedCatalogFile = catalogFilesById.get(importedCatalogueId);
            if (importedCatalogFile != null) {
                añadirUnidadesDeCatalogo(importedCatalogFile, rootUnitsById, catalogFilesById, unitElements, visitedCatalogIds);
            }
        }
    }

    private boolean esCatalogoSeleccionable(CatalogFile catalogFile) {
        String name = catalogFile.name();
        return !name.endsWith(" Library")
                && !name.contains(" - Library")
                && !name.startsWith("Library -");
    }

    private boolean esUnidadVisible(Element entry) {
        String type = entry.getAttribute("type");
        return ("unit".equals(type) || "model".equals(type)) && !Boolean.parseBoolean(entry.getAttribute("hidden"));
    }

    private List<Element> rootSelectionEntries(Element catalogue) {
        List<Element> units = new ArrayList<>();
        for (String containerName : List.of("selectionEntries", "sharedSelectionEntries")) {
            Element container = firstDirectChild(catalogue, containerName);
            if (container != null) {
                for (Element entry : directChildren(container, "selectionEntry")) {
                    if (esUnidadVisible(entry)) {
                        units.add(entry);
                    }
                }
            }
        }
        return units;
    }

    private List<Element> rootEntryLinks(Element catalogue) {
        Element container = firstDirectChild(catalogue, "entryLinks");
        if (container == null) {
            return List.of();
        }
        return directChildren(container, "entryLink").stream()
                .filter(entryLink -> "selectionEntry".equals(entryLink.getAttribute("type")))
                .filter(entryLink -> !Boolean.parseBoolean(entryLink.getAttribute("hidden")))
                .toList();
    }

    private List<String> importedCatalogueIds(Element catalogue) {
        Element container = firstDirectChild(catalogue, "catalogueLinks");
        if (container == null) {
            return List.of();
        }
        return directChildren(container, "catalogueLink").stream()
                .filter(catalogueLink -> "true".equals(catalogueLink.getAttribute("importRootEntries")))
                .map(catalogueLink -> catalogueLink.getAttribute("targetId"))
                .filter(targetId -> targetId != null && !targetId.isBlank())
                .toList();
    }

    private Unidad40k leerUnidad(Element unitEntry) {
        List<Integer> puntos = descendantElements(unitEntry, "cost").stream()
                .filter(cost -> "pts".equalsIgnoreCase(cost.getAttribute("name")))
                .map(cost -> parseInt(cost.getAttribute("value")))
                .filter(Objects::nonNull)
                .filter(value -> value > 0)
                .distinct()
                .sorted()
                .toList();

        Set<String> roles = new LinkedHashSet<>();
        Set<String> keywords = new LinkedHashSet<>();
        Set<String> factionKeywords = new LinkedHashSet<>();
        for (Element category : descendantElements(unitEntry, "categoryLink")) {
            String name = category.getAttribute("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            if (name.startsWith("Faction:")) {
                factionKeywords.add(name.substring("Faction:".length()).trim());
            } else if (UNIT_ROLE_NAMES.contains(name)) {
                roles.add(name);
            } else {
                keywords.add(name);
            }
        }

        Set<String> perfilesUnidad = new LinkedHashSet<>();
        Set<String> habilidades = new LinkedHashSet<>();
        Set<String> armas = new LinkedHashSet<>();
        for (Element profile : descendantElements(unitEntry, "profile")) {
            String typeName = profile.getAttribute("typeName");
            String name = profile.getAttribute("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            if ("Unit".equalsIgnoreCase(typeName)) {
                perfilesUnidad.add(name + " " + leerCaracteristicas(profile));
            } else if ("Abilities".equalsIgnoreCase(typeName)) {
                habilidades.add(name);
            } else if (typeName != null && typeName.toLowerCase(Locale.ROOT).contains("weapons")) {
                armas.add(name);
            }
        }

        return new Unidad40k(
                unitEntry.getAttribute("name"),
                puntos.isEmpty() ? "Sin coste directo" : joinInts(puntos),
                String.join(", ", roles),
                String.join(", ", factionKeywords),
                limitar(keywords, 12),
                limitar(perfilesUnidad, 4),
                limitar(habilidades, 8),
                limitar(armas, 10)
        );
    }

    private String leerCaracteristicas(Element profile) {
        Map<String, String> values = new LinkedHashMap<>();
        for (Element characteristic : descendantElements(profile, "characteristic")) {
            String name = characteristic.getAttribute("name");
            String value = characteristic.getTextContent();
            if (name != null && !name.isBlank() && value != null && !value.isBlank()) {
                values.put(name, value.trim());
            }
        }
        if (values.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        values.forEach((key, value) -> parts.add(key + ": " + value));
        return "(" + String.join(", ", parts) + ")";
    }

    private List<Element> descendantElements(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(BATTLESCRIBE_NAMESPACE, localName);
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element) {
                elements.add(element);
            }
        }
        return elements;
    }

    private Element firstDirectChild(Element parent, String localName) {
        for (Element child : directChildren(parent, localName)) {
            return child;
        }
        return null;
    }

    private List<Element> directChildren(Element parent, String localName) {
        List<Element> children = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && localName.equals(element.getLocalName())) {
                children.add(element);
            }
        }
        return children;
    }

    private Integer parseInt(String value) {
        try {
            return Math.round(Float.parseFloat(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String joinInts(List<Integer> values) {
        return values.stream().map(String::valueOf).reduce((left, right) -> left + ", " + right).orElse("");
    }

    private String limitar(Set<String> values, int limit) {
        List<String> list = values.stream().filter(value -> value != null && !value.isBlank()).limit(limit).toList();
        return String.join(", ", list);
    }

    public record Catalogo40kData(Map<String, Map<String, Ejercito40k>> facciones, LocalDateTime actualizadoEn) {
        public Ejercito40k buscarEjercito(String faccion, String ejercito) {
            if (faccion == null || ejercito == null) {
                return null;
            }
            Map<String, Ejercito40k> ejercitos = facciones.get(faccion);
            return ejercitos == null ? null : ejercitos.get(ejercito);
        }
    }

    public record Ejercito40k(String faccion, String nombre, List<Unidad40k> unidades) {
    }

    private record CatalogFile(
            String id,
            String name,
            boolean library,
            List<Element> rootUnits,
            List<Element> rootEntryLinks,
            List<String> importedCatalogueIds
    ) {
    }

    public record Unidad40k(
            String nombre,
            String puntos,
            String roles,
            String faccionKeywords,
            String keywords,
            String perfil,
            String habilidades,
            String armas
    ) {
    }
}
