package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.Catalogo40kService;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.Catalogo40kService.Ejercito40k;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MenuPrincipalController {

    private final Catalogo40kService catalogo40kService;

    public MenuPrincipalController(Catalogo40kService catalogo40kService) {
        this.catalogo40kService = catalogo40kService;
    }

    @GetMapping("/menu-principal")
    public String mostrarMenuPrincipal(HttpSession session, Model model) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        Catalogo40kData catalogo = catalogo40kService.getData();
        if (catalogo == null) {
            try {
                catalogo = catalogo40kService.actualizarCatalogo();
            } catch (IllegalStateException ex) {
                model.addAttribute("errorCatalogo", "No se pudo cargar el catalogo de 40k.");
            }
        }

        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("catalogo40k", catalogo);
        return "menuPrincipal";
    }

    @GetMapping("/creador-listas-40k")
    public String mostrarCreadorListas40k(
            HttpSession session,
            @RequestParam("faccion") String faccion,
            @RequestParam("ejercito") String ejercito,
            @RequestParam("nombreLista") String nombreLista,
            Model model
    ) {
        if (session.getAttribute("nombreUsuario") == null) {
            return "redirect:/";
        }

        Catalogo40kData catalogo = catalogo40kService.getData();
        if (catalogo == null) {
            catalogo = catalogo40kService.actualizarCatalogo();
        }

        Ejercito40k ejercitoSeleccionado = catalogo.buscarEjercito(faccion, ejercito);

        // Aqui solo paso los datos basicos para arrancar el creador de listas.
        // De moemnto no hace nada mas porque esta pensado como una prueba.
        model.addAttribute("faccion", faccion);
        model.addAttribute("ejercito", ejercito);
        model.addAttribute("nombreLista", nombreLista);
        model.addAttribute("ejercitoData", ejercitoSeleccionado);
        return "creadorListas40k";
    }
}
