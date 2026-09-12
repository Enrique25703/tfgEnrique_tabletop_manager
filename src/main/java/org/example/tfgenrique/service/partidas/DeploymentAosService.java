package org.example.tfgenrique.service.partidas;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class DeploymentAosService {
    public List<Deployment40kService.OpcionVisualView> obtenerDespliegues() {
        return IntStream.rangeClosed('A', 'E').mapToObj(letra -> {
            String nombre = "Despliegue " + (char) letra;
            String archivo = nombre + ".png";
            if (!new ClassPathResource("deploymentsAoS/" + archivo).exists()) {
                throw new IllegalStateException("No se ha encontrado el despliegue de AoS: " + nombre);
            }
            return new Deployment40kService.OpcionVisualView(archivo, nombre,
                    UriUtils.encodePath("/deploymentsAoS/" + archivo, StandardCharsets.UTF_8), "SIMETRICO");
        }).toList();
    }
}
