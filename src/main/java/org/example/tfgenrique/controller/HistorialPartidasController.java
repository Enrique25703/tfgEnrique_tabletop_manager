package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.partidas.HistorialPartidasService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HistorialPartidasController {
    private final HistorialPartidasService historial;

    public HistorialPartidasController(HistorialPartidasService historial) {
        this.historial = historial;
    }

    @GetMapping("/partidas")
    public String listar(HttpSession session, Model model,
                         @RequestParam(defaultValue = "") String formatoJuego,
                         @RequestParam(defaultValue = "") String desde,
                         @RequestParam(defaultValue = "") String hasta,
                         @RequestParam(defaultValue = "") String resultado,
                         RedirectAttributes redirect) {
        Long usuarioId = usuarioId(session);
        if (usuarioId == null) return "redirect:/";
        try {
            model.addAttribute("historial", historial.obtenerHistorial(usuarioId, formatoJuego, desde, hasta, resultado));
            model.addAttribute("formatoJuego", formatoJuego);
            model.addAttribute("desde", desde);
            model.addAttribute("hasta", hasta);
            model.addAttribute("resultado", resultado);
            return "partidas/historialPartidas";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("mensajeError", ex.getMessage());
            return formatoJuego.isBlank() && desde.isBlank() && hasta.isBlank() && resultado.isBlank()
                    ? "redirect:/" : "redirect:/partidas";
        }
    }

    @PostMapping("/partidas/{partidaId}/eliminar")
    public String eliminar(HttpSession session, @PathVariable Long partidaId, RedirectAttributes redirect) {
        Long usuarioId = usuarioId(session);
        if (usuarioId == null) return "redirect:/";
        try {
            historial.eliminar(usuarioId, partidaId);
            redirect.addFlashAttribute("mensajeOk", "Partida eliminada del registro.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/partidas";
    }

    private Long usuarioId(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (id instanceof Long valor) return valor;
        if (id instanceof Integer valor) return valor.longValue();
        return null;
    }
}
