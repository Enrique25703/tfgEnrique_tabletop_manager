package org.example.tfgenrique.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
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
        ListaPdfService servicio = new ListaPdfService(creacionListasService, catalogo40kService);

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
                                  "equipamientoFijo": ["Lasgun"],
                                  "gruposEquipamiento": [],
                                  "instancias": [
                                    {"selecciones": {}},
                                    {"selecciones": {}}
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
                ))),
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
            assertThat(documento.getNumberOfPages()).isEqualTo(2);
            assertThat(documento.getPage(0).getMediaBox().getWidth())
                    .isGreaterThan(documento.getPage(0).getMediaBox().getHeight());
        }
    }
}
