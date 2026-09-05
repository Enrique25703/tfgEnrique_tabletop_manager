package org.example.tfgenrique.service.comunidadservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class ComunidadImagenServiceTest {

    @Test
    void cargaYCodificaLasImagenesDelClasspath() {
        ComunidadImagenService service = new ComunidadImagenService(new PathMatchingResourcePatternResolver());

        var imagenes = service.cargarImagenes();

        assertEquals(10, imagenes.size());
        assertTrue(imagenes.stream().allMatch(imagen -> imagen.url().startsWith("/comunitiespicks/")));
        assertTrue(imagenes.stream().allMatch(imagen -> imagen.url().contains("%20")), imagenes.toString());
        assertFalse(imagenes.stream().anyMatch(imagen -> imagen.url().contains(" ")));
    }
}
