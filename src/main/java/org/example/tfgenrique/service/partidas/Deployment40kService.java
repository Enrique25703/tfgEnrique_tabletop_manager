package org.example.tfgenrique.service.partidas;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class Deployment40kService {
    private static final String PREFIX_LAYOUT = "CA_TerrainLayout";
    private static final String PREFIX_SIMETRICO = "CA6_SF_";
    private static final String PREFIX_ASIMETRICO = "CA6_Ass_";

    private final PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public CatalogoDesplieguesView obtenerCatalogo() {
        List<OpcionVisualView> layouts = cargarLayouts();
        List<OpcionVisualView> desplieguesSimetricos = cargarDespliegues(
                "classpath:/deployments40k/despliegues_simetricos/*.png",
                "despliegues_simetricos/",
                PREFIX_SIMETRICO,
                "SIMETRICO",
                false
        );
        List<OpcionVisualView> desplieguesAsimetricos = cargarDespliegues(
                "classpath:/deployments40k/despliegues_asimetricos/*.png",
                "despliegues_asimetricos/",
                PREFIX_ASIMETRICO,
                "ASIMETRICO",
                false
        );

        List<OpcionVisualView> desplieguesMixtos = new ArrayList<>();
        for (OpcionVisualView opcion : desplieguesSimetricos) {
            desplieguesMixtos.add(new OpcionVisualView(
                    opcion.codigo(),
                    opcion.nombre() + " (Simetrico)",
                    opcion.imagenUrl(),
                    opcion.categoria()
            ));
        }
        for (OpcionVisualView opcion : desplieguesAsimetricos) {
            desplieguesMixtos.add(new OpcionVisualView(
                    opcion.codigo(),
                    opcion.nombre() + " (Asimetrico)",
                    opcion.imagenUrl(),
                    opcion.categoria()
            ));
        }

        return new CatalogoDesplieguesView(layouts, desplieguesSimetricos, desplieguesAsimetricos, desplieguesMixtos);
    }

    public OpcionVisualView buscarLayout(String codigoONombre) {
        return buscarPorCodigoONombre(obtenerCatalogo().layouts(), codigoONombre);
    }

    public OpcionVisualView buscarDespliegue(String codigoONombre) {
        CatalogoDesplieguesView catalogo = obtenerCatalogo();
        OpcionVisualView encontrado = buscarPorCodigoONombre(catalogo.desplieguesMixtos(), codigoONombre);
        if (encontrado != null) {
            return encontrado;
        }
        encontrado = buscarPorCodigoONombre(catalogo.desplieguesSimetricos(), codigoONombre);
        if (encontrado != null) {
            return encontrado;
        }
        return buscarPorCodigoONombre(catalogo.desplieguesAsimetricos(), codigoONombre);
    }

    private OpcionVisualView buscarPorCodigoONombre(List<OpcionVisualView> opciones, String codigoONombre) {
        if (codigoONombre == null || codigoONombre.isBlank()) {
            return null;
        }
        for (OpcionVisualView opcion : opciones) {
            if (codigoONombre.equals(opcion.codigo()) || codigoONombre.equals(opcion.nombre())) {
                return opcion;
            }
        }
        return null;
    }

    private List<OpcionVisualView> cargarLayouts() {
        List<OpcionVisualView> layouts = cargarDespliegues(
                "classpath:/deployments40k/layouts/*.png",
                "layouts/",
                PREFIX_LAYOUT,
                "LAYOUT",
                true
        );
        layouts.sort(Comparator.comparingInt(this::extraerOrdenLayout));
        return layouts;
    }

    private List<OpcionVisualView> cargarDespliegues(
            String patron,
            String carpetaRelativa,
            String prefijoNombre,
            String categoria,
            boolean ordenarLayouts
    ) {
        try {
            Resource[] recursos = resourceResolver.getResources(patron);
            List<OpcionVisualView> opciones = new ArrayList<>();

            for (Resource recurso : recursos) {
                String nombreArchivo = recurso.getFilename();
                if (nombreArchivo == null || nombreArchivo.isBlank()) {
                    continue;
                }
                String codigo = carpetaRelativa + nombreArchivo;
                String stem = nombreArchivo.replaceFirst("\\.[^.]+$", "");
                String nombre = ordenarLayouts ? nombreLayout(stem) : nombreDespliegue(stem, prefijoNombre);
                String imagenUrl = UriUtils.encodePath("/deployments40k/" + codigo, StandardCharsets.UTF_8);
                opciones.add(new OpcionVisualView(codigo, nombre, imagenUrl, categoria));
            }

            if (!ordenarLayouts) {
                opciones.sort(Comparator.comparing(OpcionVisualView::nombre, String.CASE_INSENSITIVE_ORDER));
            }
            return opciones;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron cargar los despliegues de Warhammer 40k.", ex);
        }
    }

    private int extraerOrdenLayout(OpcionVisualView opcion) {
        String nombre = opcion.nombre();
        String soloNumero = nombre.replaceAll("\\D+", "");
        try {
            return Integer.parseInt(soloNumero);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private String nombreLayout(String stem) {
        String numero = stem.replace(PREFIX_LAYOUT, "").trim();
        if (!numero.isBlank()) {
            return "Layout " + numero;
        }
        return limpiarTexto(stem);
    }

    private String nombreDespliegue(String stem, String prefijo) {
        String limpio = stem;
        if (limpio.startsWith(prefijo)) {
            limpio = limpio.substring(prefijo.length());
        }
        return limpiarTexto(limpio);
    }

    private String limpiarTexto(String texto) {
        String limpio = texto
                .replace('_', ' ')
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("\\s+", " ")
                .trim();
        return limpio.isBlank() ? texto : limpio;
    }

    public record OpcionVisualView(
            String codigo,
            String nombre,
            String imagenUrl,
            String categoria
    ) {
    }

    public record CatalogoDesplieguesView(
            List<OpcionVisualView> layouts,
            List<OpcionVisualView> desplieguesSimetricos,
            List<OpcionVisualView> desplieguesAsimetricos,
            List<OpcionVisualView> desplieguesMixtos
    ) {
    }
}
