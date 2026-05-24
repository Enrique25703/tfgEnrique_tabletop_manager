package org.example.tfgenrique.controller;

import org.example.tfgenrique.service.Catalogo40kService;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kPaginaView;
import org.example.tfgenrique.service.Catalogo40kService.InfoUnidad40kView;
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

    @GetMapping("/catalogos")
    public String mostrarSelectorCatalogos() {
        return "selectorCatalogos";
    }

    @GetMapping("/catalogo40k")
    public String mostrarCatalogo40k(
            @RequestParam(name = "faccion", required = false) String faccion,
            @RequestParam(name = "ejercito", required = false) String ejercito,
            Model model
    ) {
        Catalogo40kData catalogo;
        String errorCatalogo = null;
        try {
            catalogo = catalogo40kService.actualizarCatalogo();
        } catch (IllegalStateException ex) {
            catalogo = catalogo40kService.getData();
            errorCatalogo = ex.getMessage();
        }

        Catalogo40kPaginaView paginaCatalogo = catalogo40kService.prepararPaginaCatalogo(
                catalogo,
                faccion,
                ejercito,
                errorCatalogo
        );
        model.addAttribute("paginaCatalogo", paginaCatalogo);
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

        InfoUnidad40kView infoUnidad = catalogo40kService.prepararInfoUnidad(faccion, ejercito, unidadEncontrada);
        model.addAttribute("infoUnidad", infoUnidad);
        return "infoUnidad40k";
    }
}
