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
@Table(name = "fuentes_catalogo")
public class FuenteCatalogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sistema_juego_id", nullable = false)
    private SistemaJuego sistemaJuego;

    @Column(name = "nombre_fuente", nullable = false, length = 100)
    private String nombreFuente;

    @Column(name = "tipo_fuente", nullable = false, length = 30)
    private String tipoFuente;

    @Column(name = "url_repositorio", nullable = false)
    private String urlRepositorio;

    @Column(name = "rama_por_defecto", nullable = false, length = 100)
    private String ramaPorDefecto;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public FuenteCatalogo() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SistemaJuego getSistemaJuego() { return sistemaJuego; }
    public void setSistemaJuego(SistemaJuego sistemaJuego) { this.sistemaJuego = sistemaJuego; }
    public String getNombreFuente() { return nombreFuente; }
    public void setNombreFuente(String nombreFuente) { this.nombreFuente = nombreFuente; }
    public String getTipoFuente() { return tipoFuente; }
    public void setTipoFuente(String tipoFuente) { this.tipoFuente = tipoFuente; }
    public String getUrlRepositorio() { return urlRepositorio; }
    public void setUrlRepositorio(String urlRepositorio) { this.urlRepositorio = urlRepositorio; }
    public String getRamaPorDefecto() { return ramaPorDefecto; }
    public void setRamaPorDefecto(String ramaPorDefecto) { this.ramaPorDefecto = ramaPorDefecto; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
