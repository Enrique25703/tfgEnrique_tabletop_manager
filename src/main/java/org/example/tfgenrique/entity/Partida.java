package org.example.tfgenrique.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "partidas")
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id")
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por_usuario_id", nullable = false)
    private Usuario creadoPorUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sistema_juego_id", nullable = false)
    private SistemaJuego sistemaJuego;

    @Column(name = "numero_ronda")
    private Integer numeroRonda;

    @Column(name = "numero_mesa")
    private Integer numeroMesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador1_usuario_id", nullable = false)
    private Usuario jugador1Usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador1_version_lista_id")
    private VersionListaEjercito jugador1VersionLista;

    @Column(name = "jugador1_nombre_snapshot", length = 150)
    private String jugador1NombreSnapshot;

    @Column(name = "jugador1_faccion_snapshot", length = 150)
    private String jugador1FaccionSnapshot;

    @Column(name = "jugador1_nombre_lista_snapshot", length = 150)
    private String jugador1NombreListaSnapshot;

    @Column(name = "jugador1_puntos_snapshot")
    private Integer jugador1PuntosSnapshot;

    @Column(name = "jugador1_puntuacion_total", nullable = false)
    private Integer jugador1PuntuacionTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador2_usuario_id")
    private Usuario jugador2Usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador2_version_lista_id")
    private VersionListaEjercito jugador2VersionLista;

    @Column(name = "jugador2_nombre_snapshot", length = 150)
    private String jugador2NombreSnapshot;

    @Column(name = "jugador2_faccion_snapshot", length = 150)
    private String jugador2FaccionSnapshot;

    @Column(name = "jugador2_nombre_lista_snapshot", length = 150)
    private String jugador2NombreListaSnapshot;

    @Column(name = "jugador2_puntos_snapshot")
    private Integer jugador2PuntosSnapshot;

    @Column(name = "jugador2_puntuacion_total", nullable = false)
    private Integer jugador2PuntuacionTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ganador_usuario_id")
    private Usuario ganadorUsuario;

    @Column(name = "es_empate", nullable = false)
    private Boolean esEmpate;

    @Column(name = "nombre_mision", length = 150)
    private String nombreMision;

    @Column(name = "layout_mision", length = 50)
    private String layoutMision;

    @Column(name = "despliegue_mision", length = 150)
    private String despliegueMision;

    @Column(name = "estilo_juego", length = 30)
    private String estiloJuego;

    @Column(name = "jugador_defensor", length = 20)
    private String jugadorDefensor;

    @Column(name = "jugador_primero", length = 20)
    private String jugadorPrimero;

    @Column(name = "mostrar_command_points")
    private Boolean mostrarCommandPoints;

    @Column(name = "usar_cartas_giro")
    private Boolean usarCartasGiro;

    @Column(columnDefinition = "text")
    private String notas;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "programada_en")
    private LocalDateTime programadaEn;

    @Column(name = "iniciada_en")
    private LocalDateTime iniciadaEn;

    @Column(name = "finalizada_en")
    private LocalDateTime finalizadaEn;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public Partida() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }
    public Usuario getCreadoPorUsuario() { return creadoPorUsuario; }
    public void setCreadoPorUsuario(Usuario creadoPorUsuario) { this.creadoPorUsuario = creadoPorUsuario; }
    public SistemaJuego getSistemaJuego() { return sistemaJuego; }
    public void setSistemaJuego(SistemaJuego sistemaJuego) { this.sistemaJuego = sistemaJuego; }
    public Integer getNumeroRonda() { return numeroRonda; }
    public void setNumeroRonda(Integer numeroRonda) { this.numeroRonda = numeroRonda; }
    public Integer getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(Integer numeroMesa) { this.numeroMesa = numeroMesa; }
    public Usuario getJugador1Usuario() { return jugador1Usuario; }
    public void setJugador1Usuario(Usuario jugador1Usuario) { this.jugador1Usuario = jugador1Usuario; }
    public VersionListaEjercito getJugador1VersionLista() { return jugador1VersionLista; }
    public void setJugador1VersionLista(VersionListaEjercito jugador1VersionLista) { this.jugador1VersionLista = jugador1VersionLista; }
    public String getJugador1NombreSnapshot() { return jugador1NombreSnapshot; }
    public void setJugador1NombreSnapshot(String jugador1NombreSnapshot) { this.jugador1NombreSnapshot = jugador1NombreSnapshot; }
    public String getJugador1FaccionSnapshot() { return jugador1FaccionSnapshot; }
    public void setJugador1FaccionSnapshot(String jugador1FaccionSnapshot) { this.jugador1FaccionSnapshot = jugador1FaccionSnapshot; }
    public String getJugador1NombreListaSnapshot() { return jugador1NombreListaSnapshot; }
    public void setJugador1NombreListaSnapshot(String jugador1NombreListaSnapshot) { this.jugador1NombreListaSnapshot = jugador1NombreListaSnapshot; }
    public Integer getJugador1PuntosSnapshot() { return jugador1PuntosSnapshot; }
    public void setJugador1PuntosSnapshot(Integer jugador1PuntosSnapshot) { this.jugador1PuntosSnapshot = jugador1PuntosSnapshot; }
    public Integer getJugador1PuntuacionTotal() { return jugador1PuntuacionTotal; }
    public void setJugador1PuntuacionTotal(Integer jugador1PuntuacionTotal) { this.jugador1PuntuacionTotal = jugador1PuntuacionTotal; }
    public Usuario getJugador2Usuario() { return jugador2Usuario; }
    public void setJugador2Usuario(Usuario jugador2Usuario) { this.jugador2Usuario = jugador2Usuario; }
    public VersionListaEjercito getJugador2VersionLista() { return jugador2VersionLista; }
    public void setJugador2VersionLista(VersionListaEjercito jugador2VersionLista) { this.jugador2VersionLista = jugador2VersionLista; }
    public String getJugador2NombreSnapshot() { return jugador2NombreSnapshot; }
    public void setJugador2NombreSnapshot(String jugador2NombreSnapshot) { this.jugador2NombreSnapshot = jugador2NombreSnapshot; }
    public String getJugador2FaccionSnapshot() { return jugador2FaccionSnapshot; }
    public void setJugador2FaccionSnapshot(String jugador2FaccionSnapshot) { this.jugador2FaccionSnapshot = jugador2FaccionSnapshot; }
    public String getJugador2NombreListaSnapshot() { return jugador2NombreListaSnapshot; }
    public void setJugador2NombreListaSnapshot(String jugador2NombreListaSnapshot) { this.jugador2NombreListaSnapshot = jugador2NombreListaSnapshot; }
    public Integer getJugador2PuntosSnapshot() { return jugador2PuntosSnapshot; }
    public void setJugador2PuntosSnapshot(Integer jugador2PuntosSnapshot) { this.jugador2PuntosSnapshot = jugador2PuntosSnapshot; }
    public Integer getJugador2PuntuacionTotal() { return jugador2PuntuacionTotal; }
    public void setJugador2PuntuacionTotal(Integer jugador2PuntuacionTotal) { this.jugador2PuntuacionTotal = jugador2PuntuacionTotal; }
    public Usuario getGanadorUsuario() { return ganadorUsuario; }
    public void setGanadorUsuario(Usuario ganadorUsuario) { this.ganadorUsuario = ganadorUsuario; }
    public Boolean getEsEmpate() { return esEmpate; }
    public void setEsEmpate(Boolean esEmpate) { this.esEmpate = esEmpate; }
    public String getNombreMision() { return nombreMision; }
    public void setNombreMision(String nombreMision) { this.nombreMision = nombreMision; }
    public String getLayoutMision() { return layoutMision; }
    public void setLayoutMision(String layoutMision) { this.layoutMision = layoutMision; }
    public String getDespliegueMision() { return despliegueMision; }
    public void setDespliegueMision(String despliegueMision) { this.despliegueMision = despliegueMision; }
    public String getEstiloJuego() { return estiloJuego; }
    public void setEstiloJuego(String estiloJuego) { this.estiloJuego = estiloJuego; }
    public String getJugadorDefensor() { return jugadorDefensor; }
    public void setJugadorDefensor(String jugadorDefensor) { this.jugadorDefensor = jugadorDefensor; }
    public String getJugadorPrimero() { return jugadorPrimero; }
    public void setJugadorPrimero(String jugadorPrimero) { this.jugadorPrimero = jugadorPrimero; }
    public Boolean getMostrarCommandPoints() { return mostrarCommandPoints; }
    public void setMostrarCommandPoints(Boolean mostrarCommandPoints) { this.mostrarCommandPoints = mostrarCommandPoints; }
    public Boolean getUsarCartasGiro() { return usarCartasGiro; }
    public void setUsarCartasGiro(Boolean usarCartasGiro) { this.usarCartasGiro = usarCartasGiro; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getProgramadaEn() { return programadaEn; }
    public void setProgramadaEn(LocalDateTime programadaEn) { this.programadaEn = programadaEn; }
    public LocalDateTime getIniciadaEn() { return iniciadaEn; }
    public void setIniciadaEn(LocalDateTime iniciadaEn) { this.iniciadaEn = iniciadaEn; }
    public LocalDateTime getFinalizadaEn() { return finalizadaEn; }
    public void setFinalizadaEn(LocalDateTime finalizadaEn) { this.finalizadaEn = finalizadaEn; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
