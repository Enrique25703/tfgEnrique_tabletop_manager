package org.example.tfgenrique.service.catalogoAos;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogoAosServiceTest {

    private final CatalogoAosService service = new CatalogoAosService();

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
