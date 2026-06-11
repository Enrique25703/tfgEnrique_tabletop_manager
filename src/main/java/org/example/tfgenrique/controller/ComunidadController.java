package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.comunidadservice.ComunidadService;
import org.example.tfgenrique.service.comunidadservice.ComunidadService.ComunidadPaginaView;
import org.example.tfgenrique.service.comunidadservice.ComunidadService.CrearEventoRequest;
import org.example.tfgenrique.service.comunidadservice.ComunidadService.CrearInvitacionPartidaRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class ComunidadController {

    private final ComunidadService comunidadService;

    public ComunidadController(ComunidadService comunidadService) {
        this.comunidadService = comunidadService;
    }

    @GetMapping("/comunidades")
    public String mostrarComunidades(
            HttpSession session,
            @RequestParam(value = "comunidadId", required = false) Long comunidadId,
            Model model
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        ComunidadPaginaView comunidades = comunidadService.prepararPagina(usuarioId, comunidadId);
        model.addAttribute("comunidades", comunidades);
        return "comunidades";
    }

    @PostMapping("/comunidades/crear")
    public String crearComunidad(
            HttpSession session,
            @RequestParam("nombreComunidad") String nombreComunidad,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            Long comunidadId = comunidadService.crearComunidad(usuarioId, nombreComunidad);
            redirectAttributes.addFlashAttribute("mensajeOk", "Comunidad creada correctamente.");
            return "redirect:/comunidades?comunidadId=" + comunidadId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades";
        }
    }

    @PostMapping("/comunidades/unirse")
    public String unirseAComunidad(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            Long comunidadSeleccionada = comunidadService.unirseAComunidad(usuarioId, comunidadId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Te has unido a la comunidad.");
            return "redirect:/comunidades?comunidadId=" + comunidadSeleccionada;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades";
        }
    }

    @PostMapping("/comunidades/eventos/crear")
    public String crearEvento(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha,
            @RequestParam("numeroRondas") Integer numeroRondas,
            @RequestParam("lugar") String lugar,
            @RequestParam("formatoJuego") String formatoJuego,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        CrearEventoRequest request = new CrearEventoRequest();
        request.setFecha(fecha);
        request.setNumeroRondas(numeroRondas);
        request.setLugar(lugar);
        request.setFormatoJuego(formatoJuego);

        try {
            Long comunidadSeleccionada = comunidadService.crearEvento(usuarioId, comunidadId, request);
            redirectAttributes.addFlashAttribute("mensajeOk", "Evento creado correctamente.");
            return "redirect:/comunidades?comunidadId=" + comunidadSeleccionada;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades?comunidadId=" + comunidadId;
        }
    }

    @PostMapping("/comunidades/eventos/unirse")
    public String unirseAEvento(
            HttpSession session,
            @RequestParam("eventoId") Long eventoId,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            Long comunidadSeleccionada = comunidadService.unirseAEvento(usuarioId, eventoId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Te has inscrito en el evento.");
            return "redirect:/comunidades?comunidadId=" + comunidadSeleccionada;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades";
        }
    }

    @PostMapping("/comunidades/invitaciones/crear")
    public String crearInvitacionPartida(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha,
            @RequestParam("lugar") String lugar,
            @RequestParam("formatoJuego") String formatoJuego,
            @RequestParam(value = "mensaje", required = false) String mensaje,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        CrearInvitacionPartidaRequest request = new CrearInvitacionPartidaRequest();
        request.setFecha(fecha);
        request.setLugar(lugar);
        request.setFormatoJuego(formatoJuego);
        request.setMensaje(mensaje);

        try {
            Long comunidadSeleccionada = comunidadService.crearInvitacionPartida(usuarioId, comunidadId, request);
            redirectAttributes.addFlashAttribute("mensajeOk", "Invitacion de partida creada.");
            return "redirect:/comunidades?comunidadId=" + comunidadSeleccionada;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades?comunidadId=" + comunidadId;
        }
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
