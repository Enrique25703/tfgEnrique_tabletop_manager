package org.example.tfgenrique.service.catalogo40k;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DescargadorCatalogo40k {
    private static final String URL_CATALOGO = "https://codeload.github.com/BSData/wh40k-11e/zip/refs/heads/main";

    private final HttpClient clienteHttp = HttpClient.newHttpClient();

    public InputStream descargarCatalogo() throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(URL_CATALOGO)).GET().build();
        HttpResponse<InputStream> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofInputStream());

        if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
            throw new IllegalStateException("GitHub respondio con estado " + respuesta.statusCode());
        }

        return respuesta.body();
    }
}
