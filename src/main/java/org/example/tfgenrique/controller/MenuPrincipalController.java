package org.example.tfgenrique.controller;

import org.example.tfgenrique.dao.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuPrincipalController {
    private final UsuarioRepository usuarioRepository;

    public MenuPrincipalController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping({"/", "/usuarios"})
    public String mostrarMenuPrincipal(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "index";
    }
}
