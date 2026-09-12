package org.example.tfgenrique.service.partidas;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class Misiones40kService {

    public MisionView obtenerPrimeraMisionPrincipal() {
        return obtenerPrimeraMisionPrincipal("WH40K_11");
    }

    public MisionView obtenerPrimeraMisionPrincipal(String sistemaJuego) {
        List<MisionView> misiones = leerMisiones(rutaMisiones("principales", sistemaJuego), "misionesPrimarias");
        if (misiones.isEmpty()) {
            return new MisionView("sin-mision", "Sin mision", "", "", List.of());
        }
        return misiones.get(0);
    }

    public List<MisionView> obtenerMisionesSecundarias() {
        return obtenerMisionesSecundarias("WH40K_11");
    }

    public List<MisionView> obtenerMisionesSecundarias(String sistemaJuego) {
        return leerMisiones(rutaMisiones("secundarias", sistemaJuego), "misionesSecundarias");
    }

    private String rutaMisiones(String tipo, String sistemaJuego) {
        String sufijo = switch (sistemaJuego) {
            case "AOS_4" -> "AoS";
            case "WH40K_11" -> "40k";
            default -> throw new IllegalArgumentException("Sistema de juego no compatible.");
        };
        return "/catalogos/misiones-" + tipo + "-" + sufijo + ".cat";
    }

    public List<CombinacionMisionView> obtenerCombinacionesPorDefecto() {
        try (InputStream inputStream = getClass().getResourceAsStream("/catalogos/misiones-defaults.cat")) {
            if (inputStream == null) {
                return List.of();
            }

            Document documento = leerDocumento(inputStream);
            Element raiz = documento.getDocumentElement();
            Element bloque = primerHijo(raiz, "combinaciones");
            if (bloque == null) {
                return List.of();
            }

            List<CombinacionMisionView> combinaciones = new ArrayList<>();
            for (Element combinacion : hijos(bloque, "combinacion")) {
                List<Integer> layoutsRecomendados = new ArrayList<>();
                Element layouts = primerHijo(combinacion, "layoutsRecomendados");
                if (layouts != null) {
                    for (Element layout : hijos(layouts, "layout")) {
                        try {
                            layoutsRecomendados.add(Integer.parseInt(layout.getAttribute("numero")));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                String letra = combinacion.getAttribute("letra").trim().toUpperCase(Locale.ROOT);
                if (!letra.isBlank()) {
                    combinaciones.add(new CombinacionMisionView(
                            letra,
                            textoHijo(combinacion, "misionPrimaria"),
                            textoHijo(combinacion, "despliegue"),
                            List.copyOf(layoutsRecomendados)
                    ));
                }
            }
            return List.copyOf(combinaciones);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<MisionView> leerMisiones(String ruta, String bloqueMisiones) {
        try (InputStream inputStream = getClass().getResourceAsStream(ruta)) {
            if (inputStream == null) {
                return List.of();
            }

            Document documento = leerDocumento(inputStream);
            Element raiz = documento.getDocumentElement();
            Element bloque = primerHijo(raiz, bloqueMisiones);
            if (bloque == null) {
                return List.of();
            }

            List<MisionView> misiones = new ArrayList<>();
            for (Element mision : hijos(bloque, "mision")) {
                misiones.add(new MisionView(
                        mision.getAttribute("id"),
                        textoHijo(mision, "titulo"),
                        textoHijo(mision, "puntuacion"),
                        textoHijo(mision, "descripcion"),
                        leerCondiciones(mision)
                ));
            }
            return misiones;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Document leerDocumento(InputStream inputStream) throws Exception {
        String contenido = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        contenido = limpiarXml(contenido);

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(contenido)));
    }

    private String limpiarXml(String contenido) {
        String texto = contenido.trim();
        if (texto.startsWith("```xml")) {
            texto = texto.substring(6).trim();
        }
        if (texto.startsWith("```")) {
            texto = texto.substring(3).trim();
        }
        if (texto.endsWith("```")) {
            texto = texto.substring(0, texto.length() - 3).trim();
        }
        return texto;
    }

    private List<String> leerCondiciones(Element mision) {
        Element condiciones = primerHijo(mision, "condiciones");
        if (condiciones == null) {
            return List.of();
        }

        List<String> lista = new ArrayList<>();
        for (Element condicion : hijos(condiciones, "condicion")) {
            String texto = condicion.getTextContent();
            if (texto != null && !texto.isBlank()) {
                lista.add(texto.trim());
            }
        }
        return lista;
    }

    private String textoHijo(Element padre, String nombre) {
        Element hijo = primerHijo(padre, nombre);
        if (hijo == null || hijo.getTextContent() == null) {
            return "";
        }
        return hijo.getTextContent().trim();
    }

    private Element primerHijo(Element padre, String nombre) {
        for (Element hijo : hijos(padre, nombre)) {
            return hijo;
        }
        return null;
    }

    private List<Element> hijos(Element padre, String nombre) {
        List<Element> hijos = new ArrayList<>();
        NodeList nodos = padre.getChildNodes();
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo instanceof Element elemento && nombre.equals(elemento.getLocalName())) {
                hijos.add(elemento);
            }
        }
        return hijos;
    }

    public record MisionView(
            String id,
            String titulo,
            String puntuacion,
            String descripcion,
            List<String> condiciones
    ) {
    }

    public record CombinacionMisionView(
            String letra,
            String misionPrimaria,
            String despliegue,
            List<Integer> layoutsRecomendados
    ) {
    }
}
