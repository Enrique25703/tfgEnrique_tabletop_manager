package org.example.tfgenrique.service.comunidadservice;

import jakarta.persistence.EntityManagerFactory;
import org.example.tfgenrique.dao.*;
import org.example.tfgenrique.entity.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;

class EventoConcurrenciaTest {
    private AnnotationConfigApplicationContext contexto;
    private ExecutorService hilos;
    private ComunidadEventoService primeraInstancia;
    private ComunidadEventoService segundaInstancia;
    private EventoRepository eventos;
    private InscripcionEventoRepository inscripciones;
    private Usuario organizador;
    private Comunidad comunidad;
    private SistemaJuego sistema;

    @BeforeEach
    void preparar() {
        contexto = new AnnotationConfigApplicationContext(Config.class);
        hilos = Executors.newFixedThreadPool(12);
        primeraInstancia = contexto.getBean("primera", ComunidadEventoService.class);
        segundaInstancia = contexto.getBean("segunda", ComunidadEventoService.class);
        eventos = contexto.getBean(EventoRepository.class);
        inscripciones = contexto.getBean(InscripcionEventoRepository.class);
        organizador = usuario("organizador");
        comunidad = new Comunidad();
        comunidad.setNombre("Comunidad de prueba");
        comunidad.setPrivacidad("PUBLICA");
        comunidad.setActivo(true);
        comunidad.setCreadoEn(LocalDateTime.now());
        comunidad.setActualizadoEn(LocalDateTime.now());
        comunidad = contexto.getBean(ComunidadRepository.class).saveAndFlush(comunidad);
        afiliar(organizador, "ADMINISTRADOR");
        sistema = new SistemaJuego();
        sistema.setCodigo("WH40K_11");
        sistema.setNombre("40k");
        sistema.setActivo(true);
        sistema.setCreadoEn(LocalDateTime.now());
        sistema = contexto.getBean(SistemaJuegoRepository.class).saveAndFlush(sistema);
    }

    @AfterEach
    void cerrar() throws InterruptedException {
        hilos.shutdownNow();
        assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
        contexto.close();
    }

