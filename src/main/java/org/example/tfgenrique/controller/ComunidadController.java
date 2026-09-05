package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.comunidadservice.ComunidadService;
import org.example.tfgenrique.service.comunidadservice.ComunidadService.CrearEventoRequest;
import org.example.tfgenrique.service.comunidadservice.ComunidadService.CrearInvitacionPartidaRequest;
import org.example.tfgenrique.service.comunidadservice.ComunidadEventoService;
import org.example.tfgenrique.service.comunidadservice.ComunidadDueloService;
import org.example.tfgenrique.service.comunidadservice.ComunidadMiembroService;
import org.example.tfgenrique.service.comunidadservice.ComunidadSolicitudService;
import org.example.tfgenrique.service.comunidadservice.ComunidadVisorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class ComunidadController {

    private final ComunidadService comunidadService;
    private final ComunidadVisorService comunidadVisorService;
    private final ComunidadMiembroService comunidadMiembroService;
    private final ComunidadSolicitudService comunidadSolicitudService;
    private final ComunidadEventoService comunidadEventoService;
    private final ComunidadDueloService comunidadDueloService;

    public ComunidadController(
            ComunidadService comunidadService,
            ComunidadVisorService comunidadVisorService,
            ComunidadMiembroService comunidadMiembroService,
            ComunidadSolicitudService comunidadSolicitudService,
            ComunidadEventoService comunidadEventoService,
            ComunidadDueloService comunidadDueloService
    ) {
        this.comunidadService = comunidadService;
        this.comunidadVisorService = comunidadVisorService;
        this.comunidadMiembroService = comunidadMiembroService;
        this.comunidadSolicitudService = comunidadSolicitudService;
        this.comunidadEventoService = comunidadEventoService;
        this.comunidadDueloService = comunidadDueloService;
    }

    @GetMapping("/comunidades")
    public String mostrarComunidades(
            HttpSession session,
            Model model
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        var comunidades = comunidadService.prepararEventosCercanos(usuarioId);
        model.addAttribute("comunidades", comunidades);
        model.addAttribute("pestanaComunidades", "eventos");
        return "comunity_events/comunidades";
    }

    @GetMapping("/comunidades/mis-comunidades")
    public String mostrarMisComunidades(HttpSession session, Model model) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }
        model.addAttribute("misComunidades", comunidadService.prepararMisComunidades(usuarioId));
        model.addAttribute("pestanaComunidades", "mis-comunidades");
        return "comunity_events/misComunidades";
    }

    @GetMapping("/comunidades/descubrir")
    public String mostrarDescubrirComunidades(
            HttpSession session,
            @RequestParam(value = "comunidadId", required = false) Long comunidadId,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            Model model
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }
        model.addAttribute(
                "descubrir",
                comunidadService.prepararDescubrirComunidades(usuarioId, comunidadId, busqueda)
        );
        model.addAttribute("pestanaComunidades", "descubrir");
        return "comunity_events/descubrirComunidades";
    }

    @PostMapping("/comunidades/crear")
    public String crearComunidad(
            HttpSession session,
            @RequestParam("nombreComunidad") String nombreComunidad,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "logoUrl", required = false) String logoUrl,
            @RequestParam(value = "origen", required = false) String origen,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            Long comunidadId = comunidadService.crearComunidad(usuarioId, nombreComunidad, descripcion, logoUrl);
            redirectAttributes.addFlashAttribute("mensajeOk", "Comunidad creada correctamente.");
            return "redirect:/comunidades/" + comunidadId;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:" + rutaPestanaComunidades(origen);
        }
    }

    @PostMapping("/comunidades/unirse")
    public String unirseAComunidad(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam(value = "origen", required = false) String origen,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            ComunidadSolicitudService.ResultadoUnion resultado = comunidadService.solicitarOUnirse(usuarioId, comunidadId);
            if (resultado.unidoDirectamente()) {
                redirectAttributes.addFlashAttribute("mensajeOk", "Te has unido a la comunidad.");
                return "redirect:/comunidades/" + resultado.comunidadId();
            }
            redirectAttributes.addFlashAttribute("mensajeOk", "Solicitud enviada correctamente.");
            return "redirect:/comunidades/descubrir?comunidadId=" + resultado.comunidadId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            String ruta = rutaPestanaComunidades(origen);
            return "redirect:" + ruta + ("/comunidades/descubrir".equals(ruta)
                    ? "?comunidadId=" + comunidadId
                    : "");
        }
    }

    @GetMapping({"/comunidades/{comunidadId}", "/comunidades/{comunidadId}/miembros"})
    public String mostrarMiembros(
            HttpSession session,
            @PathVariable Long comunidadId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) return "redirect:/";
        try {
            var visor = comunidadVisorService.prepararMiembros(usuarioId, comunidadId);
            model.addAttribute("visor", visor);
            model.addAttribute("comunidadActual", visor.cabecera());
            model.addAttribute("pestanaActiva", "miembros");
            return "comunity_events/comunidadMiembros";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades";
        }
    }

    @GetMapping("/comunidades/{comunidadId}/solicitudes")
    public String mostrarSolicitudes(HttpSession session, @PathVariable Long comunidadId, Model model, RedirectAttributes redirectAttributes) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) return "redirect:/";
        try {
            var visor = comunidadVisorService.prepararSolicitudes(usuarioId, comunidadId);
            model.addAttribute("visor", visor);
            model.addAttribute("comunidadActual", visor.cabecera());
            model.addAttribute("pestanaActiva", "solicitudes");
            return "comunity_events/comunidadSolicitudes";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades";
        }
    }

    @GetMapping("/comunidades/{comunidadId}/eventos")
    public String mostrarEventos(HttpSession session, @PathVariable Long comunidadId, Model model, RedirectAttributes redirectAttributes) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) return "redirect:/";
        try {
            var visor = comunidadVisorService.prepararEventos(usuarioId, comunidadId);
            model.addAttribute("visor", visor);
            model.addAttribute("comunidadActual", visor.cabecera());
            model.addAttribute("pestanaActiva", "eventos");
            return "comunity_events/comunidadEventos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades";
        }
    }

    @GetMapping("/comunidades/{comunidadId}/ajustes")
    public String mostrarAjustesComunidad(HttpSession session, @PathVariable Long comunidadId, Model model, RedirectAttributes redirectAttributes) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) return "redirect:/";
        try {
            var visor = comunidadVisorService.prepararAjustes(usuarioId, comunidadId);
            model.addAttribute("visor", visor);
            model.addAttribute("comunidadActual", visor.cabecera());
            model.addAttribute("pestanaActiva", "ajustes");
            return "comunity_events/comunidadAjustes";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades/" + comunidadId;
        }
    }

    @PostMapping("/comunidades/{comunidadId}/miembros/{usuarioMiembroId}/promover")
    public String promoverMiembro(HttpSession session, @PathVariable Long comunidadId, @PathVariable Long usuarioMiembroId, RedirectAttributes redirectAttributes) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/miembros",
                "Miembro promovido a administrador.",
                () -> comunidadMiembroService.promoverAdministrador(obtenerUsuarioId(session), comunidadId, usuarioMiembroId)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/miembros/{usuarioMiembroId}/expulsar")
    public String expulsarMiembro(HttpSession session, @PathVariable Long comunidadId, @PathVariable Long usuarioMiembroId, RedirectAttributes redirectAttributes) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/miembros",
                "Miembro expulsado de la comunidad.",
                () -> comunidadMiembroService.expulsarMiembro(obtenerUsuarioId(session), comunidadId, usuarioMiembroId)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/miembros/{usuarioMiembroId}/duelo")
    public String crearDuelo(
            HttpSession session,
            @PathVariable Long comunidadId,
            @PathVariable Long usuarioMiembroId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha,
            @RequestParam("lugar") String lugar,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam("formatoJuego") String formatoJuego,
            @RequestParam(value = "mensaje", required = false) String mensaje,
            RedirectAttributes redirectAttributes
    ) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/miembros",
                "Desafio enviado. El oponente puede aceptarlo desde sus notificaciones.",
                () -> comunidadDueloService.crearDuelo(
                        obtenerUsuarioId(session), comunidadId, usuarioMiembroId, fecha, lugar,
                        latitud, longitud, formatoJuego, mensaje)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/solicitudes/{solicitudId}/aceptar")
    public String aceptarSolicitud(HttpSession session, @PathVariable Long comunidadId, @PathVariable Long solicitudId, RedirectAttributes redirectAttributes) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/solicitudes",
                "Solicitud aceptada.",
                () -> comunidadSolicitudService.aceptar(obtenerUsuarioId(session), comunidadId, solicitudId)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/solicitudes/{solicitudId}/rechazar")
    public String rechazarSolicitud(HttpSession session, @PathVariable Long comunidadId, @PathVariable Long solicitudId, RedirectAttributes redirectAttributes) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/solicitudes",
                "Solicitud rechazada.",
                () -> comunidadSolicitudService.rechazar(obtenerUsuarioId(session), comunidadId, solicitudId)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/eventos/guardar")
    public String guardarEvento(
            HttpSession session,
            @PathVariable Long comunidadId,
            @RequestParam(value = "eventoId", required = false) Long eventoId,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha,
            @RequestParam("numeroRondas") Integer numeroRondas,
            @RequestParam(value = "maxParticipantes", required = false) Integer maxParticipantes,
            @RequestParam("lugar") String lugar,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam("formatoJuego") String formatoJuego,
            RedirectAttributes redirectAttributes
    ) {
        CrearEventoRequest request = crearEventoRequest(
                titulo, descripcion, fecha, numeroRondas, maxParticipantes, lugar, latitud, longitud, formatoJuego);
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/eventos",
                eventoId == null ? "Evento creado correctamente." : "Evento actualizado correctamente.",
                () -> comunidadEventoService.guardarEvento(obtenerUsuarioId(session), comunidadId, eventoId, request)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/eventos/{eventoId}/eliminar")
    public String eliminarEvento(HttpSession session, @PathVariable Long comunidadId, @PathVariable Long eventoId, RedirectAttributes redirectAttributes) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/eventos",
                "Evento eliminado correctamente.",
                () -> comunidadEventoService.eliminarEvento(obtenerUsuarioId(session), comunidadId, eventoId)
        );
    }

    @PostMapping("/comunidades/eventos/crear")
    public String crearEvento(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha,
            @RequestParam("numeroRondas") Integer numeroRondas,
            @RequestParam("lugar") String lugar,
            @RequestParam("latitud") Double latitud,
            @RequestParam("longitud") Double longitud,
            @RequestParam("formatoJuego") String formatoJuego,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        CrearEventoRequest request = new CrearEventoRequest();
        request.setTitulo("Evento " + fecha.toLocalDate());
        request.setFecha(fecha);
        request.setNumeroRondas(numeroRondas);
        request.setLugar(lugar);
        request.setLatitud(latitud);
        request.setLongitud(longitud);
        request.setFormatoJuego(formatoJuego);

        try {
            Long comunidadSeleccionada = comunidadService.crearEvento(usuarioId, comunidadId, request);
            redirectAttributes.addFlashAttribute("mensajeOk", "Evento creado correctamente.");
            return "redirect:/comunidades/" + comunidadSeleccionada + "/eventos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades/" + comunidadId + "/eventos";
        }
    }

    @PostMapping("/comunidades/eventos/unirse")
    public String unirseAEvento(
            HttpSession session,
            @RequestParam("eventoId") Long eventoId,
            @RequestParam(value = "comunidadId", required = false) Long comunidadId,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            Long comunidadSeleccionada = comunidadService.unirseAEvento(usuarioId, eventoId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Te has inscrito en el evento.");
            return "redirect:/comunidades/" + comunidadSeleccionada + "/eventos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return comunidadId == null
                    ? "redirect:/comunidades"
                    : "redirect:/comunidades/" + comunidadId + "/eventos";
        }
    }

    @PostMapping("/comunidades/eventos/desinscribirse")
    public String desinscribirseDeEvento(
            HttpSession session,
            @RequestParam("eventoId") Long eventoId,
            @RequestParam("comunidadId") Long comunidadId,
            RedirectAttributes redirectAttributes
    ) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/eventos",
                "Te has desinscrito del evento.",
                () -> comunidadEventoService.desinscribirseDeEvento(obtenerUsuarioId(session), eventoId)
        );
    }

    @PostMapping("/comunidades/{comunidadId}/ajustes/privacidad")
    public String cambiarPrivacidad(
            HttpSession session,
            @PathVariable Long comunidadId,
            @RequestParam("privacidad") String privacidad,
            @RequestParam(value = "logoUrl", required = false) String logoUrl,
            RedirectAttributes redirectAttributes
    ) {
        return ejecutarAccion(
                session,
                redirectAttributes,
                "/comunidades/" + comunidadId + "/ajustes",
                "Privacidad de la comunidad actualizada.",
                () -> comunidadMiembroService.cambiarAjustes(obtenerUsuarioId(session), comunidadId, privacidad, logoUrl)
        );
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
            return "redirect:/comunidades/" + comunidadSeleccionada + "/eventos";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/comunidades/" + comunidadId + "/eventos";
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

    private CrearEventoRequest crearEventoRequest(
            String titulo,
            String descripcion,
            LocalDateTime fecha,
            Integer numeroRondas,
            Integer maxParticipantes,
            String lugar,
            Double latitud,
            Double longitud,
            String formatoJuego
    ) {
        CrearEventoRequest request = new CrearEventoRequest();
        request.setTitulo(titulo);
        request.setDescripcion(descripcion);
        request.setFecha(fecha);
        request.setNumeroRondas(numeroRondas);
        request.setMaxParticipantes(maxParticipantes);
        request.setLugar(lugar);
        request.setLatitud(latitud);
        request.setLongitud(longitud);
        request.setFormatoJuego(formatoJuego);
        return request;
    }

    private String ejecutarAccion(
            HttpSession session,
            RedirectAttributes redirectAttributes,
            String rutaRetorno,
            String mensajeOk,
            Runnable accion
    ) {
        if (obtenerUsuarioId(session) == null) {
            return "redirect:/";
        }
        try {
            accion.run();
            redirectAttributes.addFlashAttribute("mensajeOk", mensajeOk);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:" + rutaRetorno;
    }

    private String rutaPestanaComunidades(String origen) {
        if ("mis-comunidades".equals(origen)) {
            return "/comunidades/mis-comunidades";
        }
        if ("descubrir".equals(origen)) {
            return "/comunidades/descubrir";
        }
        return "/comunidades";
    }
}
