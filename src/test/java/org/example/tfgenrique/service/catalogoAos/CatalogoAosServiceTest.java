package org.example.tfgenrique.service.catalogoAos;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CatalogoAosServiceTest {

    private final CatalogoAosService service = new CatalogoAosService();

    @Test
    void conservaTildesYClasificaLasCategoriasDelEnlace() throws Exception {
        var salida = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(salida)) {
            zip.putNextEntry(new ZipEntry("Ejército.cat"));
            zip.write("""
                    <catalogue xmlns="http://www.battlescribe.net/schema/catalogueSchema" id="ej" name="Ejército">
                      <entryLinks><entryLink id="enlace" name="Bastión" type="selectionEntry" targetId="unidad">
                        <categoryLinks><categoryLink name="Legends"/></categoryLinks>
                      </entryLink></entryLinks>
                    </catalogue>
                    """.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("Ejército - Library.cat"));
            zip.write("""
                    <catalogue xmlns="http://www.battlescribe.net/schema/catalogueSchema" id="lib" name="Ejército - Library" library="true">
                      <sharedSelectionEntries><selectionEntry id="unidad" name="Bastión del cañón" type="unit">
                        <categoryLinks><categoryLink name="Faction Terrain"/></categoryLinks>
                      </selectionEntry></sharedSelectionEntries>
                    </catalogue>
                    """.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        var catalogo = service.leerCatalogo(new ByteArrayInputStream(salida.toByteArray()));
        var unidad = catalogo.buscarUnidad("Ejército", "Ejército", "Bastión del cañón");
        assertNotNull(unidad);
        assertTrue(unidad.clasificacion().legends());
        assertTrue(unidad.clasificacion().estructura());
        assertFalse(unidad.clasificacion().aliado());
        var pagina = service.prepararPaginaCatalogo(catalogo, "Ejército", "Ejército", null);
        assertEquals(unidad.clasificacion(), pagina.ejercito().unidades().get(0).clasificacion());
    }

    @Test
    void leeElCatalogoRealSiSeIndicaSuRuta() throws Exception {
        String ruta = System.getProperty("catalogoAos.zip");
        assumeTrue(ruta != null && !ruta.isBlank());
        try (var flujo = Files.newInputStream(Path.of(ruta))) {
            var catalogo = service.leerCatalogo(flujo);
            var ejercito = catalogo.buscarEjercito("Stormcast Eternals", "Stormcast Eternals");
            assertNotNull(ejercito);
            assertTrue(ejercito.unidades().stream().anyMatch(u -> u.clasificacion().legends()));
            assertTrue(ejercito.unidades().stream().anyMatch(u -> !u.clasificacion().aliado()));
        }
    }

    @Test
    void usaLosPuntosDelEntryLinkDeLaFaccion() throws Exception {
        Element unidadCompartida = elemento("""
                <selectionEntry xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                    id="unidad" name="Neave Blacktalon" type="unit"/>
                """);
        Element enlaceFaccion = elemento("""
                <entryLink xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                    targetId="unidad" name="Neave Blacktalon" type="selectionEntry">
                  <costs><cost name="pts" typeId="points" value="280"/></costs>
                </entryLink>
                """);

        assertEquals(List.of(280), service.resolverPuntosUnidad(unidadCompartida, enlaceFaccion));
    }

    @Test
    void conservaElCosteDirectoSiElEnlaceNoLoSobrescribe() throws Exception {
        Element unidad = elemento("""
                <selectionEntry xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                    id="unidad" name="Unidad" type="unit">
                  <costs><cost name="pts" typeId="points" value="150"/></costs>
                </selectionEntry>
                """);
        Element enlaceSinCoste = elemento("""
                <entryLink xmlns="http://www.battlescribe.net/schema/catalogueSchema"
                    targetId="unidad" name="Unidad" type="selectionEntry"/>
                """);

        assertEquals(List.of(150), service.resolverPuntosUnidad(unidad, enlaceSinCoste));
    }

    private Element elemento(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))
                .getDocumentElement();
    }
}
