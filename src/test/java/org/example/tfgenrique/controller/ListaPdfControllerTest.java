package org.example.tfgenrique.controller;

import org.example.tfgenrique.service.ListaPdfService;
import org.example.tfgenrique.service.ListaPdfService.PdfGenerado;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ListaPdfControllerTest {
    private final ListaPdfService servicio = mock(ListaPdfService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ListaPdfController(servicio)).build();

    @Test
    void descargaElPdfDeLaListaDelUsuarioAutenticado() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("nombreUsuario", "jugador");
        byte[] pdf = "%PDF-1.4".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(servicio.generarPdf("jugador", 7L)).thenReturn(new PdfGenerado("patrulla.pdf", pdf));

        mvc.perform(get("/mis-listas/exportar").param("listaId", "7").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("attachment;")))
                .andExpect(header().string("Content-Disposition", containsString("patrulla.pdf")))
                .andExpect(header().longValue("Content-Length", pdf.length))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void requiereSesionParaExportar() throws Exception {
        mvc.perform(get("/mis-listas/exportar").param("listaId", "7"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(servicio);
    }

    @Test
    void noDescargaUnaListaNoAccesible() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("nombreUsuario", "jugador");
        when(servicio.generarPdf("jugador", 9L))
                .thenThrow(new IllegalArgumentException("No se ha encontrado la lista solicitada."));

        mvc.perform(get("/mis-listas/exportar").param("listaId", "9").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Content-Disposition"));
    }
}
