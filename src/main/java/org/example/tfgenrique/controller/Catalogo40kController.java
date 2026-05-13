package org.example.tfgenrique.controller;

import org.example.tfgenrique.service.Catalogo40kService;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.Catalogo40kService.Unidad40k;
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

    @GetMapping("/infoUnidad40k")
    public String mostrarInfoUnidad40k(
            @RequestParam("faccion") String faccion,
            @RequestParam("ejercito") String ejercito,
            @RequestParam("unidad") String unidad,
            Model model
    ) {
        Catalogo40kData catalogo = catalogo40kService.getData();
        if (catalogo == null) {
            catalogo = catalogo40kService.actualizarCatalogo();
        }

        Unidad40k unidadEncontrada = catalogo.buscarUnidad(faccion, ejercito, unidad);
        if (unidadEncontrada == null) {
            return "redirect:/catalogo40k?faccion=" + faccion + "&ejercito=" + ejercito;
        }

        model.addAttribute("faccionSeleccionada", faccion);
        model.addAttribute("ejercitoSeleccionado", ejercito);
        model.addAttribute("unidad", unidadEncontrada);
        return "infoUnidad40k";
    }
}
