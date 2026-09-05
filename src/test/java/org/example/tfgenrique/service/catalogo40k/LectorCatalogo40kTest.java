package org.example.tfgenrique.service.catalogo40k;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Ejercito40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Unidad40k;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
class LectorCatalogo40kTest {
    private final LectorCatalogo40k lector = new LectorCatalogo40k();

    @Test
    void leeEjercitoJsonConUnidadesDefinidasEnUnaBiblioteca() throws Exception {
        String ejercitoJson = """
                {
                  "catalogue": {
                    "entryLinks": [{
                      "name": "Escuadra de prueba",
                      "hidden": false,
                      "type": "selectionEntry",
                      "id": "enlace-unidad",
                      "targetId": "unidad-1"
                    }],
                    "catalogueLinks": [{
                      "name": "Imperium - Prueba - Library",
                      "targetId": "biblioteca-1",
                      "importRootEntries": false
                    }],
                    "library": false,
                    "id": "ejercito-1",
                    "name": "Imperium - Prueba"
                  }
                }
                """;
        String bibliotecaJson = """
                {
                  "catalogue": {
                    "sharedSelectionEntries": [{
                      "categoryLinks": [
                        {"name": "Faction: Prueba", "id": "faccion-1"},
                        {"name": "Infantry", "id": "rol-1"}
                      ],
                      "costs": [{"name": "pts", "typeId": "puntos", "value": 95}],
                      "profiles": [
                        {
                          "characteristics": [
                            {"name": "M", "typeId": "m", "$text": "6 pulgadas"},
                            {"name": "T", "typeId": "t", "$text": "4"}
                          ],
                          "name": "Escuadra de prueba",
                          "typeName": "Unit",
                          "id": "perfil-1"
                        },
                        {
                          "characteristics": [{
                            "name": "Description",
                            "typeId": "descripcion",
                            "$text": "Habilidad importada desde JSON"
                          }],
                          "name": "Disciplina",
                          "typeName": "Abilities",
                          "id": "habilidad-1"
                        }
                      ],
                      "type": "unit",
                      "name": "Escuadra de prueba",
                      "hidden": false,
                      "id": "unidad-1"
                    }],
                    "library": true,
                    "id": "biblioteca-1",
                    "name": "Imperium - Prueba - Library"
                  }
                }
                """;

        Catalogo40kData catalogo = lector.leerCatalogo(crearZip(
                new ArchivoJson("Imperium - Prueba.json", ejercitoJson),
                new ArchivoJson("Imperium - Prueba - Library.json", bibliotecaJson)
        ));

        Ejercito40k ejercito = catalogo.facciones().get("Imperium").get("Prueba");
        assertNotNull(ejercito);
        assertEquals(1, ejercito.unidades().size());
        Unidad40k unidad = ejercito.unidades().get(0);
        assertEquals("Escuadra de prueba", unidad.nombre());
        assertEquals("95", unidad.puntos());
        assertEquals("Infantry", unidad.roles());
        assertFalse(unidad.estadisticas().isEmpty());
        assertEquals("Description: Habilidad importada desde JSON", unidad.habilidadesDetalle().get(0).descripcion());
    }

    @Test
    void rechazaZipSinCatalogosCompatibles() {
        assertThrows(IllegalArgumentException.class, () ->
                lector.leerCatalogo(crearZip(new ArchivoJson("README.txt", "sin catalogos")))
        );
    }

