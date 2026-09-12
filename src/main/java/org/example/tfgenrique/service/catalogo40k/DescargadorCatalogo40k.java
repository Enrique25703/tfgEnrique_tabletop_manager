package org.example.tfgenrique.service.catalogo40k;

import java.io.InputStream;
import org.example.tfgenrique.service.catalogos.DescargadorCatalogos;

public class DescargadorCatalogo40k {
    private static final String URL_CATALOGO = "https://codeload.github.com/BSData/wh40k-11e/zip/refs/heads/main";

    private final DescargadorCatalogos descargador = new DescargadorCatalogos();

    public InputStream descargarCatalogo() throws Exception {
        return descargador.descargar(URL_CATALOGO);
    }
}
