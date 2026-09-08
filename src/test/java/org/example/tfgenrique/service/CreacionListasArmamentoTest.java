package org.example.tfgenrique.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreacionListasArmamentoTest {

    @Test
    void incluyePerfilesNormalizadosDeArmasEnElCreador() throws Exception {
        var arma = new Catalogo40kService.PerfilArma40k(
                "Lascannon",
                "Ranged Weapon",
                List.of(
                        new Catalogo40kService.Estadistica40k("Range", "48\""),
                        new Catalogo40kService.Estadistica40k("A", "1"),
                        new Catalogo40kService.Estadistica40k("BS", "4+"),
                        new Catalogo40kService.Estadistica40k("S", "12"),
                        new Catalogo40kService.Estadistica40k("AP", "-3"),
                        new Catalogo40kService.Estadistica40k("D", "D6+1")
                )
        );
        var unidad = new Catalogo40kService.Unidad40k(
                "Heavy Weapons Squad", "60", "Infantry", "Imperium", "Astra Militarum",
                "", "", "Lascannon", List.of(), List.of(), List.of(), List.of(arma), List.of(), List.of()
        );
        var ejercito = new Catalogo40kService.Ejercito40k("Imperium", "Astra Militarum", List.of(unidad));
        var catalogo = new Catalogo40kService.Catalogo40kData(
                Map.of("Imperium", Map.of("Astra Militarum", ejercito)), LocalDateTime.now()
        );
        var servicio = new CreacionListasService(null, null, null, null, null, null);

        var vista = servicio.prepararCreadorLista40k(
                "WH40K_11", "Imperium", "Astra Militarum", "Prueba", 2000, catalogo
        );
        var unidadVista = vista.categorias().stream()
                .flatMap(categoria -> categoria.unidades().stream())
                .findFirst()
                .orElseThrow();
        var perfil = new ObjectMapper().readTree(unidadVista.perfilesArmasJson()).get(0);

        assertEquals("Lascannon", perfil.path("nombre").asText());
        assertEquals("1", perfil.path("ataques").asText());
        assertEquals("12", perfil.path("fuerza").asText());
        assertEquals("-3", perfil.path("penetracion").asText());
        assertEquals("D6+1", perfil.path("dano").asText());
    }
}
