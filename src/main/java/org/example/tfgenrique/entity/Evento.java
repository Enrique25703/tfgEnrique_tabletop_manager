package org.example.tfgenrique.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizador_usuario_id", nullable = false)
    private Usuario organizadorUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id")
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sistema_juego_id", nullable = false)
    private SistemaJuego sistemaJuego;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "tipo_evento", nullable = false, length = 20)
    private String tipoEvento;

    @Column(name = "sistema_clasificacion", length = 10)
    private String sistemaClasificacion;

    @Column(name = "rondas_planificadas")
    private Integer rondasPlanificadas;

    @Column(name = "max_participantes")
    private Integer maxParticipantes;

    @Column(length = 150)
    private String ubicacion;

    @Column(length = 80)
    private String ciudad;

    @Column(name = "fecha_limite_inscripcion")
    private LocalDateTime fechaLimiteInscripcion;

    @Column(name = "inicio_en", nullable = false)
    private LocalDateTime inicioEn;

    @Column(name = "fin_en")
    private LocalDateTime finEn;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public Evento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getOrganizadorUsuario() { return organizadorUsuario; }
    public void setOrganizadorUsuario(Usuario organizadorUsuario) { this.organizadorUsuario = organizadorUsuario; }
    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }
    public SistemaJuego getSistemaJuego() { return sistemaJuego; }
    public void setSistemaJuego(SistemaJuego sistemaJuego) { this.sistemaJuego = sistemaJuego; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getSistemaClasificacion() { return sistemaClasificacion; }
    public void setSistemaClasificacion(String sistemaClasificacion) { this.sistemaClasificacion = sistemaClasificacion; }
    public Integer getRondasPlanificadas() { return rondasPlanificadas; }
    public void setRondasPlanificadas(Integer rondasPlanificadas) { this.rondasPlanificadas = rondasPlanificadas; }
    public Integer getMaxParticipantes() { return maxParticipantes; }
    public void setMaxParticipantes(Integer maxParticipantes) { this.maxParticipantes = maxParticipantes; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public LocalDateTime getFechaLimiteInscripcion() { return fechaLimiteInscripcion; }
    public void setFechaLimiteInscripcion(LocalDateTime fechaLimiteInscripcion) { this.fechaLimiteInscripcion = fechaLimiteInscripcion; }
    public LocalDateTime getInicioEn() { return inicioEn; }
    public void setInicioEn(LocalDateTime inicioEn) { this.inicioEn = inicioEn; }
    public LocalDateTime getFinEn() { return finEn; }
    public void setFinEn(LocalDateTime finEn) { this.finEn = finEn; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
