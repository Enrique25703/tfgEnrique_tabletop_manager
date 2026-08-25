package org.example.tfgenrique.config;

import org.example.tfgenrique.service.user.NotificacionService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributes {

    private final NotificacionService notificacionService;

    public GlobalModelAttributes(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @ModelAttribute("notificacionesHeader")
    public NotificacionService.BandejaNotificacionesView notificacionesHeader(
            HttpSession session,
            HttpServletRequest request
    ) {
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/css/")) {
            return new NotificacionService.BandejaNotificacionesView(0, java.util.List.of());
        }
        return notificacionService.prepararBandeja(obtenerUsuarioId(session));
    }

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || uri.isBlank()) {
            return "/menu-principal";
        }
        String query = request.getQueryString();
        return (query == null || query.isBlank()) ? uri : uri + "?" + query;
    }

    private Long obtenerUsuarioId(HttpSession session) {
        if (session == null) {
            return null;
        }
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
