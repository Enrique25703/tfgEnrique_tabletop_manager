package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.LoginService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/")
    public String mostrarLogin() {
        return "index";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @RequestParam("usuario") String usuarioOEmail,
            @RequestParam("password") String password,
            HttpSession session,
            Model model
    ) {
        try {
            Usuario usuarioEncontrado = loginService.autenticar(usuarioOEmail, password);

            session.setAttribute("usuarioId", usuarioEncontrado.getId());
            session.setAttribute("nombreUsuario", usuarioEncontrado.getNombreUsuario());
            session.setAttribute("rol", usuarioEncontrado.getRol());

            return "redirect:/menu-principal";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "index";
        }
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("email") String email,
            @RequestParam("passwordRegistro") String password,
            HttpSession session,
            Model model
    ) {
        try {
            Usuario usuarioRegistrado = loginService.registrarUsuario(nombreUsuario, email, password);

            session.setAttribute("usuarioId", usuarioRegistrado.getId());
            session.setAttribute("nombreUsuario", usuarioRegistrado.getNombreUsuario());
            session.setAttribute("rol", usuarioRegistrado.getRol());

            return "redirect:/menu-principal";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "index";
        }
    }
}
