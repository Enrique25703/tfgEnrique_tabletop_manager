package org.example.tfgenrique.service;

import org.example.tfgenrique.service.catalogo40k.DescargadorCatalogo40k;
import org.example.tfgenrique.service.catalogo40k.LectorCatalogo40k;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class Catalogo40kService {
    private final DescargadorCatalogo40k descargadorCatalogo = new DescargadorCatalogo40k();
    private final LectorCatalogo40k lectorCatalogo = new LectorCatalogo40k();

    private Catalogo40kData datos;

    public synchronized Catalogo40kData actualizarCatalogo() {
        try (InputStream catalogoDescargado = descargadorCatalogo.descargarCatalogo()) {
            datos = lectorCatalogo.leerCatalogo(catalogoDescargado);
            return datos;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo actualizar el catalogo de Warhammer 40k", ex);
        }
    }

    public Catalogo40kData getData() {
        return datos;
    }

    public MenuPrincipalView prepararMenuPrincipal(
            String nombreUsuario,
            Catalogo40kData catalogo,
            String errorCatalogo
    ) {
        List<FaccionMenuView> facciones = new ArrayList<>();
        Map<String, Map<String, Ejercito40k>> faccionesCatalogo = catalogo == null ? Map.of() : catalogo.facciones();

        for (Map.Entry<String, Map<String, Ejercito40k>> entrada : faccionesCatalogo.entrySet()) {
            facciones.add(new FaccionMenuView(
                    valorSeguro(entrada.getKey()),
                    new ArrayList<>(entrada.getValue().keySet())
            ));
        }

        return new MenuPrincipalView(valorSeguro(nombreUsuario), valorSeguro(errorCatalogo), List.copyOf(facciones));
    }

    public Catalogo40kPaginaView prepararPaginaCatalogo(
            Catalogo40kData catalogo,
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            String errorCatalogo
    ) {
        Map<String, Map<String, Ejercito40k>> faccionesCatalogo = catalogo == null ? Map.of() : catalogo.facciones();
        List<FaccionCatalogoView> facciones = new ArrayList<>();
        List<EjercitoOpcionView> ejercitosDisponibles = new ArrayList<>();

        for (Map.Entry<String, Map<String, Ejercito40k>> entradaFaccion : faccionesCatalogo.entrySet()) {
            String nombreFaccion = valorSeguro(entradaFaccion.getKey());
            facciones.add(new FaccionCatalogoView(nombreFaccion));

            if (nombreFaccion.equals(valorSeguro(faccionSeleccionada))) {
                for (String nombreEjercito : entradaFaccion.getValue().keySet()) {
                    ejercitosDisponibles.add(new EjercitoOpcionView(valorSeguro(nombreEjercito)));
                }
            }
        }

        Ejercito40k ejercito = catalogo == null ? null : catalogo.buscarEjercito(faccionSeleccionada, ejercitoSeleccionado);
        EjercitoCatalogoDetalleView detalle = null;

        if (ejercito != null) {
            List<UnidadCatalogoResumenView> unidades = new ArrayList<>();
            for (Unidad40k unidad : ejercito.unidades()) {
                unidades.add(new UnidadCatalogoResumenView(valorSeguro(unidad.nombre())));
            }

            detalle = new EjercitoCatalogoDetalleView(
                    valorSeguro(ejercito.faccion()),
                    valorSeguro(ejercito.nombre()),
                    unidades.size(),
                    List.copyOf(unidades)
            );
        }

        return new Catalogo40kPaginaView(
                valorSeguro(errorCatalogo),
                valorSeguro(faccionSeleccionada),
                valorSeguro(ejercitoSeleccionado),
                List.copyOf(facciones),
                List.copyOf(ejercitosDisponibles),
                detalle
        );
    }

    public InfoUnidad40kView prepararInfoUnidad(
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            Unidad40k unidad
    ) {
        if (unidad == null) {
            return null;
        }

        List<EstadisticaUnidadView> estadisticas = new ArrayList<>();
        for (Estadistica40k estadistica : unidad.estadisticas()) {
            estadisticas.add(new EstadisticaUnidadView(
                    valorSeguro(estadistica.nombre()),
                    valorSeguro(estadistica.valor())
            ));
        }

        List<HabilidadUnidadView> habilidades = new ArrayList<>();
        for (Habilidad40k habilidad : unidad.habilidadesDetalle()) {
            String descripcion = valorSeguroONulo(habilidad.descripcion()).isBlank()
                    ? "Sin descripcion"
                    : habilidad.descripcion().trim();
            habilidades.add(new HabilidadUnidadView(valorSeguro(habilidad.nombre()), descripcion));
        }

        String perfiles = valorSeguroONulo(unidad.perfiles()).isBlank()
                ? "Sin equipamiento registrado"
                : unidad.perfiles().trim();
        String armas = valorSeguroONulo(unidad.armas()).isBlank()
                ? "Sin armas registradas"
                : unidad.armas().trim();

        return new InfoUnidad40kView(
                valorSeguro(faccionSeleccionada),
                valorSeguro(ejercitoSeleccionado),
                valorSeguro(unidad.nombre()),
                perfiles,
                armas,
                List.copyOf(estadisticas),
                List.copyOf(habilidades)
        );
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String valorSeguroONulo(String texto) {
        return texto == null ? "" : texto;
    }

    public record Catalogo40kData(Map<String, Map<String, Ejercito40k>> facciones, LocalDateTime actualizadoEn) {
        public Ejercito40k buscarEjercito(String faccion, String ejercito) {
            if (faccion == null || ejercito == null) {
                return null;
            }

            Map<String, Ejercito40k> ejercitos = facciones.get(faccion);
            return ejercitos == null ? null : ejercitos.get(ejercito);
        }

        public Unidad40k buscarUnidad(String faccion, String ejercito, String nombreUnidad) {
            Ejercito40k ejercitoEncontrado = buscarEjercito(faccion, ejercito);
            if (ejercitoEncontrado == null || nombreUnidad == null) {
                return null;
            }

            for (Unidad40k unidad : ejercitoEncontrado.unidades()) {
                if (nombreUnidad.equals(unidad.nombre())) {
                    return unidad;
                }
            }
            return null;
        }
    }

    public record Ejercito40k(String faccion, String nombre, List<Unidad40k> unidades) {
    }

    public record MenuPrincipalView(
            String nombreUsuario,
            String errorCatalogo,
            List<FaccionMenuView> facciones
    ) {
    }

    public record FaccionMenuView(
            String nombre,
            List<String> ejercitos
    ) {
    }

    public record Catalogo40kPaginaView(
            String errorCatalogo,
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            List<FaccionCatalogoView> facciones,
            List<EjercitoOpcionView> ejercitosDisponibles,
            EjercitoCatalogoDetalleView ejercito
    ) {
    }

    public record FaccionCatalogoView(String nombre) {
    }

    public record EjercitoOpcionView(String nombre) {
    }

    public record EjercitoCatalogoDetalleView(
            String faccion,
            String nombre,
            int totalUnidades,
            List<UnidadCatalogoResumenView> unidades
    ) {
    }

    public record UnidadCatalogoResumenView(String nombre) {
    }

    public record InfoUnidad40kView(
            String faccionSeleccionada,
            String ejercitoSeleccionado,
            String nombreUnidad,
            String perfiles,
            String armas,
            List<EstadisticaUnidadView> estadisticas,
            List<HabilidadUnidadView> habilidades
    ) {
    }

    public record EstadisticaUnidadView(String nombre, String valor) {
    }

    public record HabilidadUnidadView(String nombre, String descripcion) {
    }

    public record Unidad40k(
            String nombre,
            String puntos,
            String roles,
            String palabrasClaveFaccion,
            String palabrasClave,
            String perfiles,
            String habilidades,
            String armas,
            List<Estadistica40k> estadisticas,
            List<Habilidad40k> habilidadesDetalle,
            List<GrupoMiniaturas40k> gruposMiniaturas,
            List<OpcionComposicion40k> opcionesComposicion
    ) {
    }

    public record OpcionComposicion40k(
            String id,
            String nombre,
            int puntos,
            boolean seleccionPorDefecto,
            List<GrupoMiniaturas40k> gruposMiniaturas
    ) {
    }

    public record GrupoMiniaturas40k(
            String id,
            String nombre,
            int minimo,
            int maximo,
            List<ModeloUnidad40k> modelos,
            List<GrupoMiniaturas40k> subgrupos
    ) {
    }

    public record ModeloUnidad40k(
            String id,
            String nombre,
            int minimo,
            int maximo,
            List<String> equipamientoFijo,
            List<GrupoEquipamiento40k> gruposEquipamiento
    ) {
    }

    public record GrupoEquipamiento40k(
            String id,
            String nombre,
            int minimo,
            int maximo,
            List<OpcionEquipamiento40k> opciones
    ) {
    }

    public record OpcionEquipamiento40k(
            String id,
            String idReferencia,
            String nombre,
            boolean seleccionPorDefecto,
            List<String> detalleEquipamiento
    ) {
    }

    public record Estadistica40k(String nombre, String valor) {
    }

    public record Habilidad40k(String nombre, String descripcion) {
    }
}
