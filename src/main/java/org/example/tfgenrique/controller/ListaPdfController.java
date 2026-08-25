package org.example.tfgenrique.controller;

import java.nio.charset.StandardCharsets;

import org.example.tfgenrique.service.ListaPdfService;
import org.example.tfgenrique.service.ListaPdfService.PdfGenerado;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class ListaPdfController {
    private final ListaPdfService listaPdfService;

    public ListaPdfController(ListaPdfService listaPdfService) {
        this.listaPdfService = listaPdfService;
    }

    @GetMapping("/mis-listas/exportar")
    @ResponseBody
    public ResponseEntity<byte[]> exportarLista(
            HttpSession session,
            @RequestParam("listaId") Long listaId
    ) {
        Object nombreUsuario = session.getAttribute("nombreUsuario");
        if (nombreUsuario == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            PdfGenerado pdf = listaPdfService.generarPdf(nombreUsuario.toString(), listaId);
            ContentDisposition disposicion = ContentDisposition.attachment()
                    .filename(pdf.nombreArchivo(), StandardCharsets.UTF_8)
                    .build();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                    .contentLength(pdf.contenido().length)
                    .body(pdf.contenido());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                    .body(ex.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }
}
