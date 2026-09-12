package org.example.tfgenrique.service.partidas;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.example.tfgenrique.controller.PartidaController;
import org.example.tfgenrique.dao.*;
import org.example.tfgenrique.entity.Usuario;
import org.example.tfgenrique.service.CreacionListasService;
import org.example.tfgenrique.service.catalogo40k.Catalogo40kService;
import org.example.tfgenrique.service.catalogoAos.CatalogoAosService;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PartidaAperturaTest {
    // No hay OpenEntityManagerInView ni transacción alrededor del test: cada HTTP
    // tiene que abrir y cerrar su propia unidad de trabajo, igual que la aplicación.
    @Test
    void abreElFormularioYCreaUnaPartidaConSuPantallaDeJugadores() throws Exception {
        try (var contexto = new AnnotationConfigApplicationContext(Config.class)) {
            Usuario usuario = crearUsuario(contexto.getBean(UsuarioRepository.class), "jugador");
            var mvc = MockMvcBuilders.standaloneSetup(
                    new PartidaController(contexto.getBean(PartidaService.class))).build();
            var sesion = new MockHttpSession();
            sesion.setAttribute("usuarioId", usuario.getId());

            mvc.perform(get("/partidas/nueva").session(sesion))
                    .andExpect(status().isOk()).andExpect(view().name("partidas/partidaNueva"));
            var creado = mvc.perform(post("/partidas/crear").session(sesion)
                            .param("sistemaJuego", "WH40K_11").param("estiloJuego", "EQUILIBRADO"))
                    .andExpect(status().is3xxRedirection()).andReturn();
            String destino = creado.getResponse().getRedirectedUrl();
            assertNotNull(destino);
            assertTrue(destino.matches("/partidas/\\d+/jugadores"));

            var jugadores = mvc.perform(get(destino).session(sesion))
                    .andExpect(status().isOk()).andExpect(view().name("partidas/partidaJugadores"))
                    .andReturn();
            var vista = (PartidaService.JugadoresView) jugadores.getModelAndView().getModel().get("jugadores");
            assertEquals("jugador", vista.partida().getJugador1NombreSnapshot());
            assertEquals("CREADA", vista.partida().getEstado());
            assertEquals("Imperium", vista.facciones().get(0).nombre());
            verify(contexto.getBean(CreacionListasService.class)).obtenerListasGuardadas("jugador");

            String base = destino.substring(0, destino.lastIndexOf('/'));
            mvc.perform(post(destino).session(sesion)
                            .param("jugador1Nombre", "jugador").param("jugador1Faccion", "Imperium")
                            .param("jugador1Ejercito", "Prueba").param("jugador1ListaTipo", "NONE")
                            .param("jugador2Nombre", "Rival").param("jugador2Faccion", "Imperium")
                            .param("jugador2Ejercito", "Prueba"))
                    .andExpect(redirectedUrl(base + "/configuracion"));
            mvc.perform(get(base + "/configuracion").session(sesion))
                    .andExpect(status().isOk()).andExpect(view().name("partidas/partidaConfiguracion"));
            mvc.perform(get("/partidas/nueva"))
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    void mantieneLaProteccionDeLasPartidasDeOtrosUsuarios() {
        try (var contexto = new AnnotationConfigApplicationContext(Config.class)) {
            var repositorio = contexto.getBean(UsuarioRepository.class);
            Usuario propietario = crearUsuario(repositorio, "propietario");
            Usuario otro = crearUsuario(repositorio, "otro");
            var servicio = contexto.getBean(PartidaService.class);
            Long partidaId = servicio.crearPartida(propietario.getId(), "WH40K_11", "ASIMETRICO");
            var error = assertThrows(IllegalArgumentException.class,
                    () -> servicio.prepararJugadores(otro.getId(), partidaId));
            assertEquals("No puedes editar esta partida.", error.getMessage());
        }
    }

    private Usuario crearUsuario(UsuarioRepository repositorio, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setEmail(nombre + "@example.test");
        usuario.setContrasenaHash("hash-solo-para-test");
        usuario.setRol("USUARIO");
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now());
        usuario.setActualizadoEn(LocalDateTime.now());
        return repositorio.saveAndFlush(usuario);
    }

    @Test
    void completaPartidaAosConCincoDesplieguesMisionesPropiasYResultado() throws Exception {
        try (var contexto = new AnnotationConfigApplicationContext(Config.class)) {
            Usuario usuario = crearUsuario(contexto.getBean(UsuarioRepository.class), "jugadorAos");
            var servicio = contexto.getBean(PartidaService.class);
            var mvc = MockMvcBuilders.standaloneSetup(new PartidaController(servicio)).build();
            var sesion = new MockHttpSession();
            sesion.setAttribute("usuarioId", usuario.getId());
            var creado = mvc.perform(post("/partidas/crear").session(sesion)
                            .param("sistemaJuego", "AOS_4").param("estiloJuego", "ASIMETRICO"))
                    .andExpect(status().is3xxRedirection()).andReturn();
            String destino = creado.getResponse().getRedirectedUrl();
            Long id = Long.valueOf(destino.split("/")[2]);
            var jugadores = servicio.prepararJugadores(usuario.getId(), id);
            assertEquals("AOS_4", jugadores.partida().getSistemaJuego().getCodigo());
            assertEquals("Age of Sigmar", jugadores.partida().getSistemaJuego().getNombre());
            assertEquals("EQUILIBRADO", jugadores.partida().getEstiloJuego());
            assertEquals("Order", jugadores.facciones().get(0).nombre());
            verifyNoInteractions(contexto.getBean(Catalogo40kService.class));

            mvc.perform(post(destino).session(sesion)
                            .param("jugador1Nombre", "jugadorAos").param("jugador1Faccion", "Order")
                            .param("jugador1Ejercito", "Stormcast Eternals").param("jugador1ListaTipo", "NONE")
                            .param("jugador2Nombre", "Rival AoS").param("jugador2Faccion", "Order")
                            .param("jugador2Ejercito", "Stormcast Eternals"))
                    .andExpect(redirectedUrl("/partidas/" + id + "/configuracion"));
            var configuracion = servicio.prepararConfiguracion(usuario.getId(), id);
            assertEquals(5, configuracion.desplieguesSimetricos().size());
            assertTrue(configuracion.layouts().isEmpty());
            assertTrue(configuracion.desplieguesMixtos().isEmpty());
            assertTrue(configuracion.opcionesMision().isEmpty());
            for (var despliegue : configuracion.desplieguesSimetricos()) {
                mvc.perform(post("/partidas/{id}/configuracion", id).session(sesion)
                                .param("despliegue", despliegue.codigo())
                                .param("jugadorDefensor", "USUARIO").param("jugadorPrimero", "RIVAL")
                                .param("layout", "layouts/CA_TerrainLayout1.png")
                                .param("tipoMision", "MISION A").param("usarCartasGiro", "on"))
                        .andExpect(redirectedUrl("/partidas/" + id + "/ronda/1"));
            }
            var ronda = servicio.prepararRonda(usuario.getId(), id, 1);
            assertEquals("Despliegue E", ronda.resumenConfiguracion().despliegue());
            assertNull(ronda.partida().getLayoutMision());
            assertFalse(ronda.partida().getUsarCartasGiro());
            assertEquals("jugador2", ronda.izquierda().prefijo());
            assertEquals("secundaria-destruir-unidad", ronda.misionesSecundarias().get(0).id());
            assertFalse(ronda.misionesSecundarias().get(0).condiciones().isEmpty());
            assertFalse(ronda.misionPrincipal().descripcion().isBlank());
            for (int numero = 1; numero <= 5; numero++) {
                servicio.guardarRonda(usuario.getId(), id, numero, Map.of(
                        "jugador1Primaria", "10", "jugador1Secundaria", "5", "jugador2Primaria", "5",
                        "jugador1Detalle", "[{\"id\":\"secundaria-destruir-unidad\",\"puntos\":5,\"cumplida\":true}]"));
            }
            servicio.finalizarPartida(usuario.getId(), id);
            var resultado = servicio.prepararFinal(usuario.getId(), id);
            assertEquals("FINALIZADA", resultado.partida().getEstado());
            assertEquals(75, resultado.partida().getJugador1PuntuacionTotal());
            assertEquals(25, resultado.partida().getJugador2PuntuacionTotal());
            assertEquals(5, resultado.rondas().size());
            assertTrue(servicio.prepararRonda(usuario.getId(), id, 3).derecha().detalle().contains("secundaria-destruir-unidad"));

            for (String despliegue : List.of("", "Despliegue F.png", "despliegues_simetricos/CA6_SF_DawnOfWar.png")) {
                mvc.perform(post("/partidas/{id}/configuracion", id).session(sesion)
                                .param("despliegue", despliegue).param("jugadorDefensor", "USUARIO")
                                .param("jugadorPrimero", "USUARIO"))
                        .andExpect(redirectedUrl("/partidas/" + id + "/configuracion"))
                        .andExpect(flash().attributeExists("mensajeError"));
            }
        }
    }

    @Test
    void separaListasPorSistemaYRechazaListasDelOtroJuego() {
        try (var contexto = new AnnotationConfigApplicationContext(Config.class)) {
            var usuario = crearUsuario(contexto.getBean(UsuarioRepository.class), "listas");
            var servicio = contexto.getBean(PartidaService.class);
            var listas = contexto.getBean(CreacionListasService.class);
            var lista40k = new CreacionListasService.ListaGuardadaView(1L, "Lista 40k", "WH40K_11", "Imperium", "Prueba", 1000, 2000, 1, List.of());
            var listaAos = new CreacionListasService.ListaGuardadaView(2L, "Lista AoS", "AOS_4", "Order", "Stormcast Eternals", 1000, 2000, 1, List.of());
            when(listas.obtenerListasGuardadas("listas")).thenReturn(List.of(lista40k, listaAos));
            when(listas.obtenerListaGuardadaPorId("listas", 1L)).thenReturn(lista40k);
            when(listas.obtenerListaGuardadaPorId("listas", 2L)).thenReturn(listaAos);
            for (String formato : List.of("WH40K_11", "AOS_4")) {
                Long id = servicio.crearPartida(usuario.getId(), formato, "EQUILIBRADO");
                boolean aos = formato.equals("AOS_4");
                var disponibles = servicio.prepararJugadores(usuario.getId(), id).listasUsuario();
                assertEquals(1, disponibles.size());
                assertEquals(aos ? 2L : 1L, disponibles.get(0).listaId());
                assertThrows(IllegalArgumentException.class, () -> servicio.guardarJugadores(usuario.getId(), id,
                        new PartidaService.JugadoresRequest("Yo", "Order", "Stormcast Eternals", aos ? "1" : "2", "",
                                "Rival", "Order", "Stormcast Eternals", "")));
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = UsuarioRepository.class)
    static class Config {
        @Bean
        DataSource dataSource() {
            var fuente = new JdbcDataSource();
            fuente.setURL("jdbc:h2:mem:partidas-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return fuente;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var fabrica = new LocalContainerEntityManagerFactoryBean();
            fabrica.setDataSource(dataSource);
            fabrica.setPackagesToScan("org.example.tfgenrique.entity");
            fabrica.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            fabrica.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return fabrica;
        }

        @Bean
        JpaTransactionManager transactionManager(EntityManagerFactory fabrica) {
            return new JpaTransactionManager(fabrica);
        }

        @Bean
        Catalogo40kService catalogo() {
            var catalogo = mock(Catalogo40kService.class);
            when(catalogo.getData()).thenReturn(new Catalogo40kService.Catalogo40kData(
                    Map.of("Imperium", Map.of("Prueba", new Catalogo40kService.Ejercito40k(
                            "Imperium", "Prueba", List.of()))), LocalDateTime.now()));
            return catalogo;
        }

        @Bean
        CreacionListasService listas() {
            return mock(CreacionListasService.class);
        }

        @Bean
        CatalogoAosService catalogoAos() {
            var catalogo = mock(CatalogoAosService.class);
            when(catalogo.getData()).thenReturn(new CatalogoAosService.Catalogo40kData(
                    Map.of("Order", Map.of("Stormcast Eternals", new CatalogoAosService.Ejercito40k(
                            "Order", "Stormcast Eternals", List.of()))), LocalDateTime.now()));
            return catalogo;
        }

        @Bean
        PartidaService partidas(UsuarioRepository usuarios, SistemaJuegoRepository sistemas,
                PartidaRepository partidas, RondaPartidaRepository rondas,
                Catalogo40kService catalogo, CreacionListasService listas, CatalogoAosService catalogoAos) {
            return new PartidaService(usuarios, sistemas, partidas, rondas,
                    new Deployment40kService(), new Misiones40kService(), catalogo, listas,
                    new DeploymentAosService(), catalogoAos);
        }
    }
}
