package org.example.tfgenrique.controller;

import org.example.tfgenrique.service.user.NotificacionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping("/notificaciones/aceptar")
    public String aceptar(
            HttpSession session,
            @RequestParam("notificacionId") Long notificacionId,
            @RequestParam(value = "redirect", required = false) String redirect,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            String destino = notificacionService.aceptarNotificacion(usuarioId, notificacionId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Notificacion aceptada.");
            return "redirect:" + resolverDestino(redirect, destino);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:" + resolverDestino(redirect, "/menu-principal");
        }
    }

    @PostMapping("/notificaciones/rechazar")
    public String rechazar(
            HttpSession session,
            @RequestParam("notificacionId") Long notificacionId,
            @RequestParam(value = "redirect", required = false) String redirect,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            notificacionService.rechazarNotificacion(usuarioId, notificacionId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Notificacion rechazada.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:" + resolverDestino(redirect, "/menu-principal");
    }

    private String resolverDestino(String redirect, String defecto) {
        if (redirect == null || redirect.isBlank()) {
            return defecto;
        }
        return redirect;
    }

    private Long obtenerUsuarioId(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId instanceof Long valor) {
            return valor;
        }
        if (usuarioId instanceof Integer valor) {
            return valor.longValue();
        }
        return null;
    }
}
