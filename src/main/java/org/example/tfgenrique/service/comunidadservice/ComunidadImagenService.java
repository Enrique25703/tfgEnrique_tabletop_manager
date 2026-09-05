package org.example.tfgenrique.service.comunidadservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

@Service
public class ComunidadImagenService {
    private static final String DIRECTORIO = "comunitiespicks/";

    private final ResourcePatternResolver resourceResolver;
    private volatile List<ImagenComunidadView> cache;

    public ComunidadImagenService(ResourcePatternResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public List<ImagenComunidadView> cargarImagenes() {
        List<ImagenComunidadView> imagenes = cache;
        if (imagenes != null) {
            return imagenes;
        }
        synchronized (this) {
            if (cache == null) {
                cache = escanearImagenes();
            }
            return cache;
        }
    }

    private List<ImagenComunidadView> escanearImagenes() {
        try {
            Resource[] recursos = resourceResolver.getResources("classpath:/comunitiespicks/**/*.*");
            List<ImagenComunidadView> imagenes = new ArrayList<>();
            for (Resource recurso : recursos) {
                String rutaRelativa = extraerRutaRelativa(recurso);
                String nombre = recurso.getFilename();
                if (rutaRelativa == null || nombre == null || nombre.isBlank()) {
                    continue;
                }
                rutaRelativa = UriUtils.decode(rutaRelativa, StandardCharsets.UTF_8);
                String url = UriUtils.encodePath("/comunitiespicks/" + rutaRelativa, StandardCharsets.UTF_8);
                imagenes.add(new ImagenComunidadView(url, nombre));
            }
            imagenes.sort(Comparator.comparing(ImagenComunidadView::etiqueta, String.CASE_INSENSITIVE_ORDER));
            return List.copyOf(imagenes);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron cargar las imagenes de comunidades.", ex);
        }
    }

    private String extraerRutaRelativa(Resource recurso) throws IOException {
        String[] candidatos = { recurso.getURL().toString(), recurso.getDescription() };
        for (String candidato : candidatos) {
            if (candidato == null) continue;
            String normalizado = candidato.replace('\\', '/');
            int indice = normalizado.indexOf(DIRECTORIO);
            if (indice >= 0) {
                return normalizado.substring(indice + DIRECTORIO.length());
            }
        }
        return null;
    }

    public record ImagenComunidadView(String url, String etiqueta) {
    }
}
