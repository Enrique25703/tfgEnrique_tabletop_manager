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
@Table(name = "estados_catalogo_usuario")
public class EstadoCatalogoUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sistema_juego_id", nullable = false)
    private SistemaJuego sistemaJuego;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fuente_catalogo_id", nullable = false)
    private FuenteCatalogo fuenteCatalogo;

    @Column(name = "ultima_comprobacion_en")
    private LocalDateTime ultimaComprobacionEn;

    @Column(name = "ultima_sincronizacion_en")
    private LocalDateTime ultimaSincronizacionEn;

    @Column(name = "ultimo_commit_hash", length = 100)
    private String ultimoCommitHash;

    @Column(name = "estado_sincronizacion", nullable = false, length = 20)
    private String estadoSincronizacion;

    @Column(name = "clave_cache_local")
    private String claveCacheLocal;

    @Column(name = "version_catalogo_local", length = 50)
    private String versionCatalogoLocal;

    @Column(name = "mensaje_error", columnDefinition = "text")
    private String mensajeError;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public EstadoCatalogoUsuario() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public SistemaJuego getSistemaJuego() { return sistemaJuego; }
    public void setSistemaJuego(SistemaJuego sistemaJuego) { this.sistemaJuego = sistemaJuego; }
    public FuenteCatalogo getFuenteCatalogo() { return fuenteCatalogo; }
    public void setFuenteCatalogo(FuenteCatalogo fuenteCatalogo) { this.fuenteCatalogo = fuenteCatalogo; }
    public LocalDateTime getUltimaComprobacionEn() { return ultimaComprobacionEn; }
    public void setUltimaComprobacionEn(LocalDateTime ultimaComprobacionEn) { this.ultimaComprobacionEn = ultimaComprobacionEn; }
    public LocalDateTime getUltimaSincronizacionEn() { return ultimaSincronizacionEn; }
    public void setUltimaSincronizacionEn(LocalDateTime ultimaSincronizacionEn) { this.ultimaSincronizacionEn = ultimaSincronizacionEn; }
    public String getUltimoCommitHash() { return ultimoCommitHash; }
    public void setUltimoCommitHash(String ultimoCommitHash) { this.ultimoCommitHash = ultimoCommitHash; }
    public String getEstadoSincronizacion() { return estadoSincronizacion; }
    public void setEstadoSincronizacion(String estadoSincronizacion) { this.estadoSincronizacion = estadoSincronizacion; }
    public String getClaveCacheLocal() { return claveCacheLocal; }
    public void setClaveCacheLocal(String claveCacheLocal) { this.claveCacheLocal = claveCacheLocal; }
    public String getVersionCatalogoLocal() { return versionCatalogoLocal; }
    public void setVersionCatalogoLocal(String versionCatalogoLocal) { this.versionCatalogoLocal = versionCatalogoLocal; }
    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
