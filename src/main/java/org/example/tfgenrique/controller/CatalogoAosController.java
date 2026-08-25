package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;

import org.example.tfgenrique.service.CreacionListasService;
import org.example.tfgenrique.service.CreacionListasService.CreadorLista40kView;
import org.example.tfgenrique.service.CreacionListasService.GuardadoListaResultado;
import org.example.tfgenrique.service.CreacionListasService.GuardarListaRequest;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService.Catalogo40kData;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService.Catalogo40kPaginaView;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService.InfoUnidad40kView;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService.Unidad40k;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CatalogoAosController {
    private final CatalogoAosService catalogoAosService;
    private final CreacionListasService creacionListasService;

    public CatalogoAosController(
            CatalogoAosService catalogoAosService,
            CreacionListasService creacionListasService
    ) {
        this.catalogoAosService = catalogoAosService;
        this.creacionListasService = creacionListasService;
    }

    @GetMapping("/catalogo-aos")
    public String mostrarCatalogoAos(
            @RequestParam(name = "faccion", required = false) String faccion,
            @RequestParam(name = "ejercito", required = false) String ejercito,
            Model model
    ) {
        Catalogo40kData catalogo;
        String errorCatalogo = null;
        try {
            catalogo = catalogoAosService.actualizarCatalogo();
        } catch (IllegalStateException ex) {
            catalogo = catalogoAosService.getData();
            errorCatalogo = ex.getMessage();
        }

        Catalogo40kPaginaView paginaCatalogo = catalogoAosService.prepararPaginaCatalogo(
                catalogo,
                faccion,
                ejercito,
                errorCatalogo
        );
        model.addAttribute("paginaCatalogoAos", paginaCatalogo);
        return "catalogos/catalogoAos";
    }

    @GetMapping("/infoUnidadAos")
    public String mostrarInfoUnidadAos(
            @RequestParam("faccion") String faccion,
            @RequestParam("ejercito") String ejercito,
            @RequestParam("unidad") String unidad,
            Model model
    ) {
        Catalogo40kData catalogo = catalogoAosService.getData();
        if (catalogo == null) {
            catalogo = catalogoAosService.actualizarCatalogo();
        }

        Unidad40k unidadEncontrada = catalogo.buscarUnidad(faccion, ejercito, unidad);
        if (unidadEncontrada == null) {
            return "redirect:/catalogo-aos?faccion=" + faccion + "&ejercito=" + ejercito;
        }

        InfoUnidad40kView infoUnidad = catalogoAosService.prepararInfoUnidad(faccion, ejercito, unidadEncontrada);
        model.addAttribute("infoUnidadAos", infoUnidad);
        return "catalogos/infoUnidadAos";
    }

    @GetMapping("/creador-listas-aos")
    public String mostrarCreadorListasAos(
            HttpSession session,
            @RequestParam(value = "formatoJuego", defaultValue = "AOS_4") String formatoJuego,
            @RequestParam("faccion") String faccion,
            @RequestParam("ejercito") String ejercito,
            @RequestParam("nombreLista") String nombreLista,
            @RequestParam(value = "limitePuntos", defaultValue = "2000") Integer limitePuntos,
            Model model
    ) {
        if (session.getAttribute("nombreUsuario") == null) {
            return "redirect:/";
        }

        Catalogo40kData catalogo = catalogoAosService.getData();
        if (catalogo == null) {
            catalogo = catalogoAosService.actualizarCatalogo();
        }

        CatalogoAosService.Ejercito40k ejercitoSeleccionado = catalogo.buscarEjercito(faccion, ejercito);
        CreadorLista40kView creadorLista = creacionListasService.prepararCreadorListaAos(
                formatoJuego,
                faccion,
                ejercito,
                nombreLista,
                limitePuntos,
                ejercitoSeleccionado
        );

        model.addAttribute("creadorListaAos", creadorLista);
        return "listas/creadorListasAos";
    }

    @PostMapping("/creador-listas-aos/guardar")
    @ResponseBody
    public String guardarListaAos(
            HttpSession session,
            @RequestParam("formatoJuego") String formatoJuego,
            @RequestParam("nombreLista") String nombreLista,
            @RequestParam("faccion") String faccion,
            @RequestParam("ejercito") String ejercito,
            @RequestParam("limitePuntos") Integer limitePuntos,
            @RequestParam("puntosTotales") Integer puntosTotales,
            @RequestParam(value = "revisionCatalogo", required = false) String revisionCatalogo,
            @RequestParam("datosListaJson") String datosListaJson
    ) {
        try {
            Object nombreUsuario = session.getAttribute("nombreUsuario");
            if (nombreUsuario == null) {
                throw new IllegalArgumentException("La sesion ha caducado. Inicia sesion de nuevo.");
            }

            GuardarListaRequest request = new GuardarListaRequest(
                    formatoJuego,
                    nombreLista,
                    faccion,
                    ejercito,
                    limitePuntos,
                    puntosTotales,
                    revisionCatalogo,
                    datosListaJson
            );
            GuardadoListaResultado resultado = creacionListasService.guardarLista(nombreUsuario.toString(), request);
            return "OK|" + resultado.numeroVersion();
        } catch (IllegalArgumentException ex) {
            return "ERROR|" + ex.getMessage();
        }
    }
}
