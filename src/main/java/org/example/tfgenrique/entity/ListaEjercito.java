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
@Table(name = "listas_ejercito")
public class ListaEjercito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietario_usuario_id", nullable = false)
    private Usuario propietarioUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sistema_juego_id", nullable = false)
    private SistemaJuego sistemaJuego;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "nombre_faccion_snapshot", length = 120)
    private String nombreFaccionSnapshot;

    @Column(name = "limite_puntos", nullable = false)
    private Integer limitePuntos;

    @Column(name = "puntos_actuales", nullable = false)
    private Integer puntosActuales;

    @Column(nullable = false, length = 20)
    private String visibilidad;

    @Column(name = "numero_version_actual", nullable = false)
    private Integer numeroVersionActual;

    @Column(nullable = false)
    private Boolean archivada;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public ListaEjercito() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getPropietarioUsuario() { return propietarioUsuario; }
    public void setPropietarioUsuario(Usuario propietarioUsuario) { this.propietarioUsuario = propietarioUsuario; }
    public SistemaJuego getSistemaJuego() { return sistemaJuego; }
    public void setSistemaJuego(SistemaJuego sistemaJuego) { this.sistemaJuego = sistemaJuego; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreFaccionSnapshot() { return nombreFaccionSnapshot; }
    public void setNombreFaccionSnapshot(String nombreFaccionSnapshot) { this.nombreFaccionSnapshot = nombreFaccionSnapshot; }
    public Integer getLimitePuntos() { return limitePuntos; }
    public void setLimitePuntos(Integer limitePuntos) { this.limitePuntos = limitePuntos; }
    public Integer getPuntosActuales() { return puntosActuales; }
    public void setPuntosActuales(Integer puntosActuales) { this.puntosActuales = puntosActuales; }
    public String getVisibilidad() { return visibilidad; }
    public void setVisibilidad(String visibilidad) { this.visibilidad = visibilidad; }
    public Integer getNumeroVersionActual() { return numeroVersionActual; }
    public void setNumeroVersionActual(Integer numeroVersionActual) { this.numeroVersionActual = numeroVersionActual; }
    public Boolean getArchivada() { return archivada; }
    public void setArchivada(Boolean archivada) { this.archivada = archivada; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
