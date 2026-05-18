package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.Catalogo40kService;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.Catalogo40kService.Ejercito40k;
import org.example.tfgenrique.service.CreacionListasService;
import org.example.tfgenrique.service.CreacionListasService.GuardadoListaResultado;
import org.example.tfgenrique.service.CreacionListasService.GuardarListaRequest;
import org.example.tfgenrique.service.CreacionListasService.ListaGuardadaView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MenuPrincipalController {

    private final Catalogo40kService catalogo40kService;
    private final CreacionListasService creacionListasService;

    public MenuPrincipalController(
            Catalogo40kService catalogo40kService,
            CreacionListasService creacionListasService
    ) {
        this.catalogo40kService = catalogo40kService;
        this.creacionListasService = creacionListasService;
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
            @RequestParam(value = "formatoJuego", defaultValue = "WH40K_10") String formatoJuego,
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
        model.addAttribute("formatoJuego", formatoJuego);
        model.addAttribute("faccion", faccion);
        model.addAttribute("ejercito", ejercito);
        model.addAttribute("nombreLista", nombreLista);
        model.addAttribute("ejercitoData", ejercitoSeleccionado);
        return "creadorListas40k";
    }

    @GetMapping("/mis-listas-40k")
    public String mostrarMisListas40k(HttpSession session, Model model) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        java.util.List<ListaGuardadaView> listas = creacionListasService.obtenerListasGuardadas(nombreUsuario.toString());
        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("listasGuardadas", listas);
        return "misListas40k";
    }

    @GetMapping("/mi-lista-40k")
    public String mostrarDetalleLista40k(
            HttpSession session,
            @RequestParam("listaId") Long listaId,
            Model model
    ) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        ListaGuardadaView lista = creacionListasService.obtenerListaGuardadaPorId(nombreUsuario.toString(), listaId);
        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("listaGuardada", lista);
        return "detalleLista40k";
    }

    @PostMapping("/creador-listas-40k/guardar")
    @ResponseBody
    public String guardarLista40k(
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
