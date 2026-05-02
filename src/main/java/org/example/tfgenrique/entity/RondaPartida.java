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
@Table(name = "rondas_partida")
public class RondaPartida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @Column(name = "numero_ronda", nullable = false)
    private Integer numeroRonda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_con_prioridad_id")
    private Usuario jugadorConPrioridad;

    @Column(name = "cp_jugador1_inicio", nullable = false)
    private Integer cpJugador1Inicio;

    @Column(name = "cp_jugador1_fin", nullable = false)
    private Integer cpJugador1Fin;

    @Column(name = "cp_jugador2_inicio", nullable = false)
    private Integer cpJugador2Inicio;

    @Column(name = "cp_jugador2_fin", nullable = false)
    private Integer cpJugador2Fin;

    @Column(name = "primaria_jugador1", nullable = false)
    private Integer primariaJugador1;

    @Column(name = "primaria_jugador2", nullable = false)
    private Integer primariaJugador2;

    @Column(name = "secundaria_jugador1", nullable = false)
    private Integer secundariaJugador1;

    @Column(name = "secundaria_jugador2", nullable = false)
    private Integer secundariaJugador2;

    @Column(name = "bonus_jugador1", nullable = false)
    private Integer bonusJugador1;

    @Column(name = "bonus_jugador2", nullable = false)
    private Integer bonusJugador2;

    @Column(name = "total_acumulado_jugador1", nullable = false)
    private Integer totalAcumuladoJugador1;

    @Column(name = "total_acumulado_jugador2", nullable = false)
    private Integer totalAcumuladoJugador2;

    @Column(name = "detalle_jugador1", columnDefinition = "json")
    private String detalleJugador1;

    @Column(name = "detalle_jugador2", columnDefinition = "json")
    private String detalleJugador2;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(columnDefinition = "text")
    private String notas;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public RondaPartida() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }
    public Integer getNumeroRonda() { return numeroRonda; }
    public void setNumeroRonda(Integer numeroRonda) { this.numeroRonda = numeroRonda; }
    public Usuario getJugadorConPrioridad() { return jugadorConPrioridad; }
    public void setJugadorConPrioridad(Usuario jugadorConPrioridad) { this.jugadorConPrioridad = jugadorConPrioridad; }
    public Integer getCpJugador1Inicio() { return cpJugador1Inicio; }
    public void setCpJugador1Inicio(Integer cpJugador1Inicio) { this.cpJugador1Inicio = cpJugador1Inicio; }
    public Integer getCpJugador1Fin() { return cpJugador1Fin; }
    public void setCpJugador1Fin(Integer cpJugador1Fin) { this.cpJugador1Fin = cpJugador1Fin; }
    public Integer getCpJugador2Inicio() { return cpJugador2Inicio; }
    public void setCpJugador2Inicio(Integer cpJugador2Inicio) { this.cpJugador2Inicio = cpJugador2Inicio; }
    public Integer getCpJugador2Fin() { return cpJugador2Fin; }
    public void setCpJugador2Fin(Integer cpJugador2Fin) { this.cpJugador2Fin = cpJugador2Fin; }
    public Integer getPrimariaJugador1() { return primariaJugador1; }
    public void setPrimariaJugador1(Integer primariaJugador1) { this.primariaJugador1 = primariaJugador1; }
    public Integer getPrimariaJugador2() { return primariaJugador2; }
    public void setPrimariaJugador2(Integer primariaJugador2) { this.primariaJugador2 = primariaJugador2; }
    public Integer getSecundariaJugador1() { return secundariaJugador1; }
    public void setSecundariaJugador1(Integer secundariaJugador1) { this.secundariaJugador1 = secundariaJugador1; }
    public Integer getSecundariaJugador2() { return secundariaJugador2; }
    public void setSecundariaJugador2(Integer secundariaJugador2) { this.secundariaJugador2 = secundariaJugador2; }
    public Integer getBonusJugador1() { return bonusJugador1; }
    public void setBonusJugador1(Integer bonusJugador1) { this.bonusJugador1 = bonusJugador1; }
    public Integer getBonusJugador2() { return bonusJugador2; }
    public void setBonusJugador2(Integer bonusJugador2) { this.bonusJugador2 = bonusJugador2; }
    public Integer getTotalAcumuladoJugador1() { return totalAcumuladoJugador1; }
    public void setTotalAcumuladoJugador1(Integer totalAcumuladoJugador1) { this.totalAcumuladoJugador1 = totalAcumuladoJugador1; }
    public Integer getTotalAcumuladoJugador2() { return totalAcumuladoJugador2; }
    public void setTotalAcumuladoJugador2(Integer totalAcumuladoJugador2) { this.totalAcumuladoJugador2 = totalAcumuladoJugador2; }
    public String getDetalleJugador1() { return detalleJugador1; }
    public void setDetalleJugador1(String detalleJugador1) { this.detalleJugador1 = detalleJugador1; }
    public String getDetalleJugador2() { return detalleJugador2; }
    public void setDetalleJugador2(String detalleJugador2) { this.detalleJugador2 = detalleJugador2; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
