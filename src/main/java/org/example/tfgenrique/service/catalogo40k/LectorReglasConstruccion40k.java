package org.example.tfgenrique.service.catalogo40k;

import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Destacamento40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Mejora40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.ReglaConstruccion40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.ReglasEjercito40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.TamanoBatalla40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.VinculosUnidad40k;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LectorReglasConstruccion40k {
    private static final Set<String> DISPOSICIONES = Set.of(
            "Take and Hold", "Disruption", "Purge the Foe", "Priority Assets", "Reconnaissance"
    );
    private static final Pattern TEXTO_DESTACADO = Pattern.compile("\\*\\*(?:\\^\\^)?([^*]+?)(?:\\^\\^)?\\*\\*");

    ResultadoReglas leer(List<JsonNode> documentos) {
        Map<String, DocumentoCatalogo> catalogosPorId = new HashMap<>();
        List<DocumentoCatalogo> catalogos = new ArrayList<>();
        JsonNode sistemaJuego = null;

        for (JsonNode documento : documentos) {
            JsonNode catalogo = documento.path("catalogue");
            if (catalogo.isObject()) {
                DocumentoCatalogo metadatos = new DocumentoCatalogo(
                        texto(catalogo, "id"),
                        texto(catalogo, "name"),
                        catalogo.path("library").asBoolean(false),
                        catalogo
                );
                catalogos.add(metadatos);
                catalogosPorId.put(metadatos.id(), metadatos);
            } else if (documento.path("gameSystem").isObject()) {
                sistemaJuego = documento.path("gameSystem");
            }
        }

        Map<String, ReglasEjercito40k> reglas = new HashMap<>();
        for (DocumentoCatalogo catalogo : catalogos) {
            if (!esCatalogoSeleccionable(catalogo)) {
                continue;
            }

            List<JsonNode> fuentes = new ArrayList<>();
            fuentes.add(catalogo.raiz());
            for (JsonNode enlace : catalogo.raiz().path("catalogueLinks")) {
                DocumentoCatalogo importado = catalogosPorId.get(texto(enlace, "targetId"));
                if (importado != null && importado.libreria()) {
                    fuentes.add(importado.raiz());
                }
            }

            String[] partes = catalogo.nombre().split(" - ", 2);
            String faccion = partes.length == 2 ? partes[0].trim() : "Otros";
            String ejercito = partes.length == 2 ? partes[1].trim() : catalogo.nombre().trim();
            reglas.put(
                    Catalogo40kData.claveEjercito(faccion, ejercito),
                    new ReglasEjercito40k(
                            leerDestacamentos(fuentes),
                            leerMejoras(fuentes),
                            leerVinculosUnidades(fuentes)
                    )
            );
        }

        return new ResultadoReglas(Map.copyOf(reglas), leerTamanosBatalla(sistemaJuego));
    }

    private List<TamanoBatalla40k> leerTamanosBatalla(JsonNode sistemaJuego) {
        Set<String> nombres = new LinkedHashSet<>();
        if (sistemaJuego != null) {
            recopilarNombres(sistemaJuego, nombres);
        }

        List<TamanoBatalla40k> tamanos = new ArrayList<>();
        if (contieneNombre(nombres, "Incursion")) {
            tamanos.add(new TamanoBatalla40k("INCURSION", "Incursion", 1000, 2, 2, 4, 2));
        }
        if (contieneNombre(nombres, "Strike Force")) {
            tamanos.add(new TamanoBatalla40k("STRIKE_FORCE", "Strike Force", 2000, 3, 3, 6, 4));
        }
        if (contieneNombre(nombres, "Onslaught")) {
            tamanos.add(new TamanoBatalla40k("ONSLAUGHT", "Onslaught", 3000, 4, 3, 6, 4));
        }
        if (tamanos.isEmpty()) {
            tamanos.add(new TamanoBatalla40k("INCURSION", "Incursion", 1000, 2, 2, 4, 2));
            tamanos.add(new TamanoBatalla40k("STRIKE_FORCE", "Strike Force", 2000, 3, 3, 6, 4));
        }
        return List.copyOf(tamanos);
    }

    private void recopilarNombres(JsonNode nodo, Set<String> nombres) {
        if (nodo.isObject()) {
            String nombre = texto(nodo, "name");
            if (!nombre.isBlank()) {
                nombres.add(nombre);
            }
            for (Map.Entry<String, JsonNode> propiedad : nodo.properties()) {
                recopilarNombres(propiedad.getValue(), nombres);
            }
        } else if (nodo.isArray()) {
            for (JsonNode hijo : nodo) {
                recopilarNombres(hijo, nombres);
            }
        }
    }

    private boolean contieneNombre(Set<String> nombres, String buscado) {
        return nombres.stream().anyMatch(nombre -> nombre.contains(buscado));
    }

    private List<Destacamento40k> leerDestacamentos(List<JsonNode> fuentes) {
        Map<String, Destacamento40k> destacamentos = new LinkedHashMap<>();
        for (JsonNode fuente : fuentes) {
            for (JsonNode grupo : gruposRaiz(fuente)) {
                String nombreGrupo = normalizar(texto(grupo, "name"));
                if (!nombreGrupo.equals("detachment") && !nombreGrupo.equals("detachments")) {
                    continue;
                }
                for (JsonNode entrada : grupo.path("selectionEntries")) {
                    if (entrada.path("hidden").asBoolean(false)) {
                        continue;
                    }
                    List<String> disposiciones = new ArrayList<>();
                    for (JsonNode categoria : entrada.path("categoryLinks")) {
                        String nombreCategoria = texto(categoria, "name");
                        if (DISPOSICIONES.contains(nombreCategoria)) {
                            disposiciones.add(nombreCategoria);
                        }
                    }
                    List<ReglaConstruccion40k> reglas = new ArrayList<>();
                    for (JsonNode regla : entrada.path("rules")) {
                        reglas.add(new ReglaConstruccion40k(texto(regla, "name"), texto(regla, "description")));
                    }
                    Destacamento40k destacamento = new Destacamento40k(
                            texto(entrada, "id"),
                            texto(entrada, "name"),
                            coste(entrada, "Detachment Points"),
                            List.copyOf(disposiciones),
                            List.copyOf(reglas)
                    );
                    destacamentos.putIfAbsent(destacamento.id(), destacamento);
                }
            }
        }
        return destacamentos.values().stream()
                .sorted(Comparator.comparing(Destacamento40k::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<Mejora40k> leerMejoras(List<JsonNode> fuentes) {
        Map<String, Mejora40k> mejoras = new LinkedHashMap<>();
        for (JsonNode fuente : fuentes) {
            for (JsonNode raizMejoras : gruposRaiz(fuente)) {
                String nombreRaiz = texto(raizMejoras, "name");
                boolean upgrade = "Enhancements - Upgrades".equalsIgnoreCase(nombreRaiz);
                if (!upgrade && !"Enhancements".equalsIgnoreCase(nombreRaiz)) {
                    continue;
                }
                for (JsonNode grupo : raizMejoras.path("selectionEntryGroups")) {
                    String destacamento = limpiarNombreGrupoMejoras(texto(grupo, "name"));
                    for (JsonNode entrada : grupo.path("selectionEntries")) {
                        if (entrada.path("hidden").asBoolean(false)) {
                            continue;
                        }
                        Mejora40k mejora = new Mejora40k(
                                texto(entrada, "id"),
                                texto(entrada, "name"),
                                coste(entrada, "pts"),
                                destacamento,
                                upgrade,
                                leerDescripcion(entrada)
                        );
                        mejoras.putIfAbsent(mejora.id(), mejora);
                    }
                }
            }
        }
        return mejoras.values().stream()
                .sorted(Comparator.comparing(Mejora40k::destacamento, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Mejora40k::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Map<String, VinculosUnidad40k> leerVinculosUnidades(List<JsonNode> fuentes) {
        Map<String, VinculosUnidad40k> vinculos = new HashMap<>();
        for (JsonNode fuente : fuentes) {
            for (String contenedor : List.of("selectionEntries", "sharedSelectionEntries")) {
                for (JsonNode unidad : fuente.path(contenedor)) {
                    String nombreUnidad = texto(unidad, "name");
                    if (nombreUnidad.isBlank()) {
                        continue;
                    }
                    boolean leader = false;
                    boolean support = false;
                    Set<String> compatibles = new LinkedHashSet<>();
                    for (JsonNode perfil : unidad.path("profiles")) {
                        String nombrePerfil = texto(perfil, "name");
                        if (!"Leader".equalsIgnoreCase(nombrePerfil) && !"Support".equalsIgnoreCase(nombrePerfil)) {
                            continue;
                        }
                        leader |= "Leader".equalsIgnoreCase(nombrePerfil);
                        support |= "Support".equalsIgnoreCase(nombrePerfil);
                        for (JsonNode caracteristica : perfil.path("characteristics")) {
                            extraerUnidadesCompatibles(caracteristica.path("$text").asText(""), compatibles);
                        }
                    }
                    if (leader || support) {
                        vinculos.put(nombreUnidad, new VinculosUnidad40k(leader, support, List.copyOf(compatibles)));
                    }
                }
            }
        }
        return Map.copyOf(vinculos);
    }

    private void extraerUnidadesCompatibles(String descripcion, Set<String> compatibles) {
        Matcher matcher = TEXTO_DESTACADO.matcher(descripcion);
        while (matcher.find()) {
            String nombre = limpiarMarcado(matcher.group(1));
            if (!nombre.isBlank()) {
                compatibles.add(nombre);
            }
        }
        if (compatibles.isEmpty()) {
            for (String linea : descripcion.split("\\R")) {
                String nombre = limpiarMarcado(linea.replaceFirst("^[\\s\\-•]+", ""));
                if (!nombre.isBlank() && !nombre.toLowerCase(Locale.ROOT).contains("attached")) {
                    compatibles.add(nombre);
                }
            }
        }
    }

    private String leerDescripcion(JsonNode entrada) {
        List<String> partes = new ArrayList<>();
        for (JsonNode regla : entrada.path("rules")) {
            agregarSiPresente(partes, texto(regla, "description"));
        }
        for (JsonNode perfil : entrada.path("profiles")) {
            for (JsonNode caracteristica : perfil.path("characteristics")) {
                agregarSiPresente(partes, caracteristica.path("$text").asText(""));
            }
        }
        return String.join("\n\n", partes);
    }

    private void agregarSiPresente(List<String> valores, String valor) {
        if (valor != null && !valor.isBlank()) {
            valores.add(valor.trim());
        }
    }

    private List<JsonNode> gruposRaiz(JsonNode fuente) {
        List<JsonNode> grupos = new ArrayList<>();
        for (String contenedor : List.of("selectionEntryGroups", "sharedSelectionEntryGroups")) {
            for (JsonNode grupo : fuente.path(contenedor)) {
                grupos.add(grupo);
            }
        }
        return grupos;
    }

    private int coste(JsonNode entrada, String nombreCoste) {
        for (JsonNode coste : entrada.path("costs")) {
            if (nombreCoste.equalsIgnoreCase(texto(coste, "name"))) {
                return (int) Math.round(coste.path("value").asDouble(0));
            }
        }
        return 0;
    }

    private boolean esCatalogoSeleccionable(DocumentoCatalogo catalogo) {
        String nombre = catalogo.nombre();
        return !catalogo.libreria()
                && !nombre.endsWith(" Library")
                && !nombre.contains(" - Library")
                && !nombre.startsWith("Library -");
    }

    private String limpiarNombreGrupoMejoras(String nombre) {
        return nombre.replaceFirst("(?i)\\s+Enhancements$", "").trim();
    }

    private String limpiarMarcado(String texto) {
        return texto == null ? "" : texto
                .replace("^^", "")
                .replace("**", "")
                .replaceAll("[.:]+$", "")
                .trim();
    }

    private String normalizar(String texto) {
        String sinAcentos = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.trim().toLowerCase(Locale.ROOT);
    }

    private String texto(JsonNode nodo, String campo) {
        return nodo.path(campo).asText("").trim();
    }

    record ResultadoReglas(
            Map<String, ReglasEjercito40k> reglasEjercitos,
            List<TamanoBatalla40k> tamanosBatalla
    ) {
    }

    private record DocumentoCatalogo(String id, String nombre, boolean libreria, JsonNode raiz) {
    }
}
