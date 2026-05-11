package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuPrincipalController {

    @GetMapping("/menu-principal")
    public String mostrarMenuPrincipal(HttpSession session, Model model) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        model.addAttribute("nombreUsuario", nombreUsuario);
        return "menuPrincipal";
    }
}
