package org.example.tfgenrique.service.catalogos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DescargadorCatalogos {
    private final HttpClient clienteHttp;
    private final Duration tiempoDescarga;

    public DescargadorCatalogos() {
        this(Duration.ofSeconds(15), Duration.ofMinutes(2));
    }

    DescargadorCatalogos(Duration tiempoConexion, Duration tiempoDescarga) {
        this.clienteHttp = HttpClient.newBuilder().connectTimeout(tiempoConexion).build();
        this.tiempoDescarga = tiempoDescarga;
    }

    public InputStream descargar(String url) throws IOException, InterruptedException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(tiempoDescarga).GET().build();
        // El futuro termina al recibir todo el ZIP, no solo las cabeceras.
        var descarga = clienteHttp.sendAsync(peticion, HttpResponse.BodyHandlers.ofByteArray());
        try {
            HttpResponse<byte[]> respuesta = descarga.get(tiempoDescarga.toMillis(), TimeUnit.MILLISECONDS);
            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
                throw new IOException("GitHub respondio con estado " + respuesta.statusCode());
            }
            return new ByteArrayInputStream(respuesta.body());
        } catch (TimeoutException ex) {
            descarga.cancel(true);
            throw new HttpTimeoutException("Se agoto el tiempo de descarga del catalogo");
        } catch (InterruptedException ex) {
            descarga.cancel(true);
            throw ex;
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException causa) {
                throw causa;
            }
            throw new IOException("No se pudo descargar el catalogo", ex.getCause());
        }
    }
}
