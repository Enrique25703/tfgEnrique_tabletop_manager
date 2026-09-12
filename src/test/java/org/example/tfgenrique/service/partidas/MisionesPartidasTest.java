package org.example.tfgenrique.service.partidas;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.*;

class MisionesPartidasTest {
    @Test
    void cargaCatalogosSeparadosYLosCincoDesplieguesAos() throws Exception {
        var misiones = new Misiones40kService();
        var aos = misiones.obtenerMisionesSecundarias("AOS_4");
        var cuarenta = misiones.obtenerMisionesSecundarias("WH40K_11");
        assertThat(aos).hasSize(8);
        assertThat(cuarenta).isNotEmpty();
        assertThat(aos).extracting(Misiones40kService.MisionView::id)
                .doesNotContainAnyElementsOf(cuarenta.stream().map(Misiones40kService.MisionView::id).toList());
        assertThat(aos).allSatisfy(m -> {
            assertThat(m.titulo()).isNotBlank();
            assertThat(m.descripcion()).isNotBlank();
            assertThat(m.puntuacion()).isNotBlank();
            assertThat(m.condiciones()).isNotEmpty();
        });
        assertThat(misiones.obtenerPrimeraMisionPrincipal("AOS_4").id()).isNotEqualTo("sin-mision");
        var despliegues = new DeploymentAosService().obtenerDespliegues();
        assertThat(despliegues).extracting(Deployment40kService.OpcionVisualView::codigo)
                .containsExactly("Despliegue A.png", "Despliegue B.png", "Despliegue C.png", "Despliegue D.png", "Despliegue E.png");
        for (var despliegue : despliegues) {
            assertThat(despliegue.imagenUrl()).contains("/deploymentsAoS/Despliegue%20");
            try (var imagen = new ClassPathResource("deploymentsAoS/" + despliegue.codigo()).getInputStream()) {
                assertThat(imagen.readNBytes(4)).containsExactly((byte) 137, (byte) 80, (byte) 78, (byte) 71);
            }
        }
    }
}
