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
@Table(name = "versiones_lista_ejercito")
public class VersionListaEjercito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_ejercito_id", nullable = false)
    private ListaEjercito listaEjercito;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fuente_catalogo_id", nullable = false)
    private FuenteCatalogo fuenteCatalogo;

    @Column(name = "commit_catalogo_hash", length = 100)
    private String commitCatalogoHash;

    @Column(name = "revision_catalogo", length = 50)
    private String revisionCatalogo;

    @Column(name = "version_esquema_json", length = 30)
    private String versionEsquemaJson;

    @Column(name = "checksum_datos", length = 128)
    private String checksumDatos;

    @Column(name = "puntos_totales", nullable = false)
    private Integer puntosTotales;

    @Column(name = "datos_lista", nullable = false, columnDefinition = "json")
    private String datosLista;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public VersionListaEjercito() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ListaEjercito getListaEjercito() { return listaEjercito; }
    public void setListaEjercito(ListaEjercito listaEjercito) { this.listaEjercito = listaEjercito; }
    public Integer getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(Integer numeroVersion) { this.numeroVersion = numeroVersion; }
    public FuenteCatalogo getFuenteCatalogo() { return fuenteCatalogo; }
    public void setFuenteCatalogo(FuenteCatalogo fuenteCatalogo) { this.fuenteCatalogo = fuenteCatalogo; }
    public String getCommitCatalogoHash() { return commitCatalogoHash; }
    public void setCommitCatalogoHash(String commitCatalogoHash) { this.commitCatalogoHash = commitCatalogoHash; }
    public String getRevisionCatalogo() { return revisionCatalogo; }
    public void setRevisionCatalogo(String revisionCatalogo) { this.revisionCatalogo = revisionCatalogo; }
    public String getVersionEsquemaJson() { return versionEsquemaJson; }
    public void setVersionEsquemaJson(String versionEsquemaJson) { this.versionEsquemaJson = versionEsquemaJson; }
    public String getChecksumDatos() { return checksumDatos; }
    public void setChecksumDatos(String checksumDatos) { this.checksumDatos = checksumDatos; }
    public Integer getPuntosTotales() { return puntosTotales; }
    public void setPuntosTotales(Integer puntosTotales) { this.puntosTotales = puntosTotales; }
    public String getDatosLista() { return datosLista; }
    public void setDatosLista(String datosLista) { this.datosLista = datosLista; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
