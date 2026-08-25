package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.EventoRepository;
import org.example.tfgenrique.dao.InscripcionEventoRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.Evento;
import org.example.tfgenrique.entity.InscripcionEvento;
import org.example.tfgenrique.entity.SistemaJuego;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComunidadEventoService {

    private final EventoRepository eventoRepository;
    private final InscripcionEventoRepository inscripcionEventoRepository;
    private final SistemaJuegoRepository sistemaJuegoRepository;
    private final ComunidadMiembroService comunidadMiembroService;

    public ComunidadEventoService(
            EventoRepository eventoRepository,
            InscripcionEventoRepository inscripcionEventoRepository,
            SistemaJuegoRepository sistemaJuegoRepository,
            ComunidadMiembroService comunidadMiembroService
    ) {
        this.eventoRepository = eventoRepository;
        this.inscripcionEventoRepository = inscripcionEventoRepository;
        this.sistemaJuegoRepository = sistemaJuegoRepository;
        this.comunidadMiembroService = comunidadMiembroService;
    }

    @Transactional
    public Long crearEvento(Long usuarioId, Long comunidadId, ComunidadService.CrearEventoRequest request) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Comunidad comunidad = comunidadMiembroService.buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacion = comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);

        if (!ComunidadConstantes.ROL_PROPIETARIO.equals(afiliacion.getRolComunidad())) {
            throw new IllegalArgumentException("Solo los propietarios pueden crear eventos.");
        }
        if (request == null) {
            throw new IllegalArgumentException("No se han recibido los datos del evento.");
        }

        LocalDateTime fecha = request.getFecha();
        Integer rondas = request.getNumeroRondas();
        String lugar = normalizarTexto(request.getLugar());
        String formatoJuego = normalizarTexto(request.getFormatoJuego());
        Double latitud = request.getLatitud();
        Double longitud = request.getLongitud();

        if (fecha == null) {
            throw new IllegalArgumentException("Debes indicar una fecha para el evento.");
        }
        if (rondas == null || rondas <= 0) {
            throw new IllegalArgumentException("Debes indicar un numero de rondas valido.");
        }
        if (lugar.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un lugar para el evento.");
        }
        if (latitud == null || longitud == null) {
            throw new IllegalArgumentException("Debes seleccionar la ubicacion del evento en el mapa.");
        }
        if (formatoJuego.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un formato de juego.");
        }

        SistemaJuego sistemaJuego = resolverSistemaJuego(formatoJuego);
        LocalDateTime ahora = LocalDateTime.now();

        Evento evento = new Evento();
        evento.setOrganizadorUsuario(usuario);
        evento.setComunidad(comunidad);
        evento.setSistemaJuego(sistemaJuego);
        evento.setTitulo("Evento " + comunidad.getNombre() + " " + ComunidadConstantes.FORMATO_TITULO.format(fecha));
        evento.setDescripcion(null);
        evento.setTipoEvento(ComunidadConstantes.TIPO_EVENTO_COMUNIDAD);
        evento.setSistemaClasificacion(ComunidadConstantes.SISTEMA_CLASIFICACION_SUIZO);
        evento.setRondasPlanificadas(rondas);
        evento.setMaxParticipantes(null);
        evento.setUbicacion(lugar);
        evento.setLatitud(BigDecimal.valueOf(latitud));
        evento.setLongitud(BigDecimal.valueOf(longitud));
        evento.setCiudad(null);
        evento.setFechaLimiteInscripcion(fecha);
        evento.setInicioEn(fecha);
        evento.setFinEn(fecha.plusHours((long) rondas * 3L));
        evento.setEstado(ComunidadConstantes.ESTADO_EVENTO_ABIERTO);
        evento.setCreadoEn(ahora);
        evento.setActualizadoEn(ahora);
        eventoRepository.save(evento);

        return comunidad.getId();
    }

    @Transactional
    public Long unirseAEvento(Long usuarioId, Long eventoId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el evento."));
        Comunidad comunidad = evento.getComunidad();

        comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);

        if (inscripcionEventoRepository.existsByEventoAndUsuario(evento, usuario)) {
            throw new IllegalArgumentException("Ya estas inscrito en este evento.");
        }

        InscripcionEvento inscripcion = new InscripcionEvento();
        inscripcion.setEvento(evento);
        inscripcion.setUsuario(usuario);
        inscripcion.setVersionListaEjercito(null);
        inscripcion.setNombreListaEnviada(null);
        inscripcion.setPuntosEnviados(null);
        inscripcion.setEstadoInscripcion(ComunidadConstantes.ESTADO_INSCRIPCION_CONFIRMADA);
        inscripcion.setInscritoEn(LocalDateTime.now());
        inscripcionEventoRepository.save(inscripcion);

        return comunidad.getId();
    }

    public List<Evento> buscarEventos(Comunidad comunidad) {
        return eventoRepository.findByComunidadOrderByInicioEnAsc(comunidad);
    }

    public Evento buscarEvento(Long eventoId) {
        if (eventoId == null) {
            throw new IllegalArgumentException("Debes seleccionar un evento.");
        }
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el evento."));
    }

    public boolean usuarioInscrito(Evento evento, Usuario usuario) {
        return inscripcionEventoRepository.existsByEventoAndUsuario(evento, usuario);
    }

    public long contarInscritos(Evento evento) {
        return inscripcionEventoRepository.countByEvento(evento);
    }

    public String obtenerNombreFormato(String formatoJuego) {
        if (ComunidadConstantes.FORMATO_40K.equals(formatoJuego)) {
            return "Warhammer 40.000 11a edicion";
        }
        if (ComunidadConstantes.FORMATO_AOS.equals(formatoJuego)) {
            return "Age of Sigmar 4a edicion";
        }
        return formatoJuego;
    }

    private SistemaJuego resolverSistemaJuego(String formatoJuego) {
        SistemaJuego sistemaJuego = sistemaJuegoRepository.findByCodigo(formatoJuego).orElse(null);
        if (sistemaJuego != null) {
            return sistemaJuego;
        }

        LocalDateTime ahora = LocalDateTime.now();
        SistemaJuego nuevoSistema = new SistemaJuego();
        nuevoSistema.setCodigo(formatoJuego);
        nuevoSistema.setNombre(obtenerNombreFormato(formatoJuego));
        nuevoSistema.setEdicion(obtenerEdicionFormato(formatoJuego));
        nuevoSistema.setActivo(true);
        nuevoSistema.setCreadoEn(ahora);
        return sistemaJuegoRepository.save(nuevoSistema);
    }

    private String obtenerEdicionFormato(String formatoJuego) {
        if (ComunidadConstantes.FORMATO_40K.equals(formatoJuego)) {
            return "11a edicion";
        }
        if (ComunidadConstantes.FORMATO_AOS.equals(formatoJuego)) {
            return "4a edicion";
        }
        return null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }
}
