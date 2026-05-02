package org.example.tfgenrique.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuraciones_mision_partida")
public class ConfiguracionMisionPartida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false, unique = true)
    private Partida partida;

    @Column(name = "pack_mision", length = 150)
    private String packMision;

    @Column(length = 150)
    private String mision;

    @Column(length = 150)
    private String despliegue;

    @Column(name = "regla_mision", length = 150)
    private String reglaMision;

    @Column(name = "seleccion_jugador1", columnDefinition = "json")
    private String seleccionJugador1;

    @Column(name = "seleccion_jugador2", columnDefinition = "json")
    private String seleccionJugador2;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public ConfiguracionMisionPartida() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }
    public String getPackMision() { return packMision; }
    public void setPackMision(String packMision) { this.packMision = packMision; }
    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }
    public String getDespliegue() { return despliegue; }
    public void setDespliegue(String despliegue) { this.despliegue = despliegue; }
    public String getReglaMision() { return reglaMision; }
    public void setReglaMision(String reglaMision) { this.reglaMision = reglaMision; }
    public String getSeleccionJugador1() { return seleccionJugador1; }
    public void setSeleccionJugador1(String seleccionJugador1) { this.seleccionJugador1 = seleccionJugador1; }
    public String getSeleccionJugador2() { return seleccionJugador2; }
    public void setSeleccionJugador2(String seleccionJugador2) { this.seleccionJugador2 = seleccionJugador2; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