    @Test
    void resuelveArmasDeEquipoYPerfilesCompartidosSinDuplicadosNiCiclos() throws Exception {
        String ejercito = """
                {"catalogue":{"id":"ejercito","name":"Imperium - Prueba",
                  "entryLinks":[{"id":"u-link","type":"selectionEntry","targetId":"u"}],
                  "catalogueLinks":[{"targetId":"lib"}]}}
                """;
        String biblioteca = """
                {"catalogue":{"id":"lib","name":"Imperium - Prueba - Library","library":true,
                  "sharedSelectionEntries":[
                    {"id":"u","name":"Unidad","type":"unit",
                     "profiles":[{"id":"espada","name":"Espada","typeName":"Melee Weapons",
                       "characteristics":[{"name":"Range","$text":"Melee"},{"name":"WS","$text":"3+"}]}],
                     "entryLinks":[{"type":"selectionEntryGroup","targetId":"grupo"}],
                     "infoLinks":[{"type":"profile","targetId":"rifle"}]},
                    {"id":"equipo","name":"Equipo","type":"upgrade",
                     "infoLinks":[{"type":"profile","targetId":"rifle"}],
                     "entryLinks":[{"type":"selectionEntryGroup","targetId":"grupo"}]}],
                  "sharedSelectionEntryGroups":[{"id":"grupo","name":"Armas",
                    "entryLinks":[{"type":"selectionEntry","targetId":"equipo"}]}],
                  "sharedProfiles":[
                    {"id":"rifle","name":"Rifle","typeName":"Ranged Weapons",
                     "characteristics":[{"name":"Range","$text":"24 pulgadas"},
                       {"name":"A","$text":"D6"},{"name":"BS","$text":"4+"},
                       {"name":"S","$text":"5"},{"name":"AP","$text":"-1"},{"name":"D","$text":"2"}]},
                    {"id":"ajena","name":"Arma de otra unidad","typeName":"Ranged Weapons"}]}}
                """;
        var catalogo = lector.leerCatalogo(crearZip(new ArchivoJson("ejercito.json", ejercito),
                new ArchivoJson("biblioteca.json", biblioteca)));
        var unidad = catalogo.buscarUnidad("Imperium", "Prueba", "Unidad");
        assertEquals(2, unidad.armasDetalle().size());
        var vista = new Catalogo40kService().prepararInfoUnidad("Imperium", "Prueba", unidad);
        assertEquals("Rifle", vista.armasDistancia().get(0).nombre());
        assertEquals("D6", vista.armasDistancia().get(0).ataques());
        assertEquals("-1", vista.armasDistancia().get(0).penetracion());
        assertEquals("Espada", vista.armasCuerpoACuerpo().get(0).nombre());
    }

    @Test
    void leeElCatalogoRealSiSeIndicaSuRuta() throws Exception {
        String rutaCatalogo = System.getProperty("catalogo40k.zip");
        assumeTrue(rutaCatalogo != null && !rutaCatalogo.isBlank());

        Catalogo40kData catalogo;
        try (var entrada = Files.newInputStream(Path.of(rutaCatalogo))) {
            catalogo = lector.leerCatalogo(entrada);
        }

        Ejercito40k astraMilitarum = catalogo.facciones().get("Imperium").get("Astra Militarum");
        assertNotNull(astraMilitarum);
        assertFalse(astraMilitarum.unidades().isEmpty());
        assertFalse(astraMilitarum.unidades().get(0).perfilesDetalle().isEmpty());
    }

    @Test
    void cargaDestacamentosDeLaBibliotecaDelEjercitoSeleccionado() throws Exception {
        String ejercito = """
                {"catalogue":{"id":"ejercito","name":"Imperium - Prueba",
                  "catalogueLinks":[{"targetId":"lib"}]}}
                """;
        String biblioteca = """
                {"catalogue":{"id":"lib","name":"Imperium - Prueba - Library","library":true,
                  "sharedSelectionEntryGroups":[{"name":"Detachments","selectionEntries":[
                    {"id":"dest-1","name":"Destacamento de prueba",
                     "costs":[{"name":"Detachment Points","value":2}],
                     "categoryLinks":[{"name":"Take and Hold"}],
                     "rules":[{"name":"Disciplina","description":"Regla de prueba"}]},
                    {"id":"oculto","name":"No seleccionable","hidden":true}]}]}}
                """;
        var catalogo = lector.leerCatalogo(crearZip(new ArchivoJson("ejercito.json", ejercito),
                new ArchivoJson("biblioteca.json", biblioteca)));
        var destacamentos = catalogo.buscarReglasEjercito("Imperium", "Prueba").destacamentos();
        assertEquals(1, destacamentos.size());
        assertEquals("dest-1", destacamentos.get(0).id());
        assertEquals(2, destacamentos.get(0).puntosDestacamento());
        assertEquals(java.util.List.of("Take and Hold"), destacamentos.get(0).disposiciones());
        assertTrue(catalogo.buscarReglasEjercito("Otra faccion", "Otro ejercito").destacamentos().isEmpty());
    }

    private ByteArrayInputStream crearZip(ArchivoJson... archivos) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(salida, StandardCharsets.UTF_8)) {
            for (ArchivoJson archivo : archivos) {
                zip.putNextEntry(new ZipEntry(archivo.nombre()));
                zip.write(archivo.contenido().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return new ByteArrayInputStream(salida.toByteArray());
    }

    private record ArchivoJson(String nombre, String contenido) {
    }
}
