package org.example.tfgenrique.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService;
import org.example.tfgenrique.service.CreacionListasService.ListaExportacionView;
import org.example.tfgenrique.service.ListaPdfService.PdfGenerado;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Ejercito40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Estadistica40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Habilidad40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilArma40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilUnidad40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Unidad40k;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListaPdfServiceTest {

    @Test
    void generaResumenYFichaDeUnidad() throws Exception {
        CreacionListasService creacionListasService = mock(CreacionListasService.class);
        Catalogo40kService catalogo40kService = mock(Catalogo40kService.class);
        ListaPdfService servicio = new ListaPdfService(creacionListasService, catalogo40kService, mock(CatalogoAosService.class));

        ListaExportacionView lista = new ListaExportacionView(
                7L,
                "Patrulla de prueba",
                "WH40K_11",
                "Imperium - Astra Militarum",
                "Astra Militarum",
                100,
                1000,
                1,
                """
                        {
                          "unidades": [{
                            "nombre": "Escuadra de prueba",
                            "roles": "Infantry",
                            "puntosBase": 100,
                            "categoria": "linea",
                            "notas": "Mantener en cobertura.",
                            "configuracionMiniaturas": {
                              "opcionSeleccionadaId": "",
                              "opcionesComposicion": [],
                              "gruposMiniaturas": [{
                                "modelos": [{
                                  "nombre": "Guardia",
                                  "equipamientoFijo": [],
                                  "gruposEquipamiento": [{
                                    "id": "principal",
                                    "opciones": [
                                      {"id": "lasgun", "nombre": "Lasgun"},
                                      {"id": "bolter", "nombre": "Bolter"}
                                    ]
                                  }],
                                  "instancias": [
                                    {"selecciones": {"principal": ["lasgun"]}},
                                    {"selecciones": {"principal": ["lasgun"]}}
                                  ]
                                }],
                                "subgrupos": []
                              }]
                            }
                          }]
                        }
                        """
        );

        Unidad40k unidad = new Unidad40k(
                "Escuadra de prueba",
                "100",
                "Infantry",
                "Astra Militarum",
                "Infantry, Grenades",
                "",
                "Objetivo asegurado",
                "Lasgun",
                List.of(
                        new Estadistica40k("M", "6\""),
                        new Estadistica40k("T", "3"),
                        new Estadistica40k("SV", "5+"),
                        new Estadistica40k("W", "1"),
                        new Estadistica40k("LD", "7+"),
                        new Estadistica40k("OC", "2")
                ),
                List.of(new Habilidad40k("Objetivo asegurado", "Esta unidad controla objetivos con eficacia.")),
                List.of(new PerfilUnidad40k("Guardia", List.of(
                        new Estadistica40k("M", "6\""),
                        new Estadistica40k("T", "3"),
                        new Estadistica40k("SV", "5+"),
                        new Estadistica40k("W", "1"),
                        new Estadistica40k("LD", "7+"),
                        new Estadistica40k("OC", "2")
                ))),
                List.of(new PerfilArma40k("Lasgun", "Ranged Weapons", List.of(
                        new Estadistica40k("Range", "24\""),
                        new Estadistica40k("A", "1"),
                        new Estadistica40k("BS", "4+"),
                        new Estadistica40k("S", "3"),
                        new Estadistica40k("AP", "0"),
                        new Estadistica40k("D", "1"),
                        new Estadistica40k("Keywords", "Rapid Fire 1")
                )), new PerfilArma40k("Bolter", "Ranged Weapons", List.of())),
                List.of(),
                List.of()
        );
        Catalogo40kData catalogo = new Catalogo40kData(
                Map.of(lista.faccion(), Map.of(lista.ejercito(), new Ejercito40k(
                        lista.faccion(),
                        lista.ejercito(),
                        List.of(unidad)
                ))),
                LocalDateTime.now()
        );

        when(creacionListasService.obtenerListaParaExportar("jugador", 7L)).thenReturn(lista);
        when(catalogo40kService.getData()).thenReturn(catalogo);

        PdfGenerado resultado = servicio.generarPdf("jugador", 7L);

        assertThat(resultado.nombreArchivo()).isEqualTo("patrulla-de-prueba.pdf");
        assertThat(resultado.contenido()).isNotEmpty();
        try (PDDocument documento = Loader.loadPDF(resultado.contenido())) {
            assertThat(new PDFTextStripper().getText(documento)).contains(
                    "Patrulla de prueba", "2x Guardia", "Lasgun", "24\"", "Rapid Fire 1",
                    "Objetivo asegurado", "Mantener en cobertura.", "M T SV W LD OC")
                    .doesNotContain("Bolter");
            assertThat(documento.getNumberOfPages()).isEqualTo(2);
            assertThat(documento.getPage(0).getMediaBox().getWidth())
                    .isGreaterThan(documento.getPage(0).getMediaBox().getHeight());
            var renderer = new PDFRenderer(documento);
            for (int pagina = 0; pagina < documento.getNumberOfPages(); pagina++) {
                var imagen = renderer.renderImage(pagina);
                for (int y = 0; y < imagen.getHeight(); y++) {
                    for (int x = 0; x < imagen.getWidth(); x++) {
                        int rgb = imagen.getRGB(x, y);
                        int rojo = (rgb >> 16) & 255;
                        int verde = (rgb >> 8) & 255;
                        int azul = rgb & 255;
                        if (rojo != verde || verde != azul) {
                            throw new AssertionError("El PDF contiene color en la pagina " + pagina);
                        }
                    }
                }
            }
        }
    }

    @Test
    void exportaAosYDivideLasReglasLargasSinPerderTexto() throws Exception {
        var listas = mock(CreacionListasService.class);
        var catalogoAos = mock(CatalogoAosService.class);
        var servicio = new ListaPdfService(listas, mock(Catalogo40kService.class), catalogoAos);
        var lista = new ListaExportacionView(8L, "Defensores", "AOS_4", "Order", "Stormcast",
                200, 1000, 2, """
                {"unidades":[
                  {"nombre":"Liberators","puntosBase":100},
                  {"nombre":"Liberators","puntosBase":100}
                ]}
                """);
        String descripcion = java.util.stream.IntStream.range(0, 140)
                .mapToObj(i -> "Regla numero " + i + ": mantener el objetivo.")
                .collect(java.util.stream.Collectors.joining("\n"));
        var unidad = new CatalogoAosService.Unidad40k("Liberators", "100", "Infantry", "Stormcast",
                "Order", "", "Defensores", "Warhammer", List.of(
                        new CatalogoAosService.Estadistica40k("Move", "5\""),
                        new CatalogoAosService.Estadistica40k("Health", "2")),
                List.of(new CatalogoAosService.Habilidad40k("Defensores", descripcion)), List.of(), List.of());
        var datos = new CatalogoAosService.Catalogo40kData(Map.of("Order", Map.of("Stormcast",
                new CatalogoAosService.Ejercito40k("Order", "Stormcast", List.of(unidad)))), LocalDateTime.now());
        when(listas.obtenerListaParaExportar("jugador", 8L)).thenReturn(lista);
        when(catalogoAos.getData()).thenReturn(datos);

        try (var documento = Loader.loadPDF(servicio.generarPdf("jugador", 8L).contenido())) {
            assertThat(documento.getNumberOfPages()).isGreaterThan(4);
            var lector = new PDFTextStripper() {
                @Override
                protected void processTextPosition(TextPosition texto) {
                    assertThat(texto.getYDirAdj()).isBetween(36f, documento.getPage(0).getMediaBox().getHeight() - 36);
                    assertThat(texto.getXDirAdj()).isGreaterThanOrEqualTo(36f);
                    assertThat(texto.getXDirAdj() + texto.getWidthDirAdj())
                            .isLessThanOrEqualTo(documento.getPage(0).getMediaBox().getWidth() - 36);
                    super.processTextPosition(texto);
                }
            };
            String texto = lector.getText(documento);
            assertThat(texto).contains("Age of Sigmar", "Move", "Health", "Warhammer", "CONTINUACION");
            for (int i = 0; i < 140; i++) {
                assertThat(texto.split(java.util.regex.Pattern.quote("Regla numero " + i + ":"), -1))
                        .hasSize(3); // Cada regla aparece una vez por cada unidad de la lista.
            }
        }
    }

    @Test
    void conservaLaUnidadSiYaNoEstaEnElCatalogo() throws Exception {
        var listas = mock(CreacionListasService.class);
        var catalogo = mock(Catalogo40kService.class);
        var servicio = new ListaPdfService(listas, catalogo, mock(CatalogoAosService.class));
        when(listas.obtenerListaParaExportar("jugador", 9L)).thenReturn(new ListaExportacionView(
                9L, "Veteranos", "WH40K_11", "Imperium", "Astra Militarum", 100, 1000, 1,
                """
                {"unidades":[{"nombre":"Escuadra retirada","puntosBase":100,"notas":"Mi unidad"}]}
                """));
        when(catalogo.getData()).thenReturn(new Catalogo40kData(Map.of(), LocalDateTime.now()));

        try (var documento = Loader.loadPDF(servicio.generarPdf("jugador", 9L).contenido())) {
            assertThat(new PDFTextStripper().getText(documento)).contains(
                    "Escuadra retirada", "No se ha encontrado esta unidad en el catalogo actual.", "Mi unidad");
        }
    }
}
