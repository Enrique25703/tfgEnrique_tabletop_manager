package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.EstadisticasService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(
            HttpSession session,
            @RequestParam(value = "juego", required = false) String juego,
            Model model
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("estadisticas", estadisticasService.prepararEstadisticas(usuarioId, juego));
        return "infoUser/estadisticas";
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
