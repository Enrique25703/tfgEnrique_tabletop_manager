package org.example.tfgenrique.service.catalogos;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public record ClasificacionUnidad(boolean legends, boolean aliado, boolean estructura) {
    public static ClasificacionUnidad normal() {
        return new ClasificacionUnidad(false, false, false);
    }

    public static ClasificacionUnidad leer(Element unidad, Element enlace, String catalogoSeleccionado) {
        Set<String> etiquetas = new LinkedHashSet<>();
        agregarEtiquetas(unidad, etiquetas);
        agregarEtiquetas(enlace, etiquetas);
        String origen = unidad.getOwnerDocument().getDocumentElement().getAttribute("name");
        String incorporacion = enlace == null ? origen
                : enlace.getOwnerDocument().getDocumentElement().getAttribute("name");
        boolean legends = etiquetas.stream().anyMatch(e -> contienePalabra(e, "legends"))
                || contienePalabra(normalizar(origen), "legends")
                || contienePalabra(normalizar(incorporacion), "legends");
        boolean estructura = etiquetas.stream().anyMatch(e -> Set.of(
                "fortification", "fortifications", "faction terrain", "terrain", "terrain feature",
                "fortificacion", "fortificaciones", "estructura", "estructuras").contains(e));
        boolean aliado = etiquetas.stream().anyMatch(e -> contienePalabra(e, "allied")
                || contienePalabra(e, "ally") || contienePalabra(e, "allies")
                || contienePalabra(e, "aliado") || contienePalabra(e, "aliados")
                || e.startsWith("regiment of renown"))
                || esCatalogoAjeno(incorporacion, catalogoSeleccionado)
                || esCatalogoAjeno(origen, catalogoSeleccionado);
        return new ClasificacionUnidad(legends, aliado, estructura);
    }

    private static void agregarEtiquetas(Element entrada, Set<String> etiquetas) {
        if (entrada == null) return;
        etiquetas.add(normalizar(entrada.getAttribute("name")));
        // Solo categorías de la unidad/enlace: las opciones de equipo pueden tener
        // categorías distintas y no deben clasificar a toda la unidad.
        for (Node hijo = entrada.getFirstChild(); hijo != null; hijo = hijo.getNextSibling()) {
            if (hijo instanceof Element contenedor && "categoryLinks".equals(contenedor.getLocalName())) {
                for (Node nodo = contenedor.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
                    if (nodo instanceof Element categoria && "categoryLink".equals(categoria.getLocalName())) {
                        etiquetas.add(normalizar(categoria.getAttribute("name")));
                    }
                }
            }
        }
    }

    private static boolean esCatalogoAjeno(String origen, String seleccionado) {
        String fuente = familia(origen);
        String destino = familia(seleccionado);
        if (fuente.isBlank() || destino.isBlank() || fuente.equals(destino)) return false;
        if (fuente.contains("regiments of renown")) return true;
        // Bibliotecas de reglas y de campaña no representan otra facción.
        if (fuente.equals("lores") || fuente.startsWith("path to glory")
                || fuente.contains("manifestations") || fuente.contains("endless spells")) return false;
        // Bibliotecas comunes a los capítulos Astartes y a sus unidades de Herejía.
        if (fuente.contains("astartes heresy")) return false;
        if (fuente.contains("titans") && destino.contains("titan")) return false;
        if (fuente.equals("aeldari aeldari") && destino.startsWith("aeldari ")) return false;
        if (destino.equals("big waaagh") && Set.of("ironjawz", "kruleboyz", "bonesplitterz", "orruk warclans").contains(fuente)) return false;
        return true;
    }

    private static String familia(String nombre) {
        String limpio = nombre == null ? "" : nombre.replaceAll("(?i)\\bLibrary\\b|\\[LEGENDS\\]", "");
        String[] partes = limpio.split("\\s+-\\s+");
        String primero = normalizar(partes[0]);
        if (Set.of("imperium", "chaos", "aeldari").contains(primero) && partes.length > 1) {
            String faccion = normalizar(partes[1]);
            if (primero.equals("chaos") && faccion.equals("daemons")) faccion = "chaos daemons";
            return primero + " " + faccion;
        }
        // Una biblioteca con prefijo 'Library -' empieza con un segmento vacío.
        return primero.isBlank() && partes.length > 1 ? normalizar(partes[1]) : primero;
    }

    private static boolean contienePalabra(String texto, String palabra) {
        return (" " + texto + " ").contains(" " + palabra + " ");
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ").trim();
    }
}
