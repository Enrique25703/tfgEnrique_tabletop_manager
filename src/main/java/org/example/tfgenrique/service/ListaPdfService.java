package org.example.tfgenrique.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.tfgenrique.service.CreacionListasService.ListaExportacionView;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Estadistica40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Habilidad40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilArma40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilUnidad40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Unidad40k;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ListaPdfService {
    private static final String FORMATO_40K = "WH40K_11";

    private final CreacionListasService creacionListasService;
    private final Catalogo40kService catalogo40kService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ListaPdfService(
            CreacionListasService creacionListasService,
            Catalogo40kService catalogo40kService
    ) {
        this.creacionListasService = creacionListasService;
        this.catalogo40kService = catalogo40kService;
    }

    public PdfGenerado generarPdf(String nombreUsuario, Long listaId) {
        ListaExportacionView lista = creacionListasService.obtenerListaParaExportar(nombreUsuario, listaId);
        if (!FORMATO_40K.equals(lista.formatoJuego())) {
            throw new IllegalArgumentException("La exportacion PDF esta disponible para listas de Warhammer 40,000.");
        }

        Catalogo40kData catalogo = catalogo40kService.getData();
        if (catalogo == null) {
            catalogo = catalogo40kService.actualizarCatalogo();
        }

        try {
            List<UnidadPdf> unidades = leerUnidades(lista, catalogo);
            byte[] contenido = crearDocumento(lista, unidades);
            return new PdfGenerado(crearNombreArchivo(lista.nombreLista()), contenido);
        } catch (IOException ex) {
            throw new IllegalStateException("No se ha podido generar el PDF de la lista.", ex);
        }
    }

    private byte[] crearDocumento(ListaExportacionView lista, List<UnidadPdf> unidades) throws IOException {
        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfLienzo lienzo = new PdfLienzo(documento);
            dibujarResumen(lienzo, lista, unidades);
            for (UnidadPdf unidad : unidades) {
                dibujarUnidad(lienzo, unidad);
            }
            lienzo.cerrar();
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private void dibujarResumen(
            PdfLienzo lienzo,
            ListaExportacionView lista,
            List<UnidadPdf> unidades
    ) throws IOException {
        lienzo.nuevaPaginaResumen();
        lienzo.textoCentrado(lista.nombreLista() + " [" + lista.puntosActuales() + " pts]", 15, true);
        lienzo.saltar(12);
        lienzo.texto("Lista de ejercito [" + lista.puntosActuales() + "/" + lista.limitePuntos() + " pts]", 11, false);
        lienzo.texto("Warhammer 40,000 - " + lista.faccion() + " - " + lista.ejercito(), 10, false);
        lienzo.texto("Version " + lista.numeroVersion() + " - " + unidades.size() + " unidades", 10, false);
        lienzo.saltar(8);

        List<List<String>> filas = new ArrayList<>();
        for (UnidadPdf unidad : unidades) {
            filas.add(List.of(
                    unidad.nombre(),
                    textoPorDefecto(unidad.roles(), "Sin rol"),
                    unidad.puntos() + " pts",
                    resumenComposicion(unidad.composicion())
            ));
        }
        lienzo.tabla(
                List.of("NOMBRE", "ROL", "PTS", "OPCIONES"),
                new float[]{0.42f, 0.18f, 0.10f, 0.30f},
                filas
        );
    }

    private void dibujarUnidad(PdfLienzo lienzo, UnidadPdf unidad) throws IOException {
        lienzo.nuevaUnidad(unidad.nombre(), unidad.puntos());

        lienzo.seccion("Composicion y equipo");
        List<List<String>> composicion = new ArrayList<>();
        for (FilaComposicion fila : unidad.composicion()) {
            composicion.add(List.of(fila.modelo(), fila.equipamiento()));
        }
        if (composicion.isEmpty()) {
            composicion.add(List.of("Unidad", "Sin configuracion de miniaturas registrada"));
        }
        lienzo.tabla(
                List.of("MINIATURAS", "EQUIPO SELECCIONADO"),
                new float[]{0.42f, 0.58f},
                composicion
        );

        Unidad40k datosCatalogo = unidad.datosCatalogo();
        if (datosCatalogo == null) {
            lienzo.seccion("Datos de la unidad");
            lienzo.parClaveValor("Aviso", "No se ha encontrado esta unidad en el catalogo actual.");
            dibujarMetadatos(lienzo, unidad, null);
            return;
        }

        List<PerfilUnidad40k> perfiles = datosCatalogo.perfilesDetalle();
        if (perfiles == null || perfiles.isEmpty()) {
            perfiles = List.of(new PerfilUnidad40k(unidad.nombre(), datosCatalogo.estadisticas()));
        }
        lienzo.seccion("Perfil de unidad");
        List<List<String>> filasPerfil = new ArrayList<>();
        for (PerfilUnidad40k perfil : perfiles) {
            filasPerfil.add(List.of(
                    perfil.nombre(),
                    valorEstadistica(perfil.estadisticas(), "M", "Move"),
                    valorEstadistica(perfil.estadisticas(), "T", "Toughness"),
                    valorEstadistica(perfil.estadisticas(), "SV", "Save"),
                    valorEstadistica(perfil.estadisticas(), "W", "Wounds"),
                    valorEstadistica(perfil.estadisticas(), "LD", "Leadership"),
                    valorEstadistica(perfil.estadisticas(), "OC", "Objective Control")
            ));
        }
        lienzo.tabla(
                List.of("UNIDAD", "M", "T", "SV", "W", "LD", "OC"),
                new float[]{0.43f, 0.095f, 0.095f, 0.095f, 0.095f, 0.095f, 0.095f},
                filasPerfil
        );

        List<PerfilArma40k> armas = filtrarArmas(datosCatalogo.armasDetalle(), unidad.equipamiento());
        dibujarArmas(lienzo, "Armas a distancia", armas, false);
        dibujarArmas(lienzo, "Armas cuerpo a cuerpo", armas, true);

        if (datosCatalogo.habilidadesDetalle() != null && !datosCatalogo.habilidadesDetalle().isEmpty()) {
            lienzo.seccion("Habilidades");
            List<List<String>> habilidades = new ArrayList<>();
            for (Habilidad40k habilidad : datosCatalogo.habilidadesDetalle()) {
                habilidades.add(List.of(
                        textoPorDefecto(habilidad.nombre(), "Habilidad"),
                        textoPorDefecto(habilidad.descripcion(), "Sin descripcion")
                ));
            }
            lienzo.tabla(
                    List.of("HABILIDAD", "DESCRIPCION"),
                    new float[]{0.22f, 0.78f},
                    habilidades
            );
        }

        dibujarMetadatos(lienzo, unidad, datosCatalogo);
    }

    private void dibujarArmas(
            PdfLienzo lienzo,
            String titulo,
            List<PerfilArma40k> armas,
            boolean cuerpoACuerpo
    ) throws IOException {
        List<List<String>> filas = new ArrayList<>();
        for (PerfilArma40k arma : armas) {
            boolean esCuerpoACuerpo = normalizar(arma.tipo()).contains("melee");
            if (esCuerpoACuerpo != cuerpoACuerpo) {
                continue;
            }
            filas.add(List.of(
                    arma.nombre(),
                    valorEstadistica(arma.estadisticas(), "Range"),
                    valorEstadistica(arma.estadisticas(), "A", "Attacks"),
                    valorEstadistica(arma.estadisticas(), cuerpoACuerpo ? "WS" : "BS"),
                    valorEstadistica(arma.estadisticas(), "S", "Strength"),
                    valorEstadistica(arma.estadisticas(), "AP"),
                    valorEstadistica(arma.estadisticas(), "D", "Damage"),
                    valorEstadistica(arma.estadisticas(), "Keywords")
            ));
        }

        if (filas.isEmpty()) {
            return;
        }

        lienzo.seccion(titulo);
        lienzo.tabla(
                List.of("ARMA", "ALCANCE", "A", cuerpoACuerpo ? "HA" : "HP", "F", "FP", "D", "CLAVES"),
                new float[]{0.31f, 0.10f, 0.065f, 0.065f, 0.065f, 0.065f, 0.065f, 0.265f},
                filas
        );
    }

    private void dibujarMetadatos(
            PdfLienzo lienzo,
            UnidadPdf unidad,
            Unidad40k datosCatalogo
    ) throws IOException {
        lienzo.seccion("Reglas y categorias");
        List<List<String>> metadatos = new ArrayList<>();
        metadatos.add(List.of("Rol", textoPorDefecto(unidad.roles(), "Sin rol")));
        if (datosCatalogo != null) {
            metadatos.add(List.of(
                    "Palabras clave",
                    unirNoVacios(datosCatalogo.palabrasClaveFaccion(), datosCatalogo.palabrasClave())
            ));
        }
        metadatos.add(List.of("Categoria de lista", textoPorDefecto(unidad.categoria(), "Sin categoria")));
        if (!unidad.notas().isBlank()) {
            metadatos.add(List.of("Notas", unidad.notas()));
        }
        lienzo.tabla(
                List.of("CAMPO", "DETALLE"),
                new float[]{0.16f, 0.84f},
                metadatos
        );
    }

    private List<UnidadPdf> leerUnidades(ListaExportacionView lista, Catalogo40kData catalogo) throws IOException {
        JsonNode raiz = objectMapper.readTree(lista.datosListaJson());
        List<UnidadPdf> unidades = new ArrayList<>();
        for (JsonNode unidadJson : raiz.path("unidades")) {
            String nombre = unidadJson.path("nombre").asText("");
            List<FilaComposicion> composicion = leerComposicion(unidadJson.path("configuracionMiniaturas"));
            Set<String> equipamiento = new LinkedHashSet<>();
            for (FilaComposicion fila : composicion) {
                if (!fila.equipamiento().isBlank()) {
                    equipamiento.addAll(separarEquipamiento(fila.equipamiento()));
                }
            }

            unidades.add(new UnidadPdf(
                    nombre,
                    unidadJson.path("roles").asText(""),
                    unidadJson.path("puntosBase").asInt(0),
                    unidadJson.path("categoria").asText(""),
                    unidadJson.path("notas").asText(""),
                    composicion,
                    Set.copyOf(equipamiento),
                    catalogo.buscarUnidad(lista.faccion(), lista.ejercito(), nombre)
            ));
        }
        return List.copyOf(unidades);
    }

    private List<FilaComposicion> leerComposicion(JsonNode configuracion) {
        JsonNode grupos = configuracion.path("gruposMiniaturas");
        JsonNode opciones = configuracion.path("opcionesComposicion");
        String opcionActiva = configuracion.path("opcionSeleccionadaId").asText("");
        if (opciones.isArray()) {
            for (JsonNode opcion : opciones) {
                if (opcionActiva.equals(opcion.path("id").asText(""))) {
                    grupos = opcion.path("gruposMiniaturas");
                    break;
                }
            }
        }

        Map<String, ComposicionAcumulada> acumuladas = new LinkedHashMap<>();
        recorrerGrupos(grupos, acumuladas);
        List<FilaComposicion> resultado = new ArrayList<>();
        for (ComposicionAcumulada acumulada : acumuladas.values()) {
            resultado.add(new FilaComposicion(
                    acumulada.cantidad() + "x " + acumulada.modelo(),
                    acumulada.equipamiento()
            ));
        }
        return List.copyOf(resultado);
    }

    private void recorrerGrupos(JsonNode grupos, Map<String, ComposicionAcumulada> acumuladas) {
        if (!grupos.isArray()) {
            return;
        }
        for (JsonNode grupo : grupos) {
            for (JsonNode modelo : grupo.path("modelos")) {
                for (JsonNode instancia : modelo.path("instancias")) {
                    LinkedHashSet<String> equipo = new LinkedHashSet<>();
                    for (JsonNode fijo : modelo.path("equipamientoFijo")) {
                        agregarTexto(equipo, fijo.asText(""));
                    }

                    JsonNode selecciones = instancia.path("selecciones");
                    for (JsonNode grupoEquipo : modelo.path("gruposEquipamiento")) {
                        JsonNode seleccionGrupo = selecciones.path(grupoEquipo.path("id").asText(""));
                        for (JsonNode opcion : grupoEquipo.path("opciones")) {
                            if (!contieneTexto(seleccionGrupo, opcion.path("id").asText(""))) {
                                continue;
                            }
                            agregarTexto(equipo, opcion.path("nombre").asText(""));
                            for (JsonNode detalle : opcion.path("detalleEquipamiento")) {
                                agregarTexto(equipo, detalle.asText(""));
                            }
                        }
                    }

                    String modeloNombre = textoPorDefecto(modelo.path("nombre").asText(""), "Miniatura");
                    String equipoTexto = String.join(", ", equipo);
                    String clave = modeloNombre + "\u0000" + equipoTexto;
                    ComposicionAcumulada existente = acumuladas.get(clave);
                    acumuladas.put(
                            clave,
                            new ComposicionAcumulada(
                                    modeloNombre,
                                    equipoTexto,
                                    existente == null ? 1 : existente.cantidad() + 1
                            )
                    );
                }
            }
            recorrerGrupos(grupo.path("subgrupos"), acumuladas);
        }
    }

    private boolean contieneTexto(JsonNode valores, String buscado) {
        if (!valores.isArray()) {
            return false;
        }
        for (JsonNode valor : valores) {
            if (buscado.equals(valor.asText(""))) {
                return true;
            }
        }
        return false;
    }

    private void agregarTexto(Set<String> destino, String texto) {
        if (texto != null && !texto.isBlank()) {
            destino.add(texto.trim());
        }
    }

    private List<PerfilArma40k> filtrarArmas(List<PerfilArma40k> armas, Set<String> equipo) {
        if (armas == null || armas.isEmpty() || equipo.isEmpty()) {
            return armas == null ? List.of() : armas;
        }

        List<PerfilArma40k> resultado = armas.stream()
                .filter(arma -> {
                    String nombreArma = normalizar(arma.nombre()).replaceAll("\\s*\\(x\\d+\\)$", "");
                    return equipo.stream()
                            .map(this::normalizar)
                            .anyMatch(elemento -> elemento.contains(nombreArma) || nombreArma.contains(elemento));
                })
                .toList();
        return resultado.isEmpty() ? armas : resultado;
    }

    private Set<String> separarEquipamiento(String equipamiento) {
        Set<String> resultado = new LinkedHashSet<>();
        for (String elemento : equipamiento.split(",")) {
            agregarTexto(resultado, elemento);
        }
        return resultado;
    }

    private String valorEstadistica(List<Estadistica40k> estadisticas, String... nombres) {
        if (estadisticas == null) {
            return "-";
        }
        for (String nombre : nombres) {
            for (Estadistica40k estadistica : estadisticas) {
                if (normalizar(estadistica.nombre()).equals(normalizar(nombre))) {
                    return textoPorDefecto(estadistica.valor(), "-");
                }
            }
        }
        return "-";
    }

    private String resumenComposicion(List<FilaComposicion> composicion) {
        if (composicion.isEmpty()) {
            return "Sin configuracion";
        }
        List<String> modelos = new ArrayList<>();
        for (FilaComposicion fila : composicion) {
            modelos.add(fila.modelo());
        }
        return String.join(", ", modelos);
    }

    private String unirNoVacios(String... valores) {
        List<String> resultado = new ArrayList<>();
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                resultado.add(valor.trim());
            }
        }
        return resultado.isEmpty() ? "Sin palabras clave" : String.join(", ", resultado);
    }

    private String crearNombreArchivo(String nombreLista) {
        String base = normalizar(nombreLista)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return (base.isBlank() ? "lista-40k" : base) + ".pdf";
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinAcentos.toLowerCase(Locale.ROOT).trim();
    }

    private String textoPorDefecto(String texto, String valorPorDefecto) {
        return texto == null || texto.isBlank() ? valorPorDefecto : texto.trim();
    }

    public static final class PdfGenerado {
        private final String nombreArchivo;
        private final byte[] contenido;

        public PdfGenerado(String nombreArchivo, byte[] contenido) {
            this.nombreArchivo = nombreArchivo;
            this.contenido = contenido;
        }

        public String nombreArchivo() {
            return nombreArchivo;
        }

        public byte[] contenido() {
            return contenido;
        }
    }

    private record UnidadPdf(
            String nombre,
            String roles,
            int puntos,
            String categoria,
            String notas,
            List<FilaComposicion> composicion,
            Set<String> equipamiento,
            Unidad40k datosCatalogo
    ) {
    }

    private record FilaComposicion(String modelo, String equipamiento) {
    }

    private record ComposicionAcumulada(String modelo, String equipamiento, int cantidad) {
    }

    private static final class PdfLienzo {
        private static final PDRectangle PAGINA = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        private static final float MARGEN = 36;
        private static final float ANCHO = PAGINA.getWidth() - (MARGEN * 2);
        private static final float TAMANO_TEXTO = 8.5f;
        private static final float INTERLINEADO = 10.5f;
        private static final Color COLOR_CABECERA = new Color(112, 130, 145);
        private static final Color COLOR_SECCION = new Color(222, 222, 222);
        private static final Color COLOR_LINEA = new Color(151, 164, 175);

        private final PDDocument documento;
        private final PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream contenido;
        private float y;
        private String unidadActual;
        private int puntosUnidadActual;

        private PdfLienzo(PDDocument documento) {
            this.documento = documento;
        }

        private void nuevaPaginaResumen() throws IOException {
            unidadActual = null;
            nuevaPagina();
        }

        private void nuevaUnidad(String nombre, int puntos) throws IOException {
            unidadActual = nombre;
            puntosUnidadActual = puntos;
            nuevaPagina();
            cabeceraUnidad(false);
        }

        private void nuevaPagina() throws IOException {
            cerrarContenido();
            PDPage pagina = new PDPage(PAGINA);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = PAGINA.getHeight() - MARGEN;
        }

        private void cabeceraUnidad(boolean continuacion) throws IOException {
            float alto = 18;
            contenido.setNonStrokingColor(COLOR_CABECERA);
            contenido.addRect(MARGEN, y - alto, ANCHO, alto);
            contenido.fill();
            textoEn(MARGEN + 10, y - 13, puntosUnidadActual + " PTS", 9, negrita, Color.WHITE);
            String titulo = unidadActual.toUpperCase(Locale.ROOT) + (continuacion ? " - CONTINUACION" : "");
            textoCentradoEn(titulo, y - 13, 9, negrita, Color.WHITE);
            y -= alto;
        }

        private void textoCentrado(String texto, float tamano, boolean usarNegrita) throws IOException {
            PDFont fuente = usarNegrita ? negrita : normal;
            textoCentradoEn(texto, y - tamano, tamano, fuente);
            y -= tamano + 4;
        }

        private void texto(String texto, float tamano, boolean usarNegrita) throws IOException {
            PDFont fuente = usarNegrita ? negrita : normal;
            List<String> lineas = envolver(texto, fuente, tamano, ANCHO);
            asegurarEspacio((lineas.size() * (tamano + 2)) + 2);
            for (String linea : lineas) {
                textoEn(MARGEN, y - tamano, linea, tamano, fuente);
                y -= tamano + 2;
            }
        }

        private void saltar(float espacio) {
            y -= espacio;
        }

        private void seccion(String titulo) throws IOException {
            asegurarEspacio(31);
            float alto = 16;
            contenido.setNonStrokingColor(COLOR_SECCION);
            contenido.addRect(MARGEN, y - alto, ANCHO, alto);
            contenido.fill();
            contenido.setStrokingColor(COLOR_LINEA);
            contenido.addRect(MARGEN, y - alto, ANCHO, alto);
            contenido.stroke();
            contenido.setNonStrokingColor(Color.DARK_GRAY);
            textoEn(MARGEN + 5, y - 11.5f, titulo, 9, negrita);
            y -= alto;
        }

        private void tabla(
                List<String> cabeceras,
                float[] proporciones,
                List<List<String>> filas
        ) throws IOException {
            float[] anchos = calcularAnchos(proporciones);
            dibujarFila(cabeceras, anchos, true);
            for (List<String> fila : filas) {
                float alto = calcularAltoFila(fila, anchos);
                if (y - alto < MARGEN) {
                    continuarUnidad();
                    dibujarFila(cabeceras, anchos, true);
                }
                dibujarFila(fila, anchos, false);
            }
            y -= 5;
        }

        private void parClaveValor(String clave, String valor) throws IOException {
            tabla(
                    List.of("CAMPO", "DETALLE"),
                    new float[]{0.16f, 0.84f},
                    List.of(List.of(clave, valor))
            );
        }

        private void dibujarFila(List<String> celdas, float[] anchos, boolean cabecera) throws IOException {
            float alto = calcularAltoFila(celdas, anchos);
            asegurarEspacio(alto);
            float x = MARGEN;

            if (cabecera) {
                contenido.setNonStrokingColor(COLOR_SECCION);
                contenido.addRect(x, y - alto, ANCHO, alto);
                contenido.fill();
            }

            contenido.setStrokingColor(COLOR_LINEA);
            contenido.addRect(x, y - alto, ANCHO, alto);
            contenido.stroke();

            for (int indice = 0; indice < celdas.size(); indice++) {
                if (indice > 0) {
                    contenido.moveTo(x, y);
                    contenido.lineTo(x, y - alto);
                    contenido.stroke();
                }
                PDFont fuente = cabecera ? negrita : normal;
                List<String> lineas = envolver(
                        indice < celdas.size() ? celdas.get(indice) : "",
                        fuente,
                        TAMANO_TEXTO,
                        anchos[indice] - 8
                );
                float lineaY = y - 4 - TAMANO_TEXTO;
                for (String linea : lineas) {
                    textoEn(x + 4, lineaY, linea, TAMANO_TEXTO, fuente);
                    lineaY -= INTERLINEADO;
                }
                x += anchos[indice];
            }
            y -= alto;
        }

        private float calcularAltoFila(List<String> celdas, float[] anchos) throws IOException {
            int maximoLineas = 1;
            for (int indice = 0; indice < celdas.size(); indice++) {
                int lineas = envolver(celdas.get(indice), normal, TAMANO_TEXTO, anchos[indice] - 8).size();
                maximoLineas = Math.max(maximoLineas, lineas);
            }
            return Math.max(17, (maximoLineas * INTERLINEADO) + 7);
        }

        private float[] calcularAnchos(float[] proporciones) {
            float[] anchos = new float[proporciones.length];
            float acumulado = 0;
            for (int indice = 0; indice < proporciones.length; indice++) {
                anchos[indice] = indice == proporciones.length - 1
                        ? ANCHO - acumulado
                        : ANCHO * proporciones[indice];
                acumulado += anchos[indice];
            }
            return anchos;
        }

        private void asegurarEspacio(float alto) throws IOException {
            if (y - alto >= MARGEN) {
                return;
            }
            continuarUnidad();
        }

        private void continuarUnidad() throws IOException {
            nuevaPagina();
            if (unidadActual != null) {
                cabeceraUnidad(true);
            }
        }

        private List<String> envolver(String texto, PDFont fuente, float tamano, float anchoMaximo) throws IOException {
            String valor = texto == null || texto.isBlank() ? "-" : limpiarTexto(texto);
            List<String> lineas = new ArrayList<>();
            for (String parrafo : valor.split("\\R", -1)) {
                String[] palabras = parrafo.split("\\s+");
                StringBuilder linea = new StringBuilder();
                for (String palabra : palabras) {
                    String candidata = linea.isEmpty() ? palabra : linea + " " + palabra;
                    if (anchoTexto(candidata, fuente, tamano) <= anchoMaximo) {
                        linea.setLength(0);
                        linea.append(candidata);
                    } else if (!linea.isEmpty()) {
                        lineas.add(linea.toString());
                        linea.setLength(0);
                        linea.append(palabra);
                    } else {
                        lineas.addAll(partirPalabra(palabra, fuente, tamano, anchoMaximo));
                    }
                }
                if (!linea.isEmpty()) {
                    lineas.add(linea.toString());
                }
            }
            return lineas.isEmpty() ? List.of("-") : lineas;
        }

        private List<String> partirPalabra(String palabra, PDFont fuente, float tamano, float anchoMaximo) throws IOException {
            List<String> partes = new ArrayList<>();
            StringBuilder parte = new StringBuilder();
            for (char caracter : palabra.toCharArray()) {
                String candidata = parte.toString() + caracter;
                if (!parte.isEmpty() && anchoTexto(candidata, fuente, tamano) > anchoMaximo) {
                    partes.add(parte.toString());
                    parte.setLength(0);
                }
                parte.append(caracter);
            }
            if (!parte.isEmpty()) {
                partes.add(parte.toString());
            }
            return partes;
        }

        private float anchoTexto(String texto, PDFont fuente, float tamano) throws IOException {
            return fuente.getStringWidth(limpiarTexto(texto)) / 1000f * tamano;
        }

        private void textoEn(float x, float yTexto, String texto, float tamano, PDFont fuente) throws IOException {
            textoEn(x, yTexto, texto, tamano, fuente, Color.DARK_GRAY);
        }

        private void textoEn(
                float x,
                float yTexto,
                String texto,
                float tamano,
                PDFont fuente,
                Color color
        ) throws IOException {
            contenido.beginText();
            contenido.setFont(fuente, tamano);
            contenido.setNonStrokingColor(color);
            contenido.newLineAtOffset(x, yTexto);
            contenido.showText(limpiarTexto(texto));
            contenido.endText();
        }

        private void textoCentradoEn(String texto, float yTexto, float tamano, PDFont fuente) throws IOException {
            textoCentradoEn(texto, yTexto, tamano, fuente, Color.DARK_GRAY);
        }

        private void textoCentradoEn(
                String texto,
                float yTexto,
                float tamano,
                PDFont fuente,
                Color color
        ) throws IOException {
            float anchoTexto = anchoTexto(texto, fuente, tamano);
            textoEn((PAGINA.getWidth() - anchoTexto) / 2, yTexto, texto, tamano, fuente, color);
        }

        private String limpiarTexto(String texto) {
            return texto
                    .replace('\u2018', '\'')
                    .replace('\u2019', '\'')
                    .replace('\u201C', '"')
                    .replace('\u201D', '"')
                    .replace('\u2013', '-')
                    .replace('\u2014', '-')
                    .replace('\u00A0', ' ')
                    .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
        }

        private void cerrar() throws IOException {
            cerrarContenido();
        }

        private void cerrarContenido() throws IOException {
            if (contenido != null) {
                contenido.close();
                contenido = null;
            }
        }
    }
}
