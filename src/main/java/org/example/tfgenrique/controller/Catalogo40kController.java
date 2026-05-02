package org.example.tfgenrique.controller;

import org.example.tfgenrique.service.Catalogo40kService;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Catalogo40kController {
    private final Catalogo40kService catalogo40kService;

    public Catalogo40kController(Catalogo40kService catalogo40kService) {
        this.catalogo40kService = catalogo40kService;
    }

    @GetMapping("/catalogo40k")
    public String mostrarCatalogo40k(
            @RequestParam(name = "faccion", required = false) String faccion,
            @RequestParam(name = "ejercito", required = false) String ejercito,
            Model model
    ) {
        try {
            Catalogo40kData catalogo = catalogo40kService.actualizarCatalogo();
            model.addAttribute("catalogo", catalogo);
            model.addAttribute("faccionSeleccionada", faccion);
            model.addAttribute("ejercitoSeleccionado", ejercito);
            model.addAttribute("ejercito", catalogo.buscarEjercito(faccion, ejercito));
        } catch (IllegalStateException ex) {
            Catalogo40kData catalogo = catalogo40kService.getData();
            model.addAttribute("catalogo", catalogo);
            model.addAttribute("faccionSeleccionada", faccion);
            model.addAttribute("ejercitoSeleccionado", ejercito);
            model.addAttribute("ejercito", catalogo.buscarEjercito(faccion, ejercito));
            model.addAttribute("errorCatalogo", ex.getMessage());
        }
        return "catalogo40k";
    }
}
