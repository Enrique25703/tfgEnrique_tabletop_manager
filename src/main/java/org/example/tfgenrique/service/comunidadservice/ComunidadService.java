package org.example.tfgenrique.service.comunidadservice;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComunidadService {

    private final ComunidadViewService comunidadViewService;
    private final ComunidadMiembroService comunidadMiembroService;
    private final ComunidadEventoService comunidadEventoService;
    private final ComunidadInvitacionService comunidadInvitacionService;
    private final ComunidadSolicitudService comunidadSolicitudService;

    public ComunidadService(
            ComunidadViewService comunidadViewService,
            ComunidadMiembroService comunidadMiembroService,
            ComunidadEventoService comunidadEventoService,
            ComunidadInvitacionService comunidadInvitacionService,
            ComunidadSolicitudService comunidadSolicitudService
    ) {
        this.comunidadViewService = comunidadViewService;
        this.comunidadMiembroService = comunidadMiembroService;
        this.comunidadEventoService = comunidadEventoService;
        this.comunidadInvitacionService = comunidadInvitacionService;
        this.comunidadSolicitudService = comunidadSolicitudService;
    }

    public ComunidadPaginaView prepararPagina(Long usuarioId, Long comunidadId) {
        return comunidadViewService.prepararPagina(usuarioId, comunidadId);
    }

    public EventosCercanosPaginaView prepararEventosCercanos(Long usuarioId) {
        return comunidadViewService.prepararEventosCercanos(usuarioId);
    }

    public MisComunidadesPaginaView prepararMisComunidades(Long usuarioId) {
        return comunidadViewService.prepararMisComunidades(usuarioId);
    }

    public DescubrirComunidadesPaginaView prepararDescubrirComunidades(
            Long usuarioId,
            Long comunidadId,
            String busqueda
    ) {
        return comunidadViewService.prepararDescubrirComunidades(usuarioId, comunidadId, busqueda);
    }

    public Long crearComunidad(Long usuarioId, String nombreComunidad, String descripcion) {
        return comunidadMiembroService.crearComunidad(usuarioId, nombreComunidad, descripcion);
    }

    public Long crearComunidad(Long usuarioId, String nombreComunidad, String descripcion, String logoUrl) {
        return comunidadMiembroService.crearComunidad(usuarioId, nombreComunidad, descripcion, logoUrl);
    }

    public Long unirseAComunidad(Long usuarioId, Long comunidadId) {
        return comunidadSolicitudService.solicitarOUnirse(usuarioId, comunidadId).comunidadId();
    }

    public ComunidadSolicitudService.ResultadoUnion solicitarOUnirse(Long usuarioId, Long comunidadId) {
        return comunidadSolicitudService.solicitarOUnirse(usuarioId, comunidadId);
    }

    public Long crearEvento(Long usuarioId, Long comunidadId, CrearEventoRequest request) {
        return comunidadEventoService.crearEvento(usuarioId, comunidadId, request);
    }

    public Long unirseAEvento(Long usuarioId, Long eventoId) {
        return comunidadEventoService.unirseAEvento(usuarioId, eventoId);
    }

    public Long crearInvitacionPartida(Long usuarioId, Long comunidadId, CrearInvitacionPartidaRequest request) {
        return comunidadInvitacionService.crearInvitacionPartida(usuarioId, comunidadId, request);
    }

    public static class CrearEventoRequest {
        private String titulo;
        private String descripcion;
        private LocalDateTime fecha;
        private Integer numeroRondas;
        private Integer maxParticipantes;
        private String lugar;
        private String formatoJuego;
        private Double latitud;
        private Double longitud;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public void setFecha(LocalDateTime fecha) {
            this.fecha = fecha;
        }

        public Integer getNumeroRondas() {
            return numeroRondas;
        }

        public void setNumeroRondas(Integer numeroRondas) {
            this.numeroRondas = numeroRondas;
        }

        public Integer getMaxParticipantes() { return maxParticipantes; }
        public void setMaxParticipantes(Integer maxParticipantes) { this.maxParticipantes = maxParticipantes; }

        public String getLugar() {
            return lugar;
        }

        public void setLugar(String lugar) {
            this.lugar = lugar;
        }

        public String getFormatoJuego() {
            return formatoJuego;
        }

        public void setFormatoJuego(String formatoJuego) {
            this.formatoJuego = formatoJuego;
        }

        public Double getLatitud() {
            return latitud;
        }

        public void setLatitud(Double latitud) {
            this.latitud = latitud;
        }

        public Double getLongitud() {
            return longitud;
        }

        public void setLongitud(Double longitud) {
            this.longitud = longitud;
        }
    }

    public static class CrearInvitacionPartidaRequest {
        private LocalDateTime fecha;
        private String lugar;
        private String formatoJuego;
        private String mensaje;

        public LocalDateTime getFecha() {
            return fecha;
        }

        public void setFecha(LocalDateTime fecha) {
            this.fecha = fecha;
        }

        public String getLugar() {
            return lugar;
        }

        public void setLugar(String lugar) {
            this.lugar = lugar;
        }

        public String getFormatoJuego() {
            return formatoJuego;
        }

        public void setFormatoJuego(String formatoJuego) {
            this.formatoJuego = formatoJuego;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }

    public record EventosCercanosPaginaView(
            String nombreUsuario,
            int totalComunidades,
            int totalSugerencias,
            List<EventoActualView> eventosProximos
    ) {
        public int totalEventosProximos() {
            return eventosProximos.size();
        }
    }

    public record MisComunidadesPaginaView(
            String nombreUsuario,
            List<ComunidadResumenView> comunidades
    ) {
        public int totalComunidades() {
            return comunidades.size();
        }
    }

    public record DescubrirComunidadesPaginaView(
            String nombreUsuario,
            String busqueda,
            List<ComunidadDescubrimientoView> comunidades,
            ComunidadDetalleDescubrimientoView seleccionada
    ) {
        public int totalComunidades() {
            return comunidades.size();
        }
    }

    public record ComunidadDescubrimientoView(
            Long id,
            String nombre,
            String descripcion,
            String logoUrl,
            boolean privada,
            int totalMiembros,
            int totalEventos,
            List<String> juegos,
            boolean solicitudPendiente
    ) {
    }

    public record ComunidadDetalleDescubrimientoView(
            ComunidadDescubrimientoView comunidad,
            List<EventoDescubrimientoView> proximosEventos
    ) {
    }

    public record EventoDescubrimientoView(
            Long id,
            String titulo,
            String dia,
            String mes,
            String fecha,
            String formato,
            String ubicacion,
            Integer plazasDisponibles
    ) {
    }

    public static class ComunidadPaginaView {
        private final String nombreUsuario;
        private final List<ComunidadResumenView> misComunidades;
        private final List<ComunidadResumenView> comunidadesDisponibles;
        private final ComunidadDetalleView comunidadSeleccionada;
        private final List<EventoActualView> eventosProximos;

        public ComunidadPaginaView(
                String nombreUsuario,
                List<ComunidadResumenView> misComunidades,
                List<ComunidadResumenView> comunidadesDisponibles,
                ComunidadDetalleView comunidadSeleccionada,
                List<EventoActualView> eventosProximos
        ) {
            this.nombreUsuario = nombreUsuario;
            this.misComunidades = misComunidades;
            this.comunidadesDisponibles = comunidadesDisponibles;
            this.comunidadSeleccionada = comunidadSeleccionada;
            this.eventosProximos = eventosProximos;
        }

        public String getNombreUsuario() {
            return nombreUsuario;
        }

        public List<ComunidadResumenView> getMisComunidades() {
            return misComunidades;
        }

        public List<ComunidadResumenView> getComunidadesDisponibles() {
            return comunidadesDisponibles;
        }

        public ComunidadDetalleView getComunidadSeleccionada() {
            return comunidadSeleccionada;
        }

        public List<EventoActualView> getEventosProximos() {
            return eventosProximos;
        }

        public int getTotalComunidades() {
            return misComunidades.size();
        }

        public int getTotalEventosProximos() {
            return eventosProximos.size();
        }

        public int getTotalSugerencias() {
            return comunidadesDisponibles.size();
        }
    }

    public static class EventoActualView {
        private final Long id;
        private final String titulo;
        private final String fecha;
        private final String hora;
        private final String formato;
        private final String codigoFormato;
        private final String ubicacion;
        private final String latitud;
        private final String longitud;
        private final boolean tieneCoordenadas;
        private final String comunidad;
        private final String organizador;
        private final int rondas;
        private final String descripcion;

        public EventoActualView(
                Long id,
                String titulo,
                String fecha,
                String hora,
                String formato,
                String codigoFormato,
                String ubicacion,
                String latitud,
                String longitud,
                boolean tieneCoordenadas,
                String comunidad,
                String organizador,
                int rondas,
                String descripcion
        ) {
            this.id = id;
            this.titulo = titulo;
            this.fecha = fecha;
            this.hora = hora;
            this.formato = formato;
            this.codigoFormato = codigoFormato;
            this.ubicacion = ubicacion;
            this.latitud = latitud;
            this.longitud = longitud;
            this.tieneCoordenadas = tieneCoordenadas;
            this.comunidad = comunidad;
            this.organizador = organizador;
            this.rondas = rondas;
            this.descripcion = descripcion;
        }

        public Long getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getFecha() { return fecha; }
        public String getHora() { return hora; }
        public String getFormato() { return formato; }
        public String getCodigoFormato() { return codigoFormato; }
        public String getUbicacion() { return ubicacion; }
        public String getLatitud() { return latitud; }
        public String getLongitud() { return longitud; }
        public boolean isTieneCoordenadas() { return tieneCoordenadas; }
        public String getComunidad() { return comunidad; }
        public String getOrganizador() { return organizador; }
        public int getRondas() { return rondas; }
        public String getDescripcion() { return descripcion; }
    }

    public static class ComunidadResumenView {
        private final Long id;
        private final String nombre;
        private final String descripcion;
        private final String logoUrl;
        private final String rolUsuario;
        private final boolean privada;
        private final int totalMiembros;
        private final int totalEventos;

        public ComunidadResumenView(
                Long id,
                String nombre,
                String descripcion,
                String logoUrl,
                String rolUsuario,
                boolean privada,
                int totalMiembros,
                int totalEventos
        ) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.logoUrl = logoUrl;
            this.rolUsuario = rolUsuario;
            this.privada = privada;
            this.totalMiembros = totalMiembros;
            this.totalEventos = totalEventos;
        }

        public Long getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public String getRolUsuario() {
            return rolUsuario;
        }

        public boolean isPrivada() {
            return privada;
        }

        public int getTotalMiembros() {
            return totalMiembros;
        }

        public int getTotalEventos() {
            return totalEventos;
        }
    }

    public static class ComunidadDetalleView {
        private final Long id;
        private final String nombre;
        private final String rolUsuario;
        private final boolean propietario;
        private final boolean usuarioNormal;
        private final List<MiembroComunidadView> miembros;
        private final List<EventoComunidadView> eventos;
        private final List<InvitacionPartidaView> invitaciones;

        public ComunidadDetalleView(
                Long id,
                String nombre,
                String rolUsuario,
                boolean propietario,
                boolean usuarioNormal,
                List<MiembroComunidadView> miembros,
                List<EventoComunidadView> eventos,
                List<InvitacionPartidaView> invitaciones
        ) {
            this.id = id;
            this.nombre = nombre;
            this.rolUsuario = rolUsuario;
            this.propietario = propietario;
            this.usuarioNormal = usuarioNormal;
            this.miembros = miembros;
            this.eventos = eventos;
            this.invitaciones = invitaciones;
        }

        public Long getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getRolUsuario() {
            return rolUsuario;
        }

        public boolean isPropietario() {
            return propietario;
        }

        public boolean isUsuarioNormal() {
            return usuarioNormal;
        }

        public List<MiembroComunidadView> getMiembros() {
            return miembros;
        }

        public List<EventoComunidadView> getEventos() {
            return eventos;
        }

        public List<InvitacionPartidaView> getInvitaciones() {
            return invitaciones;
        }
    }

    public static class MiembroComunidadView {
        private final String nombreUsuario;
        private final String rol;

        public MiembroComunidadView(String nombreUsuario, String rol) {
            this.nombreUsuario = nombreUsuario;
            this.rol = rol;
        }

        public String getNombreUsuario() {
            return nombreUsuario;
        }

        public String getRol() {
            return rol;
        }
    }

    public static class EventoComunidadView {
        private final Long id;
        private final String titulo;
        private final String formato;
        private final String fecha;
        private final int rondas;
        private final String lugar;
        private final String organizador;
        private final long inscritos;
        private final boolean unido;
        private final boolean puedeUnirse;

        public EventoComunidadView(
                Long id,
                String titulo,
                String formato,
                String fecha,
                int rondas,
                String lugar,
                String organizador,
                long inscritos,
                boolean unido,
                boolean puedeUnirse
        ) {
            this.id = id;
            this.titulo = titulo;
            this.formato = formato;
            this.fecha = fecha;
            this.rondas = rondas;
            this.lugar = lugar;
            this.organizador = organizador;
            this.inscritos = inscritos;
            this.unido = unido;
            this.puedeUnirse = puedeUnirse;
        }

        public Long getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getFormato() {
            return formato;
        }

        public String getFecha() {
            return fecha;
        }

        public int getRondas() {
            return rondas;
        }

        public String getLugar() {
            return lugar;
        }

        public String getOrganizador() {
            return organizador;
        }

        public long getInscritos() {
            return inscritos;
        }

        public boolean isUnido() {
            return unido;
        }

        public boolean isPuedeUnirse() {
            return puedeUnirse;
        }
    }

    public static class InvitacionPartidaView {
        private final String creador;
        private final String formato;
        private final String fecha;
        private final String lugar;
        private final String mensaje;

        public InvitacionPartidaView(String creador, String formato, String fecha, String lugar, String mensaje) {
            this.creador = creador;
            this.formato = formato;
            this.fecha = fecha;
            this.lugar = lugar;
            this.mensaje = mensaje;
        }

        public String getCreador() {
            return creador;
        }

        public String getFormato() {
            return formato;
        }

        public String getFecha() {
            return fecha;
        }

        public String getLugar() {
            return lugar;
        }

        public String getMensaje() {
            return mensaje;
        }
    }
}
