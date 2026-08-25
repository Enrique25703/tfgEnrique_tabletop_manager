package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.partidas.PartidaService;
import org.example.tfgenrique.service.partidas.PartidaService.ConfiguracionRequest;
import org.example.tfgenrique.service.partidas.PartidaService.JugadoresRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @GetMapping("/partidas")
    public String nuevaPartida(HttpSession session, Model model) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("partidaNueva", partidaService.prepararNuevaPartida(usuarioId));
        return "partidas/partidaNueva";
    }

    @PostMapping("/partidas/crear")
    public String crearPartida(
            HttpSession session,
            @RequestParam("sistemaJuego") String sistemaJuego,
            @RequestParam(value = "estiloJuego", required = false) String estiloJuego,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            Long partidaId = partidaService.crearPartida(usuarioId, sistemaJuego, estiloJuego);
            return "redirect:/partidas/" + partidaId + "/jugadores";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/partidas";
        }
    }

    @GetMapping("/partidas/{partidaId}/jugadores")
    public String jugadores(
            HttpSession session,
            @PathVariable Long partidaId,
            Model model
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("jugadores", partidaService.prepararJugadores(usuarioId, partidaId));
        return "partidas/partidaJugadores";
    }

    @PostMapping("/partidas/{partidaId}/jugadores")
    public String guardarJugadores(
            HttpSession session,
            @PathVariable Long partidaId,
            @RequestParam("jugador1Nombre") String jugador1Nombre,
            @RequestParam("jugador1Faccion") String jugador1Faccion,
            @RequestParam(value = "jugador1Ejercito", required = false) String jugador1Ejercito,
            @RequestParam(value = "jugador1ListaTipo", required = false) String jugador1ListaTipo,
            @RequestParam(value = "jugador1ListaCustom", required = false) String jugador1ListaCustom,
            @RequestParam("jugador2Nombre") String jugador2Nombre,
            @RequestParam("jugador2Faccion") String jugador2Faccion,
            @RequestParam(value = "jugador2Ejercito", required = false) String jugador2Ejercito,
            @RequestParam(value = "jugador2ListaCustom", required = false) String jugador2ListaCustom,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            partidaService.guardarJugadores(
                    usuarioId,
                    partidaId,
                    new JugadoresRequest(
                            jugador1Nombre,
                            jugador1Faccion,
                            jugador1Ejercito,
                            jugador1ListaTipo,
                            jugador1ListaCustom,
                            jugador2Nombre,
                            jugador2Faccion,
                            jugador2Ejercito,
                            jugador2ListaCustom
                    )
            );
            return "redirect:/partidas/" + partidaId + "/configuracion";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/partidas/" + partidaId + "/jugadores";
        }
    }

    @GetMapping("/partidas/{partidaId}/configuracion")
    public String configuracion(HttpSession session, @PathVariable Long partidaId, Model model) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("configuracion", partidaService.prepararConfiguracion(usuarioId, partidaId));
        return "partidas/partidaConfiguracion";
    }

    @PostMapping("/partidas/{partidaId}/configuracion")
    public String guardarConfiguracion(
            HttpSession session,
            @PathVariable Long partidaId,
            @RequestParam(value = "tipoMision", required = false) String tipoMision,
            @RequestParam(value = "layout", required = false) String layout,
            @RequestParam(value = "despliegue", required = false) String despliegue,
            @RequestParam("jugadorDefensor") String jugadorDefensor,
            @RequestParam("jugadorPrimero") String jugadorPrimero,
            @RequestParam(value = "mostrarCommandPoints", required = false) String mostrarCommandPoints,
            @RequestParam(value = "usarCartasGiro", required = false) String usarCartasGiro,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            partidaService.guardarConfiguracion(
                    usuarioId,
                    partidaId,
                    new ConfiguracionRequest(
                            tipoMision,
                            layout,
                            despliegue,
                            jugadorDefensor,
                            jugadorPrimero,
                            mostrarCommandPoints != null,
                            usarCartasGiro != null
                    )
            );
            return "redirect:/partidas/" + partidaId + "/ronda/1";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/partidas/" + partidaId + "/configuracion";
        }
    }

    @GetMapping("/partidas/{partidaId}/ronda/{numeroRonda}")
    public String ronda(
            HttpSession session,
            @PathVariable Long partidaId,
            @PathVariable Integer numeroRonda,
            Model model
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("ronda", partidaService.prepararRonda(usuarioId, partidaId, numeroRonda));
        return "partidas/partidaRonda";
    }

    @PostMapping("/partidas/{partidaId}/ronda/{numeroRonda}")
    public String guardarRonda(
            HttpSession session,
            @PathVariable Long partidaId,
            @PathVariable Integer numeroRonda,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            partidaService.guardarRonda(usuarioId, partidaId, numeroRonda, params);
            if (numeroRonda >= 5) {
                return "redirect:/partidas/" + partidaId + "/final";
            }
            return "redirect:/partidas/" + partidaId + "/ronda/" + (numeroRonda + 1);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/partidas/" + partidaId + "/ronda/" + numeroRonda;
        }
    }

    @GetMapping("/partidas/{partidaId}/final")
    public String finalPartida(HttpSession session, @PathVariable Long partidaId, Model model) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("finalPartida", partidaService.prepararFinal(usuarioId, partidaId));
        return "partidas/partidaFinal";
    }

    @PostMapping("/partidas/{partidaId}/finalizar")
    public String finalizarPartida(
            HttpSession session,
            @PathVariable Long partidaId,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        partidaService.finalizarPartida(usuarioId, partidaId);
        redirectAttributes.addFlashAttribute("mensajeOk", "Partida finalizada y resultado guardado.");
        return "redirect:/partidas/" + partidaId + "/final";
    }

    private Long obtenerUsuarioId(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId instanceof Long) {
            return (Long) usuarioId;
        }
        if (usuarioId instanceof Integer) {
            return ((Integer) usuarioId).longValue();
        }
        return null;
    }
}
