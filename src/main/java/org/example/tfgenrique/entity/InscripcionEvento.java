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
@Table(name = "inscripciones_evento", uniqueConstraints = @UniqueConstraint(
        name = "uk_inscripciones_evento_evento_usuario", columnNames = {"evento_id", "usuario_id"}))
public class InscripcionEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_lista_ejercito_id")
    private VersionListaEjercito versionListaEjercito;

    @Column(name = "nombre_lista_enviada", length = 150)
    private String nombreListaEnviada;

    @Column(name = "puntos_enviados")
    private Integer puntosEnviados;

    @Column(name = "estado_inscripcion", nullable = false, length = 20)
    private String estadoInscripcion;

    @Column(name = "inscrito_en", nullable = false)
    private LocalDateTime inscritoEn;

    public InscripcionEvento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public VersionListaEjercito getVersionListaEjercito() { return versionListaEjercito; }
    public void setVersionListaEjercito(VersionListaEjercito versionListaEjercito) { this.versionListaEjercito = versionListaEjercito; }
    public String getNombreListaEnviada() { return nombreListaEnviada; }
    public void setNombreListaEnviada(String nombreListaEnviada) { this.nombreListaEnviada = nombreListaEnviada; }
    public Integer getPuntosEnviados() { return puntosEnviados; }
    public void setPuntosEnviados(Integer puntosEnviados) { this.puntosEnviados = puntosEnviados; }
    public String getEstadoInscripcion() { return estadoInscripcion; }
    public void setEstadoInscripcion(String estadoInscripcion) { this.estadoInscripcion = estadoInscripcion; }
    public LocalDateTime getInscritoEn() { return inscritoEn; }
    public void setInscritoEn(LocalDateTime inscritoEn) { this.inscritoEn = inscritoEn; }
}
