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

    public ComunidadService(
            ComunidadViewService comunidadViewService,
            ComunidadMiembroService comunidadMiembroService,
            ComunidadEventoService comunidadEventoService,
            ComunidadInvitacionService comunidadInvitacionService
    ) {
        this.comunidadViewService = comunidadViewService;
        this.comunidadMiembroService = comunidadMiembroService;
        this.comunidadEventoService = comunidadEventoService;
        this.comunidadInvitacionService = comunidadInvitacionService;
    }

    public ComunidadPaginaView prepararPagina(Long usuarioId, Long comunidadId) {
        return comunidadViewService.prepararPagina(usuarioId, comunidadId);
    }

    public Long crearComunidad(Long usuarioId, String nombreComunidad) {
        return comunidadMiembroService.crearComunidad(usuarioId, nombreComunidad);
    }

    public Long unirseAComunidad(Long usuarioId, Long comunidadId) {
        return comunidadMiembroService.unirseAComunidad(usuarioId, comunidadId);
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
        private LocalDateTime fecha;
        private Integer numeroRondas;
        private String lugar;
        private String formatoJuego;

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

    public static class ComunidadPaginaView {
        private final String nombreUsuario;
        private final List<ComunidadResumenView> misComunidades;
        private final List<ComunidadResumenView> comunidadesDisponibles;
        private final ComunidadDetalleView comunidadSeleccionada;

        public ComunidadPaginaView(
                String nombreUsuario,
                List<ComunidadResumenView> misComunidades,
                List<ComunidadResumenView> comunidadesDisponibles,
                ComunidadDetalleView comunidadSeleccionada
        ) {
            this.nombreUsuario = nombreUsuario;
            this.misComunidades = misComunidades;
            this.comunidadesDisponibles = comunidadesDisponibles;
            this.comunidadSeleccionada = comunidadSeleccionada;
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
    }

    public static class ComunidadResumenView {
        private final Long id;
        private final String nombre;
        private final String rolUsuario;
        private final int totalMiembros;
        private final int totalEventos;

        public ComunidadResumenView(Long id, String nombre, String rolUsuario, int totalMiembros, int totalEventos) {
            this.id = id;
            this.nombre = nombre;
            this.rolUsuario = rolUsuario;
            this.totalMiembros = totalMiembros;
            this.totalEventos = totalEventos;
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
