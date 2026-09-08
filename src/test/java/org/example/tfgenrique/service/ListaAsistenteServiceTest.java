package org.example.tfgenrique.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaAsistenteServiceTest {
    private final ListaAsistenteService servicio = new ListaAsistenteService();

    @Test
    void analizaUnidadesRealesEIgnoraElementosEspeciales() {
        var resultado = servicio.analizar("""
                {"faccion":"Imperium","ejercito":"Prueba","limitePuntos":2000,"unidades":[
                  {"nombre":"Capitan","puntosBase":120,"character":true,"warlord":true,
                   "roles":"Character, Infantry","palabrasClave":"Fly"},
                  {"nombre":"Intercessors","puntosBase":180,"battleline":true,"roles":"Infantry"},
                  {"nombre":"Intercessors","puntosBase":180,"battleline":true,"roles":"Infantry"},
                  {"nombre":"Dreadnought","puntosBase":300,"roles":"Vehicle, Walker"},
                  {"nombre":"Destacamentos","tipoEspecial":"destacamentos","puntosBase":0,
                   "configuracionMiniaturas":{"seleccionados":["gladius"]}}
                ]}
                """, "¿Qué cambiarías?");

        assertEquals(4, resultado.totalUnidades());
        assertEquals(780, resultado.puntosTotales());
        assertTrue(resultado.resumen().contains("Imperium Prueba"));
        assertTrue(resultado.fortalezas().stream().anyMatch(texto -> texto.contains("Warlord")));
        assertTrue(resultado.fortalezas().stream().anyMatch(texto -> texto.contains("Battleline")));
        assertTrue(resultado.fortalezas().stream().anyMatch(texto -> texto.contains("destacamento")));
        assertTrue(resultado.recomendaciones().stream().anyMatch(texto -> texto.contains("1220")));
    }

    @Test
    void recomiendaLosElementosBasicosQueFaltan() {
        var resultado = servicio.analizar("""
                {"limitePuntos":1000,"unidades":[
                  {"nombre":"Infanteria","puntosBase":100,"roles":"Infantry"}
                ]}
                """, "");

        String recomendaciones = String.join(" ", resultado.recomendaciones());
        assertTrue(recomendaciones.contains("Character"));
        assertTrue(recomendaciones.contains("Warlord"));
        assertTrue(recomendaciones.contains("Battleline"));
        assertTrue(recomendaciones.contains("destacamento"));
        assertTrue(recomendaciones.contains("moviles"));
    }

    @Test
    void respondeConOrientacionCuandoLaListaEstaVacia() {
        var resultado = servicio.analizar("{\"limitePuntos\":2000,\"unidades\":[]}", null);
        assertEquals(0, resultado.totalUnidades());
        assertTrue(resultado.resumen().contains("no hay unidades"));
    }

    @Test
    void recomiendaUnidadesParaDisrupcionYArmasContraObjetivosResistentes() {
        var resultado = servicio.analizar("""
                {"limitePuntos":2000,"unidades":[
                  {"nombre":"Cadian Recon Squad","puntosBase":80,"battleline":true,
                   "roles":"Infantry","palabrasClave":"Scout, Infiltrator",
                   "perfilesArmas":[
                     {"nombre":"Lascannon","tipo":"Ranged Weapon","rango":"48 pulgadas","ataques":"1","fuerza":"12","penetracion":"-3","dano":"D6+1"},
                     {"nombre":"Chainsword","tipo":"Melee Weapon","rango":"Melee","ataques":"4","fuerza":"4","penetracion":"0","dano":"1"}
                   ]},
                  {"nombre":"Leman Russ","puntosBase":180,"roles":"Vehicle"},
                  {"nombre":"Disposición","tipoEspecial":"disposicion","puntosBase":0,
                   "configuracionMiniaturas":{"seleccionados":["Disruption"]}}
                ]}
                """, "¿Qué armas uso contra un ejército de unidades resistentes?");

        String recomendaciones = String.join(" ", resultado.recomendaciones());
        assertTrue(recomendaciones.contains("Disruption"));
        assertTrue(recomendaciones.contains("Cadian Recon Squad"));
        assertTrue(recomendaciones.contains("Lascannon"));
        assertFalse(recomendaciones.contains("Chainsword"));
    }

    @Test
    void recomiendaSaturacionContraMuchasMiniaturasYUnidadesParaPriorityAssets() {
        var resultado = servicio.analizar("""
                {"limitePuntos":1000,"unidades":[
                  {"nombre":"Infantry Squad","puntosBase":60,"battleline":true,"roles":"Infantry",
                   "perfilesArmas":[
                     {"nombre":"Flamer","tipo":"Ranged Weapon","rango":"12 pulgadas","ataques":"D6+3","fuerza":"4","penetracion":"0","dano":"1"},
                     {"nombre":"Meltagun","tipo":"Ranged Weapon","rango":"12 pulgadas","ataques":"1","fuerza":"9","penetracion":"-4","dano":"D6"}
                   ]},
                  {"nombre":"Priority Assets","tipoEspecial":"disposicion","puntosBase":0,
                   "configuracionMiniaturas":{"seleccionados":["Priority Assets"]}}
                ]}
                """, "¿Cómo juego contra una horda con muchas miniaturas de pocas heridas?");

        String recomendaciones = String.join(" ", resultado.recomendaciones());
        assertTrue(recomendaciones.contains("Priority Assets"));
        assertTrue(recomendaciones.contains("Infantry Squad"));
        assertTrue(recomendaciones.contains("Flamer"));
        assertFalse(recomendaciones.contains("Meltagun"));
    }

    @Test
    void rechazaEntradasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> servicio.analizar("", ""));
        assertThrows(IllegalArgumentException.class, () -> servicio.analizar("[]", ""));
        assertThrows(IllegalArgumentException.class, () -> servicio.analizar("{mal json", ""));
        assertThrows(IllegalArgumentException.class, () -> servicio.analizar(
                "{\"unidades\":[]}", "x".repeat(301)));
    }
}