    @Test
    void doceSolicitudesEnDosInstanciasNuncaSuperanTresPlazas() throws Exception {
        Evento evento = evento(3);
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 0; i < 12; i++) usuarios.add(miembro("jugador" + i));
        long aceptadas = simultaneas(12, i -> () -> inscribir(servicio(i), usuarios.get(i), evento));
        assertEquals(3, aceptadas);
        assertEquals(3, inscripciones.countByEvento(evento));
    }

    @Test
    void solicitudesDuplicadasSoloOcupanUnaPlaza() throws Exception {
        Evento evento = evento(10);
        Usuario usuario = miembro("repetido");
        assertEquals(1, simultaneas(8, i -> () -> inscribir(servicio(i), usuario, evento)));
        assertEquals(1, inscripciones.countByEvento(evento));
    }

    @Test
    void reducirAforoYUnirseRespetanElMismoBloqueo() throws Exception {
        Evento evento = evento(2);
        primeraInstancia.unirseAEvento(miembro("inscrito").getId(), evento.getId());
        Usuario nuevo = miembro("nuevo");
        var request = new ComunidadService.CrearEventoRequest();
        request.setTitulo("Evento editado");
        request.setFecha(LocalDateTime.now().plusDays(2));
        request.setNumeroRondas(3);
        request.setMaxParticipantes(1);
        request.setLugar("Madrid");
        request.setLatitud(40.4);
        request.setLongitud(-3.7);
        request.setFormatoJuego("WH40K_11");
        long aceptadas = simultaneas(2, i -> () -> {
            try {
                if (i == 0) primeraInstancia.guardarEvento(organizador.getId(), comunidad.getId(), evento.getId(), request);
                else segundaInstancia.unirseAEvento(nuevo.getId(), evento.getId());
                return true;
            } catch (IllegalArgumentException ex) {
                assertTrue(ex.getMessage().contains("participantes"), ex.getMessage());
                return false;
            }
        });
        assertEquals(1, aceptadas);
        assertTrue(inscripciones.countByEvento(evento) <= eventos.findById(evento.getId()).orElseThrow().getMaxParticipantes());
    }

    @Test
    void unEventoBloqueadoNoImpideInscribirseEnOtroYRollbackLiberaLaPlaza() throws Exception {
        Evento ocupado = evento(1);
        Evento libre = evento(1);
        Usuario usuario = miembro("espera");
        CountDownLatch bloqueado = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        var titular = hilos.submit(() -> transaccion().execute(status -> {
            primeraInstancia.unirseAEvento(organizador.getId(), ocupado.getId());
            bloqueado.countDown();
            esperar(liberar);
            status.setRollbackOnly();
            return null;
        }));
        try {
            assertTrue(bloqueado.await(5, TimeUnit.SECONDS));
            CountDownLatch intentando = new CountDownLatch(1);
            var esperando = hilos.submit(() -> {
                intentando.countDown();
                return segundaInstancia.unirseAEvento(usuario.getId(), ocupado.getId());
            });
            assertTrue(intentando.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> esperando.get(200, TimeUnit.MILLISECONDS));
            assertEquals(comunidad.getId(), hilos.submit(() -> segundaInstancia.unirseAEvento(
                    usuario.getId(), libre.getId())).get(5, TimeUnit.SECONDS));
            liberar.countDown();
            titular.get(5, TimeUnit.SECONDS);
            assertEquals(comunidad.getId(), esperando.get(5, TimeUnit.SECONDS));
            assertEquals(1, inscripciones.countByEvento(ocupado));
        } finally {
            liberar.countDown();
        }
    }

    @Test
    void refrescaElAforoAunqueLaTransaccionExteriorYaHayaCargadoElEvento() throws Exception {
        Evento evento = evento(2);
        Usuario usuario = miembro("notificado");
        CountDownLatch leido = new CountDownLatch(1);
        CountDownLatch cambiado = new CountDownLatch(1);
        var aceptacion = hilos.submit(() -> transaccion().execute(status -> {
            assertEquals(2, eventos.findById(evento.getId()).orElseThrow().getMaxParticipantes());
            leido.countDown();
            esperar(cambiado);
            boolean aceptada = inscribir(primeraInstancia, usuario, evento);
            if (!aceptada) status.setRollbackOnly();
            return aceptada;
        }));
        try {
            assertTrue(leido.await(5, TimeUnit.SECONDS));
            transaccion().executeWithoutResult(status -> {
                eventos.findByIdForUpdate(evento.getId()).orElseThrow().setMaxParticipantes(1);
                segundaInstancia.unirseAEvento(organizador.getId(), evento.getId());
            });
            cambiado.countDown();
            assertFalse(aceptacion.get(5, TimeUnit.SECONDS));
            assertEquals(1, inscripciones.countByEvento(evento));
        } finally {
            cambiado.countDown();
        }
    }

    @Test
    void sinAforoAdmiteTodosYUnaBajaLiberaUnaPlaza() throws Exception {
        Evento ilimitado = evento(null);
        List<Usuario> usuarios = List.of(miembro("a"), miembro("b"), miembro("c"));
        assertEquals(3, simultaneas(3, i -> () -> inscribir(servicio(i), usuarios.get(i), ilimitado)));
        Evento limitado = evento(1);
        primeraInstancia.unirseAEvento(usuarios.get(0).getId(), limitado.getId());
        segundaInstancia.desinscribirseDeEvento(usuarios.get(0).getId(), limitado.getId());
        primeraInstancia.unirseAEvento(usuarios.get(1).getId(), limitado.getId());
        assertEquals(1, inscripciones.countByEvento(limitado));
    }

    private boolean inscribir(ComunidadEventoService servicio, Usuario usuario, Evento evento) {
        try {
            servicio.unirseAEvento(usuario.getId(), evento.getId());
            return true;
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("participantes") || ex.getMessage().contains("Ya estas inscrito"), ex.getMessage());
            return false;
        }
    }

    private long simultaneas(int numero, IntFunction<Callable<Boolean>> tareas) throws Exception {
        CountDownLatch listos = new CountDownLatch(numero);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        try {
            for (int i = 0; i < numero; i++) {
                Callable<Boolean> tarea = tareas.apply(i);
                resultados.add(hilos.submit(() -> { listos.countDown(); esperar(salida); return tarea.call(); }));
            }
            assertTrue(listos.await(5, TimeUnit.SECONDS));
            salida.countDown();
            long aceptadas = 0;
            for (var resultado : resultados) if (resultado.get(10, TimeUnit.SECONDS)) aceptadas++;
            return aceptadas;
        } finally {
            salida.countDown();
        }
    }

    private void esperar(CountDownLatch latch) {
        try { assertTrue(latch.await(10, TimeUnit.SECONDS)); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
    }

    private TransactionTemplate transaccion() { return new TransactionTemplate(contexto.getBean(JpaTransactionManager.class)); }
    private ComunidadEventoService servicio(int i) { return i % 2 == 0 ? primeraInstancia : segundaInstancia; }
    private Usuario miembro(String nombre) { Usuario usuario = usuario(nombre); afiliar(usuario, "USUARIO"); return usuario; }

    private Usuario usuario(String nombre) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre); usuario.setEmail(nombre + "@example.test");
        usuario.setContrasenaHash("test"); usuario.setRol("USUARIO"); usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now()); usuario.setActualizadoEn(LocalDateTime.now());
        return contexto.getBean(UsuarioRepository.class).saveAndFlush(usuario);
    }

    private void afiliar(Usuario usuario, String rol) {
        AfiliacionComunidad afiliacion = new AfiliacionComunidad();
        afiliacion.setUsuario(usuario); afiliacion.setComunidad(comunidad); afiliacion.setRolComunidad(rol);
        afiliacion.setEstadoAfiliacion("ACTIVA"); afiliacion.setUnidoEn(LocalDateTime.now());
        contexto.getBean(AfiliacionComunidadRepository.class).saveAndFlush(afiliacion);
    }

    private Evento evento(Integer maximo) {
        Evento evento = new Evento();
        evento.setComunidad(comunidad); evento.setOrganizadorUsuario(organizador); evento.setSistemaJuego(sistema);
        evento.setTitulo("Evento"); evento.setTipoEvento("COMUNIDAD"); evento.setEstado("ABIERTO");
        evento.setInicioEn(LocalDateTime.now().plusDays(2)); evento.setMaxParticipantes(maximo);
        evento.setCreadoEn(LocalDateTime.now()); evento.setActualizadoEn(LocalDateTime.now());
        return eventos.saveAndFlush(evento);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = EventoRepository.class)
    static class Config {
        @Bean DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:eventos-" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
            return ds;
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var fabrica = new LocalContainerEntityManagerFactoryBean();
            fabrica.setDataSource(ds); fabrica.setPackagesToScan("org.example.tfgenrique.entity");
            fabrica.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            fabrica.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return fabrica;
        }
        @Bean JpaTransactionManager transactionManager(EntityManagerFactory fabrica) { return new JpaTransactionManager(fabrica); }
        @Bean ComunidadMiembroService miembros(UsuarioRepository u, ComunidadRepository c, AfiliacionComunidadRepository a) {
            return new ComunidadMiembroService(u, c, a);
        }
        @Bean ComunidadEventoService primera(EventoRepository e, InscripcionEventoRepository i, SistemaJuegoRepository s, ComunidadMiembroService m) {
            return new ComunidadEventoService(e, i, s, m);
        }
        @Bean ComunidadEventoService segunda(EventoRepository e, InscripcionEventoRepository i, SistemaJuegoRepository s, ComunidadMiembroService m) {
            return new ComunidadEventoService(e, i, s, m);
        }
    }
}
