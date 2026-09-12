package org.example.tfgenrique.service.catalogos;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class DescargadorCatalogosTest {
    private HttpServer servidor;
    private String url;
    private final CountDownLatch liberarServidor = new CountDownLatch(1);
    private final DescargadorCatalogos descargador =
            new DescargadorCatalogos(Duration.ofSeconds(1), Duration.ofSeconds(1));

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        url = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/catalogo";
        servidor.start();
    }

    @AfterEach
    void detenerServidor() {
        liberarServidor.countDown();
        servidor.stop(0);
    }

    @Test
    void devuelveLaDescargaCompleta() throws Exception {
        byte[] contenido = {80, 75, 3, 4, 5};
        servidor.createContext("/catalogo", intercambio -> {
            try (intercambio) {
                intercambio.sendResponseHeaders(200, contenido.length);
                intercambio.getResponseBody().write(contenido);
            }
        });

        try (var flujo = descargador.descargar(url)) {
            assertArrayEquals(contenido, flujo.readAllBytes());
        }
    }

    @Test
    void rechazaUnaRespuestaHttpFallida() {
        servidor.createContext("/catalogo", intercambio -> {
            try (intercambio) {
                intercambio.sendResponseHeaders(503, -1);
            }
        });

        IOException error = assertThrows(IOException.class, () -> descargador.descargar(url));
        assertTrue(error.getMessage().contains("503"));
    }

    @Test
    void agotaElPlazoSiNoLleganLasCabeceras() {
        servidor.createContext("/catalogo", intercambio -> {
            try (intercambio) {
                esperarLiberacion();
            }
        });

        assertThrows(HttpTimeoutException.class, () -> descargador.descargar(url));
    }

    @Test
    void agotaElPlazoAunqueYaHayaRecibidoParteDelZip() {
        CountDownLatch cuerpoIniciado = new CountDownLatch(1);
        servidor.createContext("/catalogo", intercambio -> {
            try (intercambio) {
                intercambio.sendResponseHeaders(200, 100);
                intercambio.getResponseBody().write(new byte[]{80, 75});
                intercambio.getResponseBody().flush();
                cuerpoIniciado.countDown();
                esperarLiberacion();
            }
        });

        assertThrows(HttpTimeoutException.class, () -> descargador.descargar(url));
        assertEquals(0, cuerpoIniciado.getCount(), "El servidor debe haber empezado a enviar el ZIP");
    }

    private void esperarLiberacion() {
        try {
            liberarServidor.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
