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
        name = "afiliaciones_comunidad",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comunidad_id", "usuario_id"})
)
public class AfiliacionComunidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comunidad_id", nullable = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "rol_comunidad", nullable = false, length = 20)
    private String rolComunidad;

    @Column(name = "estado_afiliacion", nullable = false, length = 20)
    private String estadoAfiliacion;

    @Column(name = "unido_en", nullable = false)
    private LocalDateTime unidoEn;

    public AfiliacionComunidad() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getRolComunidad() { return rolComunidad; }
    public void setRolComunidad(String rolComunidad) { this.rolComunidad = rolComunidad; }
    public String getEstadoAfiliacion() { return estadoAfiliacion; }
    public void setEstadoAfiliacion(String estadoAfiliacion) { this.estadoAfiliacion = estadoAfiliacion; }
    public LocalDateTime getUnidoEn() { return unidoEn; }
    public void setUnidoEn(LocalDateTime unidoEn) { this.unidoEn = unidoEn; }
}
