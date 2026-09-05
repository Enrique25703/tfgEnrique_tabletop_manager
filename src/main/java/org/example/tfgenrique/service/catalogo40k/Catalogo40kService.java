package org.example.tfgenrique.service.catalogo40k;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;

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
                List.copyOf(habilidades),
                prepararArmas(unidad.armasDetalle(), false),
                prepararArmas(unidad.armasDetalle(), true)
        );
    }

    private List<ArmaUnidadView> prepararArmas(List<PerfilArma40k> perfiles, boolean cuerpoACuerpo) {
        if (perfiles == null) {
            return List.of();
        }
        return perfiles.stream().filter(perfil -> {
            String tipo = valorSeguro(perfil.tipo()).toLowerCase(Locale.ROOT);
            String rango = valorArma(perfil, "Range", "Rango", "Alcance");
            boolean melee = tipo.contains("melee") || tipo.contains("cuerpo a cuerpo")
                    || "Melee".equalsIgnoreCase(rango);
            return melee == cuerpoACuerpo;
        }).map(perfil -> new ArmaUnidadView(
                valorSeguro(perfil.nombre()),
                valorArma(perfil, "Range", "Rango", "Alcance"),
                valorArma(perfil, "A", "Attacks", "Ataques"),
                cuerpoACuerpo
                        ? valorArma(perfil, "WS", "Weapon Skill", "HA", "Hit", "BS")
                        : valorArma(perfil, "BS", "Ballistic Skill", "HP", "Hit", "WS"),
                valorArma(perfil, "S", "Strength", "Fuerza"),
                valorArma(perfil, "AP", "Armour Penetration", "Armor Penetration", "FP"),
                valorArma(perfil, "D", "Damage", "Daño")
        )).toList();
    }

    private String valorArma(PerfilArma40k perfil, String... nombres) {
        if (perfil.estadisticas() != null) {
            for (String nombre : nombres) {
                for (Estadistica40k estadistica : perfil.estadisticas()) {
                    if (nombre.equalsIgnoreCase(valorSeguro(estadistica.nombre()))
                            && !valorSeguro(estadistica.valor()).isBlank()) {
                        return estadistica.valor().trim();
                    }
                }
            }
        }
        return "—";
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String valorSeguroONulo(String texto) {
        return texto == null ? "" : texto;
    }

    public record Catalogo40kData(
            Map<String, Map<String, Ejercito40k>> facciones,
            Map<String, ReglasEjercito40k> reglasEjercitos,
            List<TamanoBatalla40k> tamanosBatalla,
            LocalDateTime actualizadoEn
    ) {
        public Catalogo40kData(Map<String, Map<String, Ejercito40k>> facciones, LocalDateTime actualizadoEn) {
            this(facciones, Map.of(), List.of(), actualizadoEn);
        }

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

        public ReglasEjercito40k buscarReglasEjercito(String faccion, String ejercito) {
            return reglasEjercitos.getOrDefault(claveEjercito(faccion, ejercito), ReglasEjercito40k.vacias());
        }

        public static String claveEjercito(String faccion, String ejercito) {
            return (faccion == null ? "" : faccion.trim()) + "\u0000"
                    + (ejercito == null ? "" : ejercito.trim());
        }
    }

    public record Ejercito40k(String faccion, String nombre, List<Unidad40k> unidades) {
    }

    public record TamanoBatalla40k(
            String codigo,
            String nombre,
            int limitePuntos,
            int puntosDestacamento,
            int limiteRepeticiones,
            int limiteBattleline,
            int limiteMejoras
    ) {
    }

    public record Destacamento40k(
            String id,
            String nombre,
            int puntosDestacamento,
            List<String> disposiciones,
            List<ReglaConstruccion40k> reglas
    ) {
    }

    public record Mejora40k(
            String id,
            String nombre,
            int puntos,
            String destacamento,
            boolean upgrade,
            String descripcion
    ) {
    }

    public record ReglaConstruccion40k(String nombre, String descripcion) {
    }

    public record VinculosUnidad40k(boolean leader, boolean support, List<String> unidadesCompatibles) {
        public static VinculosUnidad40k vacios() {
            return new VinculosUnidad40k(false, false, List.of());
        }
    }

    public record ReglasEjercito40k(
            List<Destacamento40k> destacamentos,
            List<Mejora40k> mejoras,
            Map<String, VinculosUnidad40k> vinculosUnidades
    ) {
        public static ReglasEjercito40k vacias() {
            return new ReglasEjercito40k(List.of(), List.of(), Map.of());
        }

        public VinculosUnidad40k buscarVinculos(String unidad) {
            return vinculosUnidades.getOrDefault(unidad, VinculosUnidad40k.vacios());
        }
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
            List<HabilidadUnidadView> habilidades,
            List<ArmaUnidadView> armasDistancia,
            List<ArmaUnidadView> armasCuerpoACuerpo
    ) {
    }

    public record ArmaUnidadView(
            String nombre, String rango, String ataques, String impacta,
            String fuerza, String penetracion, String dano
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
            List<PerfilUnidad40k> perfilesDetalle,
            List<PerfilArma40k> armasDetalle,
            List<GrupoMiniaturas40k> gruposMiniaturas,
            List<OpcionComposicion40k> opcionesComposicion
    ) {
    }

    public record PerfilUnidad40k(
            String nombre,
            List<Estadistica40k> estadisticas
    ) {
    }

    public record PerfilArma40k(
            String nombre,
            String tipo,
            List<Estadistica40k> estadisticas
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
