package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.EditorPerfilService;
import org.example.tfgenrique.service.EditorPerfilService.PerfilRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EditorPerfilController {
    private final EditorPerfilService editorPerfilService;

    public EditorPerfilController(EditorPerfilService editorPerfilService) {
        this.editorPerfilService = editorPerfilService;
    }

    @GetMapping("/ajustes")
    public String mostrarAjustes(HttpSession session, Model model) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        model.addAttribute("perfil", editorPerfilService.prepararPerfil(usuarioId));
        return "ajustes";
    }

    @PostMapping("/ajustes/perfil")
    public String actualizarPerfil(
            HttpSession session,
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("email") String email,
            @RequestParam(value = "fotoUrl", required = false) String fotoUrl,
            @RequestParam(value = "contrasenaActual", required = false) String contrasenaActual,
            @RequestParam(value = "nuevaContrasena", required = false) String nuevaContrasena,
            @RequestParam(value = "repetirNuevaContrasena", required = false) String repetirNuevaContrasena,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = obtenerUsuarioId(session);
        if (usuarioId == null) {
            return "redirect:/";
        }

        try {
            var perfilActualizado = editorPerfilService.actualizarPerfil(
                    usuarioId,
                    new PerfilRequest(
                            nombreUsuario,
                            email,
                            fotoUrl,
                            contrasenaActual,
                            nuevaContrasena,
                            repetirNuevaContrasena
                    )
            );
            session.setAttribute("nombreUsuario", perfilActualizado.nombreUsuario());
            session.setAttribute("fotoUrl", perfilActualizado.fotoUrl());
            redirectAttributes.addFlashAttribute("mensajeOk", "Perfil actualizado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/ajustes";
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
