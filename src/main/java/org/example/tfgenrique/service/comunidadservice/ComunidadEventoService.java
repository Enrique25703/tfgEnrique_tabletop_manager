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
        return guardarEvento(usuarioId, comunidadId, null, request);
    }

    @Transactional
    public Long guardarEvento(
            Long usuarioId,
            Long comunidadId,
            Long eventoId,
            ComunidadService.CrearEventoRequest request
    ) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Comunidad comunidad = comunidadMiembroService.buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacion = comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);
        if (request == null) {
            throw new IllegalArgumentException("No se han recibido los datos del evento.");
        }

        String titulo = normalizarTexto(request.getTitulo());
        String descripcion = normalizarTexto(request.getDescripcion());
        LocalDateTime fecha = request.getFecha();
        Integer rondas = request.getNumeroRondas();
        Integer maxParticipantes = request.getMaxParticipantes();
        String lugar = normalizarTexto(request.getLugar());
        String formatoJuego = normalizarTexto(request.getFormatoJuego());
        Double latitud = request.getLatitud();
        Double longitud = request.getLongitud();

        if (titulo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un nombre para el evento.");
        }
        if (titulo.length() > 150) {
            throw new IllegalArgumentException("El nombre del evento no puede superar los 150 caracteres.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Debes indicar una fecha para el evento.");
        }
        if (rondas == null || rondas <= 0) {
            throw new IllegalArgumentException("Debes indicar un numero de rondas valido.");
        }
        if (maxParticipantes != null && maxParticipantes <= 0) {
            throw new IllegalArgumentException("El máximo de participantes debe ser mayor que cero.");
        }
        if (lugar.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un lugar para el evento.");
        }
        if (lugar.length() > 150) {
            throw new IllegalArgumentException("La ubicación no puede superar los 150 caracteres.");
        }
        if (latitud == null || longitud == null) {
            throw new IllegalArgumentException("Debes seleccionar la ubicacion del evento en el mapa.");
        }
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas.");
        }
        if (formatoJuego.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un formato de juego.");
        }
        if (formatoJuego.length() > 30) {
            throw new IllegalArgumentException("El formato de juego no es válido.");
        }

        SistemaJuego sistemaJuego = resolverSistemaJuego(formatoJuego);
        LocalDateTime ahora = LocalDateTime.now();

        Evento evento;
        if (eventoId == null) {
            evento = new Evento();
            evento.setOrganizadorUsuario(usuario);
            evento.setComunidad(comunidad);
            evento.setCreadoEn(ahora);
            evento.setEstado(ComunidadConstantes.ESTADO_EVENTO_ABIERTO);
        } else {
            evento = bloquearEvento(eventoId);
            if (evento.getComunidad() == null || !evento.getComunidad().getId().equals(comunidad.getId())) {
                throw new IllegalArgumentException("El evento no pertenece a esta comunidad.");
            }
            if (!puedeGestionar(afiliacion, usuario, evento)) {
                throw new IllegalArgumentException("No tienes permiso para editar este evento.");
            }
            if (maxParticipantes != null
                    && inscripcionEventoRepository.buscarInscripcionesParaActualizar(evento).size() > maxParticipantes) {
                throw new IllegalArgumentException("El máximo de participantes no puede ser menor que las inscripciones actuales.");
            }
        }
        evento.setSistemaJuego(sistemaJuego);
        evento.setTitulo(titulo);
        evento.setDescripcion(descripcion.isBlank() ? null : descripcion);
        evento.setTipoEvento(ComunidadConstantes.TIPO_EVENTO_COMUNIDAD);
        evento.setSistemaClasificacion(ComunidadConstantes.SISTEMA_CLASIFICACION_SUIZO);
        evento.setRondasPlanificadas(rondas);
        evento.setMaxParticipantes(maxParticipantes);
        evento.setUbicacion(lugar);
        evento.setLatitud(BigDecimal.valueOf(latitud));
        evento.setLongitud(BigDecimal.valueOf(longitud));
        evento.setCiudad(null);
        evento.setFechaLimiteInscripcion(fecha);
        evento.setInicioEn(fecha);
        evento.setFinEn(fecha.plusHours((long) rondas * 3L));
        evento.setActualizadoEn(ahora);
        eventoRepository.save(evento);

        return comunidad.getId();
    }

    @Transactional
    public void eliminarEvento(Long usuarioId, Long comunidadId, Long eventoId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Comunidad comunidad = comunidadMiembroService.buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacion = comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);
        Evento evento = bloquearEvento(eventoId);
        if (evento.getComunidad() == null || !evento.getComunidad().getId().equals(comunidad.getId())) {
            throw new IllegalArgumentException("El evento no pertenece a esta comunidad.");
        }
        if (!puedeGestionar(afiliacion, usuario, evento)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este evento.");
        }
        evento.setEstado(ComunidadConstantes.ESTADO_EVENTO_CANCELADO);
        evento.setActualizadoEn(LocalDateTime.now());
        eventoRepository.save(evento);
    }

    @Transactional
    public Long unirseAEvento(Long usuarioId, Long eventoId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Evento evento = bloquearEvento(eventoId);
        Comunidad comunidad = evento.getComunidad();

        comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);

        if (!ComunidadConstantes.ESTADO_EVENTO_ABIERTO.equals(evento.getEstado())) {
            throw new IllegalArgumentException("Las inscripciones de este evento no están abiertas.");
        }

        // Evento actúa como mutex compartido entre procesos hasta commit/rollback.
        List<InscripcionEvento> inscripciones = inscripcionEventoRepository.buscarInscripcionesParaActualizar(evento);
        if (inscripciones.stream().anyMatch(inscripcion -> inscripcion.getUsuario().getId().equals(usuarioId))) {
            throw new IllegalArgumentException("Ya estas inscrito en este evento.");
        }
        if (evento.getMaxParticipantes() != null
                && inscripciones.size() >= evento.getMaxParticipantes()) {
            throw new IllegalArgumentException("El evento ha alcanzado el máximo de participantes.");
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

    @Transactional
    public Long desinscribirseDeEvento(Long usuarioId, Long eventoId) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Evento evento = bloquearEvento(eventoId);
        Comunidad comunidad = evento.getComunidad();
        comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);

        InscripcionEvento inscripcion = inscripcionEventoRepository.buscarInscripcionesParaActualizar(evento).stream()
                .filter(actual -> actual.getUsuario().getId().equals(usuarioId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No estas inscrito en este evento."));
        inscripcionEventoRepository.delete(inscripcion);
        return comunidad.getId();
    }

    public List<Evento> buscarEventos(Comunidad comunidad) {
        return eventoRepository.findByComunidadAndEstadoNotOrderByInicioEnAsc(
                comunidad,
                ComunidadConstantes.ESTADO_EVENTO_CANCELADO
        );
    }

    public Evento buscarEvento(Long eventoId) {
        if (eventoId == null) {
            throw new IllegalArgumentException("Debes seleccionar un evento.");
        }
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el evento."));
    }

    private Evento bloquearEvento(Long eventoId) {
        if (eventoId == null) {
            throw new IllegalArgumentException("Debes seleccionar un evento.");
        }
        return eventoRepository.findByIdForUpdate(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el evento."));
    }

    public boolean usuarioInscrito(Evento evento, Usuario usuario) {
        return inscripcionEventoRepository.existsByEventoAndUsuario(evento, usuario);
    }

    public long contarInscritos(Evento evento) {
        return inscripcionEventoRepository.countByEvento(evento);
    }

    public boolean puedeGestionar(AfiliacionComunidad afiliacion, Usuario usuario, Evento evento) {
        return ComunidadConstantes.esAdministrador(afiliacion.getRolComunidad())
                || evento.getOrganizadorUsuario().getId().equals(usuario.getId());
    }

    public boolean permiteInscripcion(Evento evento, long totalInscritos) {
        return ComunidadConstantes.ESTADO_EVENTO_ABIERTO.equals(evento.getEstado())
                && (evento.getMaxParticipantes() == null || totalInscritos < evento.getMaxParticipantes());
    }

    public List<InscripcionEvento> buscarInscripcionesProximas(Usuario usuario, LocalDateTime inicioDesde) {
        return inscripcionEventoRepository
                .buscarProximasDelUsuario(
                        usuario,
                        ComunidadConstantes.ESTADO_INSCRIPCION_CONFIRMADA,
                        inicioDesde
                );
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
