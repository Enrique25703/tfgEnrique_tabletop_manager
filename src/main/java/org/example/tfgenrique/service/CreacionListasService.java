package org.example.tfgenrique.service;
import org.example.tfgenrique.dao.FuenteCatalogoRepository;
import org.example.tfgenrique.dao.ListaEjercitoRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.dao.VersionListaEjercitoRepository;
import org.example.tfgenrique.entity.FuenteCatalogo;
import org.example.tfgenrique.entity.ListaEjercito;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.entity.VersionListaEjercito;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class CreacionListasService {

    private static final String VERSION_ESQUEMA_JSON = "1.0";
    private static final String FORMATO_40K = "WH40K_10";
    private static final String NOMBRE_FORMATO_40K = "Warhammer 40,000";
    private static final String EDICION_FORMATO_40K = "10a edicion";
    private static final String FUENTE_CATALOGO_40K = "BSData";
    private static final String TIPO_FUENTE_40K = "GIT";
    private static final String URL_FUENTE_40K = "https://github.com/BSData/wh40k-10e";
    private static final String RAMA_FUENTE_40K = "main";
    private static final int LIMITE_PUNTOS_POR_DEFECTO = 2000;

    private final UsuarioRepository usuarioRepository;
    private final SistemaJuegoRepository sistemaJuegoRepository;
    private final FuenteCatalogoRepository fuenteCatalogoRepository;
    private final ListaEjercitoRepository listaEjercitoRepository;
    private final VersionListaEjercitoRepository versionListaEjercitoRepository;

    public CreacionListasService(
            UsuarioRepository usuarioRepository,
            SistemaJuegoRepository sistemaJuegoRepository,
            FuenteCatalogoRepository fuenteCatalogoRepository,
            ListaEjercitoRepository listaEjercitoRepository,
            VersionListaEjercitoRepository versionListaEjercitoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sistemaJuegoRepository = sistemaJuegoRepository;
        this.fuenteCatalogoRepository = fuenteCatalogoRepository;
        this.listaEjercitoRepository = listaEjercitoRepository;
        this.versionListaEjercitoRepository = versionListaEjercitoRepository;
    }

    @Transactional
    public GuardadoListaResultado guardarLista(String nombreUsuarioSesion, GuardarListaRequest request) {
        if (nombreUsuarioSesion == null || nombreUsuarioSesion.isBlank()) {
            throw new IllegalArgumentException("No hay un usuario autenticado para guardar la lista.");
        }
        if (request == null) {
            throw new IllegalArgumentException("No se han recibido datos de la lista.");
        }

        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuarioSesion)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario autenticado."));

        String formatoJuego = normalizarTexto(request.formatoJuego());
        if (formatoJuego.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el formato de juego.");
        }

        SistemaJuego sistemaJuego = resolverSistemaJuego(formatoJuego);
        FuenteCatalogo fuenteCatalogo = resolverFuenteCatalogo(sistemaJuego);

        String nombreLista = normalizarTexto(request.nombreLista());
        if (nombreLista.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre para la lista.");
        }

        int limitePuntos = request.limitePuntos() == null || request.limitePuntos() <= 0
                ? LIMITE_PUNTOS_POR_DEFECTO
                : request.limitePuntos();

        int puntosTotales = request.puntosTotales() == null ? 0 : request.puntosTotales();
        if (puntosTotales <= 0) {
            throw new IllegalArgumentException("La lista debe tener puntos validos para guardarse.");
        }
        if (request.datosListaJson() == null || request.datosListaJson().isBlank()) {
            throw new IllegalArgumentException("No se han recibido las unidades de la lista.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        ListaEjercito listaEjercito = listaEjercitoRepository
                .findByPropietarioUsuarioAndSistemaJuegoAndNombre(usuario, sistemaJuego, nombreLista)
                .orElseGet(() -> crearListaEjercito(usuario, sistemaJuego, nombreLista, ahora));

        long versionesExistentes = versionListaEjercitoRepository.countByListaEjercito(listaEjercito);
        int numeroVersion = (int) versionesExistentes + 1;

        String faccion = normalizarTexto(request.faccion());
        listaEjercito.setNombreFaccionSnapshot(faccion);
        listaEjercito.setLimitePuntos(limitePuntos);
        listaEjercito.setPuntosActuales(puntosTotales);
        listaEjercito.setNumeroVersionActual(numeroVersion);
        listaEjercito.setActualizadoEn(ahora);
        listaEjercitoRepository.save(listaEjercito);

        String datosListaJson = request.datosListaJson().trim();

        VersionListaEjercito version = new VersionListaEjercito();
        version.setListaEjercito(listaEjercito);
        version.setNumeroVersion(numeroVersion);
        version.setFuenteCatalogo(fuenteCatalogo);
        version.setCommitCatalogoHash(null);
        version.setRevisionCatalogo(request.revisionCatalogo());
        version.setVersionEsquemaJson(VERSION_ESQUEMA_JSON);
        version.setChecksumDatos(calcularChecksum(datosListaJson));
        version.setPuntosTotales(puntosTotales);
        version.setDatosLista(datosListaJson);
        version.setCreadoEn(ahora);
        versionListaEjercitoRepository.save(version);

        return new GuardadoListaResultado(listaEjercito.getId(), version.getId(), numeroVersion);
    }

    private ListaEjercito crearListaEjercito(Usuario usuario, SistemaJuego sistemaJuego, String nombreLista, LocalDateTime ahora) {
        ListaEjercito listaEjercito = new ListaEjercito();
        listaEjercito.setPropietarioUsuario(usuario);
        listaEjercito.setSistemaJuego(sistemaJuego);
        listaEjercito.setNombre(nombreLista);
        listaEjercito.setLimitePuntos(LIMITE_PUNTOS_POR_DEFECTO);
        listaEjercito.setPuntosActuales(0);
        listaEjercito.setVisibilidad("PRIVADA");
        listaEjercito.setNumeroVersionActual(0);
        listaEjercito.setArchivada(false);
        listaEjercito.setCreadoEn(ahora);
        listaEjercito.setActualizadoEn(ahora);
        return listaEjercitoRepository.save(listaEjercito);
    }

    private SistemaJuego resolverSistemaJuego(String formatoJuego) {
        SistemaJuego sistemaJuego = sistemaJuegoRepository.findByCodigo(formatoJuego).orElse(null);
        if (sistemaJuego != null) {
            return sistemaJuego;
        }

        String nombreSistema = obtenerNombreSistema(formatoJuego);
        sistemaJuego = sistemaJuegoRepository.findByNombre(nombreSistema).orElse(null);
        if (sistemaJuego != null) {
            return sistemaJuego;
        }

        return crearSistemaJuego(formatoJuego);
    }

    private SistemaJuego crearSistemaJuego(String formatoJuego) {
        LocalDateTime ahora = LocalDateTime.now();
        SistemaJuego sistemaJuego = new SistemaJuego();
        sistemaJuego.setCodigo(formatoJuego);
        sistemaJuego.setNombre(obtenerNombreSistema(formatoJuego));
        sistemaJuego.setEdicion(obtenerEdicionSistema(formatoJuego));

        sistemaJuego.setActivo(true);
        sistemaJuego.setCreadoEn(ahora);
        return sistemaJuegoRepository.save(sistemaJuego);
    }

    private String obtenerNombreSistema(String formatoJuego) {
        if (FORMATO_40K.equals(formatoJuego)) {
            return NOMBRE_FORMATO_40K;
        }
        return formatoJuego;
    }

    private String obtenerEdicionSistema(String formatoJuego) {
        if (FORMATO_40K.equals(formatoJuego)) {
            return EDICION_FORMATO_40K;
        }
        return null;
    }

    private FuenteCatalogo resolverFuenteCatalogo(SistemaJuego sistemaJuego) {
        return fuenteCatalogoRepository.findFirstBySistemaJuegoAndActivoTrue(sistemaJuego)
                .orElseGet(() -> crearFuenteCatalogoPorDefecto(sistemaJuego));
    }

    private FuenteCatalogo crearFuenteCatalogoPorDefecto(SistemaJuego sistemaJuego) {
        FuenteCatalogo fuenteCatalogo = new FuenteCatalogo();
        fuenteCatalogo.setSistemaJuego(sistemaJuego);
        fuenteCatalogo.setNombreFuente(FUENTE_CATALOGO_40K);
        fuenteCatalogo.setTipoFuente(TIPO_FUENTE_40K);
        fuenteCatalogo.setUrlRepositorio(URL_FUENTE_40K);
        fuenteCatalogo.setRamaPorDefecto(RAMA_FUENTE_40K);
        fuenteCatalogo.setActivo(true);
        fuenteCatalogo.setCreadoEn(LocalDateTime.now());
        return fuenteCatalogoRepository.save(fuenteCatalogo);
    }

    private String calcularChecksum(String datosListaJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datosListaJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se ha podido calcular el checksum de la lista.", ex);
        }
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    public record GuardarListaRequest(
            String formatoJuego,
            String nombreLista,
            String faccion,
            String ejercito,
            Integer limitePuntos,
            Integer puntosTotales,
            String revisionCatalogo,
            String datosListaJson
    ) {
    }

    public record GuardadoListaResultado(Long listaId, Long versionId, Integer numeroVersion) {
    }

    @Transactional(readOnly = true)
    public java.util.List<ListaGuardadaView> obtenerListasGuardadas(String nombreUsuarioSesion) {
        if (nombreUsuarioSesion == null || nombreUsuarioSesion.isBlank()) {
            throw new IllegalArgumentException("No hay un usuario autenticado.");
        }

        java.util.List<VersionListaEjercitoRepository.ListaGuardadaProjection> listas =
                versionListaEjercitoRepository.buscarListasActualesPorUsuario(nombreUsuarioSesion);
        java.util.List<VersionListaEjercitoRepository.UnidadListaGuardadaProjection> unidades =
                versionListaEjercitoRepository.buscarUnidadesDeListasActualesPorUsuario(nombreUsuarioSesion);

        java.util.Map<Long, java.util.List<UnidadGuardadaView>> unidadesPorLista = new java.util.LinkedHashMap<>();
        for (VersionListaEjercitoRepository.UnidadListaGuardadaProjection unidad : unidades) {
            unidadesPorLista
                    .computeIfAbsent(unidad.getListaId(), clave -> new java.util.ArrayList<>())
                    .add(new UnidadGuardadaView(
                            valorSeguroVista(unidad.getNombreUnidad()),
                            valorSeguroVista(unidad.getRoles()),
                            unidad.getPuntosBase() == null ? 0 : unidad.getPuntosBase(),
                            valorSeguroVista(unidad.getCategoria())
                    ));
        }

        java.util.List<ListaGuardadaView> resultado = new java.util.ArrayList<>();
        for (VersionListaEjercitoRepository.ListaGuardadaProjection lista : listas) {
            resultado.add(new ListaGuardadaView(
                    lista.getListaId(),
                    valorSeguroVista(lista.getNombreLista()),
                    valorSeguroVista(lista.getFaccion()),
                    valorSeguroVista(lista.getEjercito()),
                    lista.getPuntosActuales() == null ? 0 : lista.getPuntosActuales(),
                    lista.getLimitePuntos() == null ? LIMITE_PUNTOS_POR_DEFECTO : lista.getLimitePuntos(),
                    lista.getNumeroVersion() == null ? 0 : lista.getNumeroVersion(),
                    unidadesPorLista.getOrDefault(lista.getListaId(), java.util.List.of())
            ));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public ListaGuardadaView obtenerListaGuardadaPorId(String nombreUsuarioSesion, Long listaId) {
        if (listaId == null) {
            throw new IllegalArgumentException("Debes indicar una lista.");
        }

        return obtenerListasGuardadas(nombreUsuarioSesion).stream()
                .filter(lista -> listaId.equals(lista.listaId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado la lista solicitada."));
    }

    private String valorSeguroVista(String texto) {
        return texto == null ? "" : texto;
    }

    public record ListaGuardadaView(
            Long listaId,
            String nombreLista,
            String faccion,
            String ejercito,
            Integer puntosActuales,
            Integer limitePuntos,
            Integer numeroVersion,
            java.util.List<UnidadGuardadaView> unidades
    ) {
    }

    public record UnidadGuardadaView(
            String nombreUnidad,
            String roles,
            Integer puntosBase,
            String categoria
    ) {
    }
}
