package org.example.tfgenrique.controller;

import org.example.tfgenrique.service.ListaAsistenteService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ListaAsistenteControllerTest {
    private final ListaAsistenteController controller = new ListaAsistenteController(
            new ListaAsistenteService());

    @Test
    void exigeSesionYDevuelveJson() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(post("/creador-listas-40k/asistente/analizar")
                        .param("datosListaJson", "{\"unidades\":[]}"))
                .andExpect(status().isUnauthorized());

        var session = new MockHttpSession();
        session.setAttribute("nombreUsuario", "jugador");
        mvc.perform(post("/creador-listas-40k/asistente/analizar").session(session)
                        .param("datosListaJson", "{\"limitePuntos\":2000,\"unidades\":[]}")
                        .param("pregunta", "¿Cómo mejoro?"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.totalUnidades").value(0))
                .andExpect(jsonPath("$.recomendaciones[0]").exists());
    }
}
