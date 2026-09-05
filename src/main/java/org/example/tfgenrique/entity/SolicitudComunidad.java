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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "solicitudes_comunidad",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comunidad_id", "usuario_id"})
)
public class SolicitudComunidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comunidad_id", nullable = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "resuelta_en")
    private LocalDateTime resueltaEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public LocalDateTime getResueltaEn() { return resueltaEn; }
    public void setResueltaEn(LocalDateTime resueltaEn) { this.resueltaEn = resueltaEn; }
}
