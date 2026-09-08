package org.example.tfgenrique.controller;

import jakarta.servlet.http.HttpSession;
import org.example.tfgenrique.service.ListaAsistenteService;
import org.example.tfgenrique.service.ListaAsistenteService.AnalisisListaView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ListaAsistenteController {
    private final ListaAsistenteService listaAsistenteService;

    public ListaAsistenteController(ListaAsistenteService listaAsistenteService) {
        this.listaAsistenteService = listaAsistenteService;
    }

    @PostMapping("/creador-listas-40k/asistente/analizar")
    @ResponseBody
    public ResponseEntity<AnalisisListaView> analizar(
            HttpSession session,
            @RequestParam("datosListaJson") String datosListaJson,
            @RequestParam(value = "pregunta", required = false) String pregunta
    ) {
        if (session.getAttribute("nombreUsuario") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("La sesion ha caducado."));
        }
        try {
            return ResponseEntity.ok(listaAsistenteService.analizar(datosListaJson, pregunta));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        }
    }

    private AnalisisListaView error(String mensaje) {
        return new AnalisisListaView(mensaje, List.of(), List.of(), 0, 0, 0);
    }
}
