package org.example.tfgenrique.service.catalogos;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ClasificacionUnidadTest {
    @Test
    void reconoceLegendsDelEnlaceSinConfundirUnaBibliotecaPropiaConAliados() throws Exception {
        Element unidad = entrada("Imperium - Astra Militarum - Library", "<selectionEntry name='Capitán' />");
        Element enlace = entrada("Imperium - Astra Militarum", "<entryLink name='Capitán [Legends]' />");
        assertEquals(new ClasificacionUnidad(true, false, false),
                ClasificacionUnidad.leer(unidad, enlace, "Imperium - Astra Militarum"));
    }

    @Test
    void unCaballeroEsAliadoParaAstraPeroPropioParaSuEjercito() throws Exception {
        Element unidad = entrada("Imperium - Imperial Knights - Library", "<selectionEntry name='Knight' />");
        assertTrue(ClasificacionUnidad.leer(unidad, null, "Imperium - Astra Militarum").aliado());
        assertFalse(ClasificacionUnidad.leer(unidad, null, "Imperium - Imperial Knights").aliado());
    }

    @Test
    void losMarinesCompartidosSonPropiosDeLosCapitulos() throws Exception {
        Element unidad = entrada("Imperium - Adeptus Astartes - Space Marines", "<selectionEntry name='Intercessors' />");
        assertFalse(ClasificacionUnidad.leer(unidad, null, "Imperium - Adeptus Astartes - Blood Angels").aliado());
        Element demonio = entrada("Chaos - Daemons Library", "<selectionEntry name='Bloodletters' />");
        assertFalse(ClasificacionUnidad.leer(demonio, null, "Chaos - Chaos Daemons").aliado());
        assertTrue(ClasificacionUnidad.leer(demonio, null, "Chaos - Death Guard").aliado());
    }

    @Test
    void distingueTerrenoDeFaccionYFortificacionesDeUnidadesConOpcionesLegends() throws Exception {
        for (String categoria : new String[]{"Fortification", "Faction Terrain", "Estructura"}) {
            Element unidad = entrada("Prueba - Library", "<selectionEntry name='Bastión'><categoryLinks>"
                    + "<categoryLink name='" + categoria + "'/></categoryLinks></selectionEntry>");
            assertTrue(ClasificacionUnidad.leer(unidad, null, "Prueba").estructura());
        }
        Element unidad = entrada("Prueba", """
                <selectionEntry name='Guardia'><selectionEntries><selectionEntry name='Opción Legends'>
                <categoryLinks><categoryLink name='Legends'/><categoryLink name='Fortification'/></categoryLinks>
                </selectionEntry></selectionEntries></selectionEntry>
                """);
        assertEquals(ClasificacionUnidad.normal(), ClasificacionUnidad.leer(unidad, null, "Prueba"));
    }

    @Test
    void reconoceRegimientosAliadosSinMarcarLasBibliotecasDeReglas() throws Exception {
        Element unidad = entrada("۞ Regiments of Renown", "<selectionEntry name='Regiment of Renown: Prueba'/>");
        assertTrue(ClasificacionUnidad.leer(unidad, null, "Stormcast Eternals").aliado());
        Element manifestacion = entrada("❖ Lores", "<selectionEntry name='Manifestación'/>");
        assertFalse(ClasificacionUnidad.leer(manifestacion, null, "Stormcast Eternals").aliado());
    }

    @Test
    void reconoceCategoriasDelEnlaceYCatalogosLegends() throws Exception {
        Element unidad = entrada("Prueba - Library", "<selectionEntry name='Guardia'/>");
        Element enlace = entrada("Prueba", "<entryLink name='Guardia'><categoryLinks><categoryLink name='Legends'/></categoryLinks></entryLink>");
        assertTrue(ClasificacionUnidad.leer(unidad, enlace, "Prueba").legends());
        Element antiguo = entrada("Library - Astartes Heresy Legends", "<selectionEntry name='Dreadnought'/>");
        assertEquals(new ClasificacionUnidad(true, false, false),
                ClasificacionUnidad.leer(antiguo, null, "Imperium - Adeptus Astartes - Blood Angels"));
    }

    private Element entrada(String catalogo, String xml) throws Exception {
        var fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        String contenido = "<catalogue xmlns='http://www.battlescribe.net/schema/catalogueSchema' name='"
                + catalogo + "'>" + xml + "</catalogue>";
        return (Element) fabrica.newDocumentBuilder().parse(new ByteArrayInputStream(
                contenido.getBytes(StandardCharsets.UTF_8))).getDocumentElement().getFirstChild();
    }
}
