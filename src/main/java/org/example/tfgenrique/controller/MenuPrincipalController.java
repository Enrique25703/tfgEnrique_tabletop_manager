package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.CatalogoAosService;
import org.example.tfgenrique.service.Catalogo40kService;
import org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData;
import org.example.tfgenrique.service.Catalogo40kService.Ejercito40k;
import org.example.tfgenrique.service.Catalogo40kService.MenuPrincipalView;
import org.example.tfgenrique.service.CreacionListasService.CreadorLista40kView;
import org.example.tfgenrique.service.CreacionListasService.DetalleLista40kView;
import org.example.tfgenrique.service.CreacionListasService.MisListasView;
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
    private final CatalogoAosService catalogoAosService;
    private final CreacionListasService creacionListasService;

    public MenuPrincipalController(
            Catalogo40kService catalogo40kService,
            CatalogoAosService catalogoAosService,
            CreacionListasService creacionListasService
    ) {
        this.catalogo40kService = catalogo40kService;
        this.catalogoAosService = catalogoAosService;
        this.creacionListasService = creacionListasService;
    }

    @GetMapping("/menu-principal")
    public String mostrarMenuPrincipal(HttpSession session, Model model) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        String errorCatalogo = null;
        Catalogo40kData catalogo = catalogo40kService.getData();
        if (catalogo == null) {
            try {
                catalogo = catalogo40kService.actualizarCatalogo();
            } catch (IllegalStateException ex) {
                errorCatalogo = "No se pudo cargar el catalogo de 40k.";
            }
        }

        MenuPrincipalView menuPrincipal = catalogo40kService.prepararMenuPrincipal(
                nombreUsuario.toString(),
                catalogo,
                errorCatalogo
        );
        CatalogoAosService.Catalogo40kData catalogoAos = catalogoAosService.getData();
        if (catalogoAos == null) {
            try {
                catalogoAos = catalogoAosService.actualizarCatalogo();
            } catch (IllegalStateException ignored) {
                // Si AoS falla, el menu sigue cargando con 40k.
            }
        }
        CatalogoAosService.MenuPrincipalView menuPrincipalAos = catalogoAosService.prepararMenuPrincipal(
                nombreUsuario.toString(),
                catalogoAos,
                null
        );
        model.addAttribute("menuPrincipal", menuPrincipal);
        model.addAttribute("menuPrincipalAos", menuPrincipalAos);
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
        CreadorLista40kView creadorLista = creacionListasService.prepararCreadorLista40k(
                formatoJuego,
                faccion,
                ejercito,
                nombreLista,
                ejercitoSeleccionado
        );

        model.addAttribute("creadorLista", creadorLista);
        return "creadorListas40k";
    }

    @GetMapping("/mis-listas-40k")
    public String redirigirMisListasAntiguo() {
        return "redirect:/mis-listas";
    }

    @GetMapping("/mis-listas")
    public String mostrarMisListas(
            HttpSession session,
            @RequestParam(value = "formatoJuego", required = false) String formatoJuego,
            Model model
    ) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        java.util.List<ListaGuardadaView> listas = creacionListasService.obtenerListasGuardadas(nombreUsuario.toString());
        MisListasView misListas = creacionListasService.prepararMisListasView(
                nombreUsuario.toString(),
                listas,
                formatoJuego
        );
        model.addAttribute("misListas", misListas);
        return "misListas";
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
        DetalleLista40kView detalleLista = creacionListasService.prepararDetalleLista40kView(lista);
        model.addAttribute("detalleLista", detalleLista);
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
