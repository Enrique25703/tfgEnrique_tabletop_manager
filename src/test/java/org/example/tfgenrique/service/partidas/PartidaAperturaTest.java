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

            mvc.perform(get("/partidas").session(sesion))
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
            mvc.perform(get("/partidas"))
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
        PartidaService partidas(UsuarioRepository usuarios, SistemaJuegoRepository sistemas,
                PartidaRepository partidas, RondaPartidaRepository rondas,
                Catalogo40kService catalogo, CreacionListasService listas) {
            return new PartidaService(usuarios, sistemas, partidas, rondas,
                    new Deployment40kService(), new Misiones40kService(), catalogo, listas);
        }
    }
}
