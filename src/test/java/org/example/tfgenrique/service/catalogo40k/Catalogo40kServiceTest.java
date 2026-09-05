package org.example.tfgenrique.service.catalogo40k;

import java.util.List;

import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Estadistica40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.PerfilArma40k;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService.Unidad40k;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Catalogo40kServiceTest {
    private final Catalogo40kService servicio = new Catalogo40kService();

    @Test
    void separaArmasYConservaTodosLosValoresDelPerfil() {
        var disparo = new PerfilArma40k("Rifle", "Ranged Weapons", List.of(
                new Estadistica40k("Range", "24\""), new Estadistica40k("A", "D6+1"),
                new Estadistica40k("BS", "3+"), new Estadistica40k("S", "5"),
                new Estadistica40k("AP", "-2"), new Estadistica40k("D", "D3")));
        var combate = new PerfilArma40k("Espada", "Melee Weapons", List.of(
                new Estadistica40k("Range", "Melee"), new Estadistica40k("A", "4"),
                new Estadistica40k("WS", "2+"), new Estadistica40k("S", "6"),
                new Estadistica40k("AP", "-3"), new Estadistica40k("D", "2")));

        var vista = servicio.prepararInfoUnidad("Imperium", "Prueba", unidad(List.of(disparo, combate)));

        assertEquals(List.of(new Catalogo40kService.ArmaUnidadView(
                "Rifle", "24\"", "D6+1", "3+", "5", "-2", "D3")), vista.armasDistancia());
        assertEquals(List.of(new Catalogo40kService.ArmaUnidadView(
                "Espada", "Melee", "4", "2+", "6", "-3", "2")), vista.armasCuerpoACuerpo());
    }

    @Test
    void admiteNombresLargosYRepresentaDatosAusentesSinInventarValores() {
        var perfil = new PerfilArma40k("Arma", "Weapon", List.of(
                new Estadistica40k("Range", "Melee"), new Estadistica40k("Attacks", "2"),
                new Estadistica40k("Weapon Skill", "4+"), new Estadistica40k("Strength", "3"),
                new Estadistica40k("Armour Penetration", "0"), new Estadistica40k("Damage", " ")));
        var vista = servicio.prepararInfoUnidad("", "", unidad(List.of(perfil)));
        assertTrue(vista.armasDistancia().isEmpty());
        assertEquals(new Catalogo40kService.ArmaUnidadView("Arma", "Melee", "2", "4+", "3", "0", "—"),
                vista.armasCuerpoACuerpo().get(0));
    }

    @Test
    void admiteUnidadesSinArmas() {
        var vista = servicio.prepararInfoUnidad("", "", unidad(List.of()));
        assertTrue(vista.armasDistancia().isEmpty());
        assertTrue(vista.armasCuerpoACuerpo().isEmpty());
        assertNull(servicio.prepararInfoUnidad("", "", null));
    }

    private Unidad40k unidad(List<PerfilArma40k> armas) {
        return new Unidad40k("Unidad", "100", "", "", "", "", "", "",
                List.of(), List.of(), List.of(), armas, List.of(), List.of());
    }
}
