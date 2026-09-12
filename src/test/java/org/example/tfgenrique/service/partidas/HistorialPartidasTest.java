package org.example.tfgenrique.service.partidas;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.tfgenrique.controller.HistorialPartidasController;
import org.example.tfgenrique.dao.PartidaRepository;
import org.example.tfgenrique.dao.SistemaJuegoRepository;
import org.example.tfgenrique.dao.UsuarioRepository;
import org.example.tfgenrique.entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistorialPartidasTest {
    private AnnotationConfigApplicationContext contexto;
    private HistorialPartidasService historial;
    private PartidaService partidas;
    private PartidaRepository repositorio;
    private Usuario propietario;
    private Usuario rival;

    @BeforeEach
    void preparar() {
        contexto = new AnnotationConfigApplicationContext(Config.class);
        historial = contexto.getBean(HistorialPartidasService.class);
        partidas = contexto.getBean(PartidaService.class);
        repositorio = contexto.getBean(PartidaRepository.class);
        propietario = usuario("propietario");
        rival = usuario("rival");
    }

    @AfterEach
    void cerrar() { contexto.close(); }

    @Test
    void filtraFinalizadasPorUsuarioFormatoFechasInclusivasYResultado() {
        var victoria = partida(propietario, 80, 30, "2026-09-10T23:59:59");
        var derrota = partida(propietario, 20, 70, "2026-09-11T00:00:00");
        var empate = partida(propietario, 40, 40, "2026-09-12T12:00:00");
        var comoSegundo = partida(rival, 10, 90, "2026-09-13T12:00:00");
        comoSegundo.setJugador2Usuario(propietario);
        repositorio.saveAndFlush(comoSegundo);
        partida(rival, 90, 0, "2026-09-14T12:00:00");
        partidas.crearPartida(propietario.getId(), "WH40K_11", "EQUILIBRADO");

        var aos = new SistemaJuego();
        aos.setCodigo("AOS_4"); aos.setNombre("Age of Sigmar"); aos.setActivo(true);
        aos.setCreadoEn(LocalDateTime.now());
        contexto.getBean(SistemaJuegoRepository.class).saveAndFlush(aos);
        empate.setSistemaJuego(aos);
        repositorio.saveAndFlush(empate);

        assertThat(historial.obtenerHistorial(propietario.getId(), "", "", "", "").partidas())
                .extracting(HistorialPartidasService.PartidaView::id)
                .containsExactly(comoSegundo.getId(), empate.getId(), derrota.getId(), victoria.getId());
        var filtradas = historial.obtenerHistorial(propietario.getId(), "WH40K_11", "2026-09-10", "2026-09-11", "DERROTA");
        assertThat(filtradas.partidas()).singleElement().satisfies(p -> {
            assertThat(p.id()).isEqualTo(derrota.getId());
            assertThat(p.jugador2()).isEqualTo("Rival invitado");
            assertThat(p.puntos1()).isEqualTo(20);
            assertThat(p.puntos2()).isEqualTo(70);
        });
        assertThat(historial.obtenerHistorial(propietario.getId(), "", "2026-09-10", "2026-09-10", "VICTORIA").partidas())
                .extracting(HistorialPartidasService.PartidaView::id).containsExactly(victoria.getId());
        assertThat(historial.obtenerHistorial(propietario.getId(), "AOS_4", "", "", "EMPATE").partidas())
                .extracting(HistorialPartidasService.PartidaView::id).containsExactly(empate.getId());
        assertThat(historial.obtenerHistorial(propietario.getId(), "", "", "", "VICTORIA").partidas())
                .extracting(HistorialPartidasService.PartidaView::id).containsExactly(comoSegundo.getId(), victoria.getId());
        assertThat(historial.obtenerHistorial(propietario.getId(), "", "", "", "").partidas().get(0).puedeEliminar()).isFalse();
        assertThatThrownBy(() -> historial.obtenerHistorial(propietario.getId(), "", "2026-09-12", "2026-09-10", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> historial.obtenerHistorial(propietario.getId(), "", "2026-02-30", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eliminaDependenciasSinAfectarOtrasPartidasYCompruebaPermisos() {
        Long id = partidas.crearPartida(propietario.getId(), "WH40K_11", "EQUILIBRADO");
        partidas.guardarRonda(propietario.getId(), id, 1, java.util.Map.of());
        partidas.finalizarPartida(propietario.getId(), id);
        var otra = partida(propietario, 60, 20, "2026-09-10T12:00:00");
        var em = SharedEntityManagerCreator.createSharedEntityManager(contexto.getBean(EntityManagerFactory.class));
        var tx = new TransactionTemplate(contexto.getBean(PlatformTransactionManager.class));
        tx.executeWithoutResult(status -> {
            var configuracion = new ConfiguracionMisionPartida();
            configuracion.setPartida(em.getReference(Partida.class, id));
            configuracion.setCreadoEn(LocalDateTime.now());
            em.persist(configuracion);
            var notificacion = new NotificacionUsuario();
            notificacion.setPartida(em.getReference(Partida.class, id));
            notificacion.setReceptorUsuario(em.getReference(Usuario.class, propietario.getId()));
            notificacion.setTipo("PARTIDA"); notificacion.setEstado("ACEPTADA");
            notificacion.setCreadoEn(LocalDateTime.now());
            em.persist(notificacion);
        });

        assertThatThrownBy(() -> historial.eliminar(rival.getId(), id)).isInstanceOf(IllegalArgumentException.class);
        assertThat(repositorio.existsById(id)).isTrue();
        historial.eliminar(propietario.getId(), id);
        assertThat(repositorio.existsById(id)).isFalse();
        assertThat(repositorio.existsById(otra.getId())).isTrue();
        for (String entidad : java.util.List.of("RondaPartida", "ConfiguracionMisionPartida", "NotificacionUsuario")) {
            assertThat(em.createQuery("select count(e) from " + entidad + " e where e.partida.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult()).isZero();
        }
        Long enCurso = partidas.crearPartida(propietario.getId(), "WH40K_11", "EQUILIBRADO");
        assertThatThrownBy(() -> historial.eliminar(propietario.getId(), enCurso)).isInstanceOf(IllegalArgumentException.class);
        assertThat(repositorio.existsById(enCurso)).isTrue();
    }

    @Test
    void rutasProtegidasValidacionDeFiltrosYBorradoPorPost() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new HistorialPartidasController(historial)).build();
        var sesion = new MockHttpSession();
        sesion.setAttribute("usuarioId", propietario.getId());
        var partida = partida(propietario, 10, 20, "2026-09-10T12:00:00");
        mvc.perform(get("/partidas")).andExpect(redirectedUrl("/"));
        mvc.perform(post("/partidas/{id}/eliminar", partida.getId())).andExpect(redirectedUrl("/"));
        mvc.perform(get("/partidas").session(sesion)).andExpect(status().isOk())
                .andExpect(view().name("partidas/historialPartidas")).andExpect(model().attributeExists("historial"));
        mvc.perform(get("/partidas").session(sesion).param("desde", "incorrecta"))
                .andExpect(redirectedUrl("/partidas")).andExpect(flash().attributeExists("mensajeError"));
        mvc.perform(get("/partidas").session(sesion).param("resultado", "INVALIDO"))
                .andExpect(redirectedUrl("/partidas")).andExpect(flash().attributeExists("mensajeError"));
        mvc.perform(get("/partidas").session(sesion).param("formatoJuego", "INVALIDO"))
                .andExpect(redirectedUrl("/partidas")).andExpect(flash().attributeExists("mensajeError"));
        mvc.perform(get("/partidas/{id}/eliminar", partida.getId()).session(sesion))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/partidas/{id}/eliminar", partida.getId()).session(sesion))
                .andExpect(redirectedUrl("/partidas")).andExpect(flash().attributeExists("mensajeOk"));
        assertThat(repositorio.existsById(partida.getId())).isFalse();
    }

    private Partida partida(Usuario creador, int puntos1, int puntos2, String fecha) {
        Long id = partidas.crearPartida(creador.getId(), "WH40K_11", "EQUILIBRADO");
        var p = repositorio.findById(id).orElseThrow();
        p.setJugador1PuntuacionTotal(puntos1); p.setJugador2PuntuacionTotal(puntos2);
        p.setJugador2NombreSnapshot("Rival invitado");
        p.setEstado("FINALIZADA"); p.setFinalizadaEn(LocalDateTime.parse(fecha));
        return repositorio.saveAndFlush(p);
    }

    private Usuario usuario(String nombre) {
        var u = new Usuario();
        u.setNombreUsuario(nombre); u.setEmail(nombre + "@example.test"); u.setContrasenaHash("test");
        u.setRol("USUARIO"); u.setActivo(true);
        u.setCreadoEn(LocalDateTime.now()); u.setActualizadoEn(LocalDateTime.now());
        return contexto.getBean(UsuarioRepository.class).saveAndFlush(u);
    }

    @Configuration(proxyBeanMethods = false)
    @Import(PartidaAperturaTest.Config.class)
    static class Config {
        @Bean
        HistorialPartidasService historial(PartidaRepository partidas, SistemaJuegoRepository sistemas,
                                          UsuarioRepository usuarios, EntityManagerFactory fabrica) {
            EntityManager em = SharedEntityManagerCreator.createSharedEntityManager(fabrica);
            return new HistorialPartidasService(partidas, sistemas, usuarios, em);
        }
    }
}
