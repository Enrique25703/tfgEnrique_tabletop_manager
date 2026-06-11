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
@Table(name = "invitaciones_partida_comunidad")
public class InvitacionPartidaComunidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comunidad_id", nullable = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creador_usuario_id", nullable = false)
    private Usuario creadorUsuario;

    @Column(name = "formato_juego", nullable = false, length = 30)
    private String formatoJuego;

    @Column(nullable = false, length = 150)
    private String lugar;

    @Column(name = "fecha_propuesta", nullable = false)
    private LocalDateTime fechaPropuesta;

    @Column(columnDefinition = "text")
    private String mensaje;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    public InvitacionPartidaComunidad() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comunidad getComunidad() { return comunidad; }
    public void setComunidad(Comunidad comunidad) { this.comunidad = comunidad; }
    public Usuario getCreadorUsuario() { return creadorUsuario; }
    public void setCreadorUsuario(Usuario creadorUsuario) { this.creadorUsuario = creadorUsuario; }
    public String getFormatoJuego() { return formatoJuego; }
    public void setFormatoJuego(String formatoJuego) { this.formatoJuego = formatoJuego; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDateTime fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
}
