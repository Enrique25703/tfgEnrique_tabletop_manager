package org.example.tfgenrique.service.catalogo40k;

import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.Catalogo40kService.Ejercito40k;
import org.example.tfgenrique.service.Catalogo40kService.Estadistica40k;
import org.example.tfgenrique.service.Catalogo40kService.GrupoEquipamiento40k;
import org.example.tfgenrique.service.Catalogo40kService.GrupoMiniaturas40k;
import org.example.tfgenrique.service.Catalogo40kService.Habilidad40k;
import org.example.tfgenrique.service.Catalogo40kService.ModeloUnidad40k;
import org.example.tfgenrique.service.Catalogo40kService.OpcionComposicion40k;
import org.example.tfgenrique.service.Catalogo40kService.OpcionEquipamiento40k;
import org.example.tfgenrique.service.Catalogo40kService.Unidad40k;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LectorCatalogo40k {
    private static final Set<String> NOMBRES_ROL_UNIDAD = Set.of(
            "Epic Hero", "Character", "Battleline", "Dedicated Transport", "Infantry", "Mounted",
            "Vehicle", "Monster", "Walker", "Aircraft", "Fortification", "Beast", "Swarm"
    );

    public Catalogo40kData leerCatalogo(InputStream flujoEntrada) throws Exception {
        List<ArchivoCatalogo> archivosCatalogo = new ArrayList<>();
        Map<String, Map<String, Ejercito40k>> catalogoPorFaccion = new TreeMap<>();

        try (ZipInputStream flujoZip = new ZipInputStream(flujoEntrada)) {
            ZipEntry entradaZip;
            while ((entradaZip = flujoZip.getNextEntry()) != null) {
                if (!entradaZip.isDirectory() && entradaZip.getName().endsWith(".cat")) {
                    ArchivoCatalogo archivoCatalogo = leerArchivoCatalogo(new ByteArrayInputStream(flujoZip.readAllBytes()));
                    if (archivoCatalogo != null) {
                        archivosCatalogo.add(archivoCatalogo);
                    }
                }
                flujoZip.closeEntry();
            }
        }

        Map<String, Element> unidadesRaizPorId = new HashMap<>();
        Map<String, Element> selectionEntriesPorId = new HashMap<>();
        Map<String, Element> selectionEntryGroupsPorId = new HashMap<>();
        Map<String, ArchivoCatalogo> archivosCatalogoPorId = new HashMap<>();

        for (ArchivoCatalogo archivoCatalogo : archivosCatalogo) {
            archivosCatalogoPorId.put(archivoCatalogo.id(), archivoCatalogo);
            for (Element unidadRaiz : archivoCatalogo.unidadesRaiz()) {
                unidadesRaizPorId.put(unidadRaiz.getAttribute("id"), unidadRaiz);
            }
            selectionEntriesPorId.putAll(archivoCatalogo.selectionEntriesPorId());
            selectionEntryGroupsPorId.putAll(archivoCatalogo.selectionEntryGroupsPorId());
        }

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

        return new Catalogo40kData(catalogoPorFaccion, LocalDateTime.now());
    }
    private ArchivoCatalogo leerArchivoCatalogo(InputStream flujoEntrada) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        Document documento = fabrica.newDocumentBuilder()
                .parse(new InputSource(new InputStreamReader(flujoEntrada, StandardCharsets.UTF_8)));

        Element catalogo = documento.getDocumentElement();
        if (!"catalogue".equals(catalogo.getLocalName())) {
            return null;
        }

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
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        List<Integer> puntos = leerPuntosUnidad(entradaUnidad);

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
        agregarNombresEquipamiento(armas, gruposMiniaturas, opcionesComposicion);

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

    private void agregarNombresEquipamiento(
            Set<String> nombres,
            List<GrupoMiniaturas40k> gruposMiniaturas,
            List<OpcionComposicion40k> opcionesComposicion
    ) {
        for (GrupoMiniaturas40k grupo : gruposMiniaturas) {
            agregarNombresEquipamientoGrupo(nombres, grupo);
        }

        for (OpcionComposicion40k opcion : opcionesComposicion) {
            for (GrupoMiniaturas40k grupo : opcion.gruposMiniaturas()) {
                agregarNombresEquipamientoGrupo(nombres, grupo);
            }
        }
    }

    private void agregarNombresEquipamientoGrupo(Set<String> nombres, GrupoMiniaturas40k grupo) {
        for (ModeloUnidad40k modelo : grupo.modelos()) {
            for (String equipamiento : modelo.equipamientoFijo()) {
                agregarNombreEquipamiento(nombres, equipamiento);
            }

            for (GrupoEquipamiento40k grupoEquipamiento : modelo.gruposEquipamiento()) {
                for (OpcionEquipamiento40k opcion : grupoEquipamiento.opciones()) {
                    agregarNombreEquipamiento(nombres, opcion.nombre());
                    for (String detalle : opcion.detalleEquipamiento()) {
                        agregarNombreEquipamiento(nombres, detalle);
                    }
                }
            }
        }

        for (GrupoMiniaturas40k subgrupo : grupo.subgrupos()) {
            agregarNombresEquipamientoGrupo(nombres, subgrupo);
        }
    }

    private void agregarNombreEquipamiento(Set<String> nombres, String nombre) {
        String texto = valorSeguroONulo(nombre);
        if (!texto.isBlank() && !debeIgnorarEntradaEquipamiento(texto)) {
            nombres.add(texto);
        }
    }

    private List<GrupoMiniaturas40k> leerGruposMiniaturas(
            Element entradaUnidad,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        if ("model".equals(entradaUnidad.getAttribute("type"))) {
            return List.of(crearGrupoMiniaturasModeloRaiz(entradaUnidad, selectionEntriesPorId, selectionEntryGroupsPorId));
        }

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

    private GrupoMiniaturas40k crearGrupoMiniaturasModeloRaiz(
            Element entradaUnidad,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        ModeloUnidad40k modelo = leerModeloUnidad(
                new ModelNode(entradaUnidad, entradaUnidad),
                selectionEntriesPorId,
                selectionEntryGroupsPorId
        );
        if (modelo.minimo() <= 0) {
            modelo = new ModeloUnidad40k(
                    modelo.id(),
                    modelo.nombre(),
                    1,
                    Math.max(modelo.maximo(), 1),
                    modelo.equipamientoFijo(),
                    modelo.gruposEquipamiento()
            );
        }

        return new GrupoMiniaturas40k(
                valorSeguroONulo(entradaUnidad.getAttribute("id")) + "-modelo",
                "Modelo",
                1,
                Math.max(modelo.maximo(), 1),
                List.of(modelo),
                List.of()
        );
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

        List<String> equipamientoFijo = new ArrayList<>(leerEquipamientoFijoDirecto(entradaModelo, selectionEntriesPorId));
        List<GrupoEquipamiento40k> gruposEquipamiento = new ArrayList<>();

        for (GroupNode grupo : obtenerGruposDirectos(entradaModelo, selectionEntryGroupsPorId)) {
            agregarGruposEquipamiento(
                    grupo,
                    equipamientoFijo,
                    gruposEquipamiento,
                    selectionEntriesPorId,
                    selectionEntryGroupsPorId
            );
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

    private void agregarGruposEquipamiento(
            GroupNode grupo,
            List<String> equipamientoFijo,
            List<GrupoEquipamiento40k> gruposEquipamiento,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
    ) {
        String nombreGrupo = obtenerNombreGrupo(grupo);
        if (debeIgnorarGrupoAnidado(nombreGrupo)) {
            return;
        }

        List<GroupNode> subgrupos = obtenerGruposDirectos(grupo.definicion(), selectionEntryGroupsPorId);
        if (!tieneOpcionesEquipamientoDirectas(grupo.definicion(), selectionEntriesPorId) && !subgrupos.isEmpty()) {
            for (GroupNode subgrupo : subgrupos) {
                agregarGruposEquipamiento(
                        subgrupo,
                        equipamientoFijo,
                        gruposEquipamiento,
                        selectionEntriesPorId,
                        selectionEntryGroupsPorId
                );
            }
            return;
        }

        GrupoEquipamiento40k grupoEquipamiento = leerGrupoEquipamiento(
                grupo.definicion(),
                grupo.origen(),
                selectionEntriesPorId,
                selectionEntryGroupsPorId
        );
        if (grupoEquipamiento == null || grupoEquipamiento.opciones().isEmpty()) {
            return;
        }

        if (grupoEquipamiento.opciones().size() == 1
                && grupoEquipamiento.minimo() == 1
                && grupoEquipamiento.maximo() == 1) {
            equipamientoFijo.add(grupoEquipamiento.opciones().get(0).nombre());
            return;
        }

        gruposEquipamiento.add(grupoEquipamiento);
    }

    private String obtenerNombreGrupo(GroupNode grupo) {
        String nombreOrigen = valorSeguroONulo(grupo.origen().getAttribute("name"));
        if (!nombreOrigen.isBlank()) {
            return nombreOrigen;
        }
        return valorSeguroONulo(grupo.definicion().getAttribute("name"));
    }

    private boolean tieneOpcionesEquipamientoDirectas(Element grupo, Map<String, Element> selectionEntriesPorId) {
        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries", "entryLinks")) {
            Element contenedor = primerHijoDirecto(grupo, nombreContenedor);
            if (contenedor == null) {
                continue;
            }

            for (Element entrada : hijosDirectos(contenedor, "selectionEntry")) {
                String nombre = valorSeguroONulo(entrada.getAttribute("name"));
                if (!Boolean.parseBoolean(entrada.getAttribute("hidden"))
                        && !nombre.isBlank()
                        && !debeIgnorarEntradaEquipamiento(nombre)) {
                    return true;
                }
            }

            for (Element enlace : hijosDirectos(contenedor, "entryLink")) {
                if (Boolean.parseBoolean(enlace.getAttribute("hidden"))) {
                    continue;
                }
                Element entradaResuelta = selectionEntriesPorId.get(enlace.getAttribute("targetId"));
                String nombre = obtenerNombreEnlace(enlace, entradaResuelta);
                if ("selectionEntry".equals(enlace.getAttribute("type"))
                        && !nombre.isBlank()
                        && !debeIgnorarEntradaEquipamiento(nombre)) {
                    return true;
                }
            }
        }
        return false;
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

    private List<String> leerEquipamientoFijoDirecto(Element entrada, Map<String, Element> selectionEntriesPorId) {
        List<String> equipamiento = new ArrayList<>();

        for (String nombreContenedor : List.of("selectionEntries", "sharedSelectionEntries", "entryLinks")) {
            Element contenedor = primerHijoDirecto(entrada, nombreContenedor);
            if (contenedor == null) {
                continue;
            }

            for (Element hijo : hijosDirectos(contenedor, "selectionEntry")) {
                if (!Boolean.parseBoolean(hijo.getAttribute("hidden"))) {
                    String nombre = valorSeguroONulo(hijo.getAttribute("name"));
                    if (!nombre.isBlank() && !debeIgnorarEntradaEquipamiento(nombre)) {
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

                Element entradaResuelta = selectionEntriesPorId.get(enlace.getAttribute("targetId"));
                String nombre = obtenerNombreEnlace(enlace, entradaResuelta);
                if (!nombre.isBlank() && !debeIgnorarEntradaEquipamiento(nombre)) {
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
                        opcion.idReferencia(),
                        opcion.nombre(),
                        opcion.id().equals(idSeleccionPorDefecto) || opcion.idReferencia().equals(idSeleccionPorDefecto),
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
                if (!nombre.isBlank() && !debeIgnorarEntradaEquipamiento(nombre)) {
                    opciones.add(new OpcionEquipamiento40k(
                            valorSeguroONulo(entrada.getAttribute("id")),
                            valorSeguroONulo(entrada.getAttribute("id")),
                            nombre,
                            false,
                            leerEquipamientoOpcion(entrada, selectionEntriesPorId)
                    ));
                }
            }

            for (Element enlace : hijosDirectos(contenedor, "entryLink")) {
                if (Boolean.parseBoolean(enlace.getAttribute("hidden"))) {
                    continue;
                }
                if ("selectionEntryGroup".equals(enlace.getAttribute("type"))) {
                    Element grupoResuelto = selectionEntryGroupsPorId.get(enlace.getAttribute("targetId"));
                    String nombreGrupo = obtenerNombreEnlace(enlace, grupoResuelto);
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

                Element entradaResuelta = selectionEntriesPorId.get(enlace.getAttribute("targetId"));
                String nombre = obtenerNombreEnlace(enlace, entradaResuelta);
                if (!nombre.isBlank() && !debeIgnorarEntradaEquipamiento(nombre)) {
                    opciones.add(new OpcionEquipamiento40k(
                            valorSeguroONulo(enlace.getAttribute("id")),
                            valorSeguroONulo(enlace.getAttribute("targetId")),
                            nombre,
                            false,
                            entradaResuelta == null ? List.of() : leerEquipamientoOpcion(entradaResuelta, selectionEntriesPorId)
                    ));
                }
            }
        }

        Element contenedorSubgrupos = primerHijoDirecto(grupo, "selectionEntryGroups");
        if (contenedorSubgrupos != null) {
            for (Element subgrupo : hijosDirectos(contenedorSubgrupos, "selectionEntryGroup")) {
                if (!Boolean.parseBoolean(subgrupo.getAttribute("hidden"))
                        && !debeIgnorarGrupoAnidado(subgrupo.getAttribute("name"))) {
                    recopilarOpcionesEquipamiento(
                            subgrupo,
                            opciones,
                            selectionEntriesPorId,
                            selectionEntryGroupsPorId,
                            idsVisitados
                    );
                }
            }
        }
    }

    private String obtenerNombreEnlace(Element enlace, Element destino) {
        String nombre = valorSeguroONulo(enlace.getAttribute("name"));
        if (!nombre.isBlank() || destino == null) {
            return nombre;
        }
        return valorSeguroONulo(destino.getAttribute("name"));
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

    private List<String> leerEquipamientoOpcion(Element entrada, Map<String, Element> selectionEntriesPorId) {
        return leerEquipamientoFijoDirecto(entrada, selectionEntriesPorId).stream()
                .filter(texto -> texto != null && !texto.isBlank())
                .distinct()
                .toList();
    }

    private boolean debeIgnorarEntradaEquipamiento(String nombreEntrada) {
        String texto = valorSeguroONulo(nombreEntrada).toLowerCase(Locale.ROOT);
        return texto.equals("warlord")
                || texto.equals("enhancements")
                || texto.equals("crusade")
                || texto.contains("battle honours")
                || texto.contains("battle traits")
                || texto.contains("battle scars")
                || texto.contains("weapon modifications")
                || texto.contains("weapon upgrades");
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

    private Integer parsearEntero(String valor) {
        try {
            return Math.round(Float.parseFloat(valor));
        } catch (NumberFormatException ex) {
            return null;
        }
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

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String valorSeguroONulo(String texto) {
        return texto == null ? "" : texto;
    }

    private record ArchivoCatalogo(
            String id,
            String nombre,
            boolean esLibreria,
            List<Element> unidadesRaiz,
            List<Element> enlacesRaiz,
            List<String> idsCatalogosImportados,
            Map<String, Element> selectionEntriesPorId,
            Map<String, Element> selectionEntryGroupsPorId
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

}
