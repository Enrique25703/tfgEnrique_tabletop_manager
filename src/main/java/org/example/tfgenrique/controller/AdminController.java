package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.adminservice.AdminService;
import org.example.tfgenrique.service.adminservice.AdminService.ActualizarComunidadRequest;
import org.example.tfgenrique.service.adminservice.AdminService.ActualizarUsuarioRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private static final String ROL_ADMIN = "ADMIN";

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String mostrarPanelAdmin(
            HttpSession session,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            @RequestParam(value = "comunidadId", required = false) Long comunidadId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            model.addAttribute("admin", adminService.prepararPagina(
                    obtenerUsuarioId(session),
                    buscarUsuario,
                    buscarComunidad,
                    comunidadId
            ));
            return "infoUser/admin";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/menu-principal";
        }
    }

    @PostMapping("/admin/usuarios/actualizar")
    public String actualizarUsuario(
            HttpSession session,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("email") String email,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            @RequestParam(value = "comunidadIdRetorno", required = false) Long comunidadIdRetorno,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            Usuario usuarioActualizado = adminService.actualizarUsuario(
                    obtenerUsuarioId(session),
                    usuarioId,
                    new ActualizarUsuarioRequest(nombreUsuario, email)
            );
            if (usuarioActualizado.getId().equals(obtenerUsuarioId(session))) {
                session.setAttribute("nombreUsuario", usuarioActualizado.getNombreUsuario());
            }
            redirectAttributes.addFlashAttribute("mensajeOk", "Usuario actualizado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        conservarContextoBusqueda(redirectAttributes, buscarUsuario, buscarComunidad, comunidadIdRetorno);
        return "redirect:/admin";
    }

    @PostMapping("/admin/usuarios/contrasena")
    public String actualizarContrasenaUsuario(
            HttpSession session,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("nuevaContrasena") String nuevaContrasena,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            @RequestParam(value = "comunidadIdRetorno", required = false) Long comunidadIdRetorno,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            adminService.actualizarContrasenaUsuario(obtenerUsuarioId(session), usuarioId, nuevaContrasena);
            redirectAttributes.addFlashAttribute("mensajeOk", "Contrasena actualizada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        conservarContextoBusqueda(redirectAttributes, buscarUsuario, buscarComunidad, comunidadIdRetorno);
        return "redirect:/admin";
    }

    @PostMapping("/admin/usuarios/eliminar")
    public String eliminarUsuario(
            HttpSession session,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            @RequestParam(value = "comunidadIdRetorno", required = false) Long comunidadIdRetorno,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            adminService.eliminarUsuario(obtenerUsuarioId(session), usuarioId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Usuario desactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        conservarContextoBusqueda(redirectAttributes, buscarUsuario, buscarComunidad, comunidadIdRetorno);
        return "redirect:/admin";
    }

    @PostMapping("/admin/comunidades/actualizar")
    public String actualizarComunidad(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "logoUrl", required = false) String logoUrl,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            @RequestParam(value = "comunidadIdRetorno", required = false) Long comunidadIdRetorno,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            adminService.actualizarComunidad(
                    obtenerUsuarioId(session),
                    comunidadId,
                    new ActualizarComunidadRequest(nombre, logoUrl)
            );
            redirectAttributes.addFlashAttribute("mensajeOk", "Comunidad actualizada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        conservarContextoBusqueda(
                redirectAttributes,
                buscarUsuario,
                buscarComunidad,
                comunidadIdRetorno != null ? comunidadIdRetorno : comunidadId
        );
        return "redirect:/admin";
    }

    @PostMapping("/admin/comunidades/eliminar")
    public String eliminarComunidad(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            adminService.eliminarComunidad(obtenerUsuarioId(session), comunidadId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Comunidad desactivada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        conservarContextoBusqueda(redirectAttributes, buscarUsuario, buscarComunidad, null);
        return "redirect:/admin";
    }

    @PostMapping("/admin/comunidades/miembros/eliminar")
    public String eliminarMiembroComunidad(
            HttpSession session,
            @RequestParam("comunidadId") Long comunidadId,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam(value = "buscarUsuario", required = false) String buscarUsuario,
            @RequestParam(value = "buscarComunidad", required = false) String buscarComunidad,
            RedirectAttributes redirectAttributes
    ) {
        if (!esAdmin(session)) {
            return "redirect:/menu-principal";
        }

        try {
            adminService.eliminarMiembroComunidad(obtenerUsuarioId(session), comunidadId, usuarioId);
            redirectAttributes.addFlashAttribute("mensajeOk", "Miembro eliminado de la comunidad.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        conservarContextoBusqueda(redirectAttributes, buscarUsuario, buscarComunidad, comunidadId);
        return "redirect:/admin";
    }

    private void conservarContextoBusqueda(
            RedirectAttributes redirectAttributes,
            String buscarUsuario,
            String buscarComunidad,
            Long comunidadId
    ) {
        if (buscarUsuario != null && !buscarUsuario.isBlank()) {
            redirectAttributes.addAttribute("buscarUsuario", buscarUsuario.trim());
        }
        if (buscarComunidad != null && !buscarComunidad.isBlank()) {
            redirectAttributes.addAttribute("buscarComunidad", buscarComunidad.trim());
        }
        if (comunidadId != null) {
            redirectAttributes.addAttribute("comunidadId", comunidadId);
        }
    }

    private boolean esAdmin(HttpSession session) {
        Object rol = session.getAttribute("rol");
        return rol instanceof String rolTexto && ROL_ADMIN.equalsIgnoreCase(rolTexto);
    }

    private Long obtenerUsuarioId(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");
        if (usuarioId instanceof Long valorLong) {
            return valorLong;
        }
        if (usuarioId instanceof Integer valorInt) {
            return valorInt.longValue();
        }
        return null;
    }
}
