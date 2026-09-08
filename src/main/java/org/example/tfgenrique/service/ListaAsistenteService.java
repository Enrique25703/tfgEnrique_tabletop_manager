package org.example.tfgenrique.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ListaAsistenteService {
    private static final int TAMANO_MAXIMO_JSON = 1_000_000;
    private static final Pattern EXPRESION_DADOS = Pattern.compile("(?:(\\d+))?D(\\d+)(?:([+-])(\\d+))?");
    private static final Pattern PRIMER_ENTERO = Pattern.compile("-?\\d+");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ListaAsistenteService() {
    }

    public AnalisisListaView analizar(String datosListaJson, String pregunta) {
        String consulta = normalizarPregunta(pregunta);
        JsonNode lista = leerLista(datosListaJson);
        int limite = entero(lista.path("limitePuntos"));
        String faccion = texto(lista.path("faccion"));
        String ejercito = texto(lista.path("ejercito"));
        List<JsonNode> unidades = new ArrayList<>();
        List<String> destacamentos = new ArrayList<>();
        List<String> disposiciones = new ArrayList<>();

        for (JsonNode unidad : lista.path("unidades")) {
            String tipoEspecial = texto(unidad.path("tipoEspecial"));
            if (tipoEspecial.isBlank()) {
                unidades.add(unidad);
            } else if ("destacamentos".equalsIgnoreCase(tipoEspecial)) {
                unidad.path("configuracionMiniaturas").path("seleccionados")
                        .forEach(valor -> destacamentos.add(texto(valor)));
            } else if ("disposicion".equalsIgnoreCase(tipoEspecial)) {
                unidad.path("configuracionMiniaturas").path("seleccionados")
                        .forEach(valor -> disposiciones.add(texto(valor)));
            }
        }

        if (unidades.isEmpty()) {
            return new AnalisisListaView(
                    "Todavia no hay unidades que pueda analizar.",
                    List.of(),
                    List.of("Añade algunas unidades y vuelve a preguntarme. Analizare el equilibrio de puntos, roles y objetivos."),
                    0,
                    limite,
                    0
            );
        }

        int puntos = unidades.stream().mapToInt(unidad -> entero(unidad.path("puntosBase"))).sum();
        int battleline = contarBooleano(unidades, "battleline");
        int personajes = contarBooleano(unidades, "character");
        int warlords = contarBooleano(unidades, "warlord");
        int moviles = contarPorTexto(unidades, "fly", "mounted", "jump pack", "biker", "transport", "aircraft");
        int resistentes = contarPorTexto(unidades, "vehicle", "monster", "walker", "titan", "fortification");
        Map<String, Integer> repeticiones = contarRepeticiones(unidades);

        List<String> fortalezas = new ArrayList<>();
        List<String> recomendaciones = new ArrayList<>();
        analizarPuntos(puntos, limite, recomendaciones, fortalezas);
        analizarMando(personajes, warlords, recomendaciones, fortalezas);
        analizarObjetivos(battleline, unidades.size(), limite, recomendaciones, fortalezas);
        analizarVariedad(moviles, resistentes, unidades.size(), recomendaciones, fortalezas);
        analizarRepeticiones(repeticiones, unidades, recomendaciones);
        analizarDestacamentos(destacamentos, recomendaciones, fortalezas);
        analizarDisposiciones(disposiciones, unidades, recomendaciones, fortalezas);
        analizarArmamento(unidades, consulta, recomendaciones, fortalezas);

        if (recomendaciones.isEmpty()) {
            recomendaciones.add("La estructura general parece equilibrada. El siguiente paso es comprobar que el equipamiento cubre infanteria, unidades resistentes y amenazas a distancia.");
        }

        String contexto = String.join(" ", faccion, ejercito).trim();
        String resumen = "He analizado " + unidades.size() + (unidades.size() == 1 ? " unidad" : " unidades")
                + " por " + puntos + (limite > 0 ? " de " + limite : "") + " puntos"
                + (contexto.isBlank() ? "." : " de " + contexto + ".");
        return new AnalisisListaView(
                adaptarResumen(resumen, consulta),
                List.copyOf(fortalezas),
                List.copyOf(recomendaciones),
                puntos,
                limite,
                unidades.size()
        );
    }

    private JsonNode leerLista(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("No se han recibido los datos de la lista.");
        }
        if (json.length() > TAMANO_MAXIMO_JSON) {
            throw new IllegalArgumentException("La lista es demasiado grande para analizarla.");
        }
        try {
            JsonNode raiz = objectMapper.readTree(json);
            if (!raiz.isObject() || !raiz.path("unidades").isArray()) {
                throw new IllegalArgumentException("El formato de la lista no es valido.");
            }
            return raiz;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se ha podido leer la lista.");
        }
    }

    private void analizarPuntos(int puntos, int limite, List<String> recomendaciones, List<String> fortalezas) {
        if (limite <= 0) return;
        int diferencia = limite - puntos;
        if (diferencia < 0) {
            recomendaciones.add("Reduce al menos " + Math.abs(diferencia) + " puntos para respetar el limite.");
        } else if (diferencia == 0) {
            fortalezas.add("Aprovechas exactamente el limite de puntos.");
        } else if (diferencia > Math.max(100, limite / 10)) {
            recomendaciones.add("Quedan " + diferencia + " puntos libres. Puedes añadir otra unidad o reforzar las existentes.");
        } else {
            fortalezas.add("La lista aprovecha bien el limite: solo quedan " + diferencia + " puntos.");
        }
    }

    private void analizarMando(int personajes, int warlords, List<String> recomendaciones, List<String> fortalezas) {
        if (personajes == 0) {
            recomendaciones.add("Añade al menos un Character para disponer de liderazgo y poder designar un Warlord.");
        } else {
            fortalezas.add("Incluyes " + personajes + (personajes == 1 ? " Character." : " Characters."));
        }
        if (warlords == 0) recomendaciones.add("Designa exactamente una de tus unidades Character como Warlord.");
        else if (warlords > 1) recomendaciones.add("Hay varios Warlords; deja solamente uno.");
        else fortalezas.add("La lista tiene un Warlord definido.");
    }

    private void analizarObjetivos(int battleline, int unidades, int limite,
                                   List<String> recomendaciones, List<String> fortalezas) {
        int recomendado = limite >= 1500 ? 2 : 1;
        if (battleline < recomendado) {
            recomendaciones.add("Tienes " + battleline + " unidad(es) Battleline. Considera llegar a " + recomendado
                    + " para disputar objetivos y ocupar mesa.");
        } else {
            fortalezas.add("Cuentas con " + battleline + " unidad(es) Battleline para jugar objetivos.");
        }
        if (unidades < (limite >= 1500 ? 5 : 3)) {
            recomendaciones.add("La lista tiene pocas activaciones. Dividir parte de los puntos entre más unidades puede darte mayor presencia en mesa.");
        }
    }

    private void analizarVariedad(int moviles, int resistentes, int unidades,
                                  List<String> recomendaciones, List<String> fortalezas) {
        if (moviles == 0) {
            recomendaciones.add("No detecto unidades especialmente moviles. Valora FLY, Mounted, Transport o unidades rápidas para alcanzar objetivos.");
        } else {
            fortalezas.add("Detecto " + moviles + " unidad(es) con palabras clave de movilidad.");
        }
        if (resistentes == 0 && unidades >= 4) {
            recomendaciones.add("No detecto Vehicle, Monster o Walker. Comprueba que alguna unidad pueda absorber daño y proteger el centro.");
        }
    }

    private void analizarRepeticiones(Map<String, Integer> repeticiones, List<JsonNode> unidades,
                                      List<String> recomendaciones) {
        repeticiones.forEach((nombre, cantidad) -> {
            if (cantidad >= 3 && unidades.stream()
                    .filter(unidad -> nombre.equalsIgnoreCase(texto(unidad.path("nombre"))))
                    .noneMatch(unidad -> unidad.path("battleline").asBoolean(false))) {
                recomendaciones.add("Repites " + cantidad + " veces " + nombre
                        + ". Comprueba el limite de copias y si otra unidad cubriría una función diferente.");
            }
        });
    }

    private void analizarDestacamentos(List<String> destacamentos,
                                       List<String> recomendaciones, List<String> fortalezas) {
        if (destacamentos.isEmpty()) {
            recomendaciones.add("Selecciona un destacamento para que la lista tenga una regla de juego y una identidad claras.");
        } else {
            fortalezas.add("Has seleccionado " + destacamentos.size()
                    + (destacamentos.size() == 1 ? " destacamento." : " destacamentos."));
        }
    }

    private void analizarDisposiciones(List<String> disposiciones, List<JsonNode> unidades,
                                       List<String> recomendaciones, List<String> fortalezas) {
        List<String> seleccionadas = disposiciones.stream().filter(valor -> !valor.isBlank()).distinct().toList();
        if (seleccionadas.isEmpty()) {
            recomendaciones.add("Selecciona una disposición para adaptar las funciones de las unidades al plan de misión.");
            return;
        }

        fortalezas.add("La lista está preparada para la disposición " + String.join(", ", seleccionadas) + ".");
        for (String disposicion : seleccionadas) {
            String clave = normalizarParaComparar(disposicion);
            if (contieneAlguno(clave, "recon", "disruption")) {
                List<String> candidatas = seleccionarUnidadesParaAccion(unidades, false);
                if (candidatas.isEmpty()) {
                    recomendaciones.add("Para " + disposicion
                            + ", añade una unidad barata con Infantry, Scout, Infiltrator, Deep Strike o buena movilidad para realizar acciones sin perder potencia principal.");
                } else {
                    recomendaciones.add("Para " + disposicion + ", priorizaría " + unirNombres(candidatas)
                            + " como unidades de acción por su movilidad, despliegue, condición Battleline o coste. Comprueba las restricciones exactas de la misión antes de asignarlas.");
                }
            }
            if (clave.contains("priority assets") || clave.contains("take and hold")) {
                List<String> candidatas = seleccionarUnidadesParaAccion(unidades, true);
                if (candidatas.isEmpty()) {
                    recomendaciones.add("Para " + disposicion
                            + ", añade una unidad Battleline o resistente que pueda permanecer sobre los activos mientras el resto del ejército combate.");
                } else {
                    recomendaciones.add("Para " + disposicion + ", usaría " + unirNombres(candidatas)
                            + " para asegurar los activos por su presencia Battleline, Infantry o resistencia detectada.");
                }
            }
        }
    }

    private List<String> seleccionarUnidadesParaAccion(List<JsonNode> unidades, boolean mantenerObjetivos) {
        Map<String, CandidatoUnidad> candidatas = new LinkedHashMap<>();
        for (JsonNode unidad : unidades) {
            String nombre = texto(unidad.path("nombre"));
            if (nombre.isBlank()) continue;
            String descripcion = normalizarParaComparar(nombre + " " + texto(unidad.path("roles"))
                    + " " + texto(unidad.path("palabrasClave")));
            int puntos = entero(unidad.path("puntosBase"));
            int puntuacion = mantenerObjetivos
                    ? puntuacionMantenerObjetivo(unidad, descripcion, puntos)
                    : puntuacionAccionAvanzada(unidad, descripcion, puntos);
            if (puntuacion > 0) {
                candidatas.merge(nombre, new CandidatoUnidad(nombre, puntuacion, puntos),
                        (actual, nueva) -> actual.puntuacion() >= nueva.puntuacion() ? actual : nueva);
            }
        }
        return candidatas.values().stream()
                .sorted(Comparator.comparingInt(CandidatoUnidad::puntuacion).reversed()
                        .thenComparingInt(CandidatoUnidad::puntos)
                        .thenComparing(CandidatoUnidad::nombre))
                .limit(3)
                .map(CandidatoUnidad::nombre)
                .toList();
    }

    private int puntuacionAccionAvanzada(JsonNode unidad, String descripcion, int puntos) {
        int puntuacion = 0;
        if (contieneAlguno(descripcion, "recon", "scout", "infiltrator")) puntuacion += 6;
        if (contieneAlguno(descripcion, "deep strike", "fly", "mounted", "biker", "jump pack")) puntuacion += 4;
        if (descripcion.contains("infantry")) puntuacion += 3;
        if (unidad.path("battleline").asBoolean(false)) puntuacion += 3;
        if (puntos > 0 && puntos <= 120) puntuacion += 2;
        if (unidad.path("character").asBoolean(false)) puntuacion -= 2;
        return puntuacion;
    }

    private int puntuacionMantenerObjetivo(JsonNode unidad, String descripcion, int puntos) {
        int puntuacion = 0;
        if (unidad.path("battleline").asBoolean(false)) puntuacion += 6;
        if (contieneAlguno(descripcion, "vehicle", "monster", "walker", "titan")) puntuacion += 5;
        if (descripcion.contains("infantry")) puntuacion += 3;
        if (descripcion.contains("transport")) puntuacion += 2;
        if (puntos > 0 && puntos <= 150) puntuacion += 1;
        return puntuacion;
    }

    private void analizarArmamento(List<JsonNode> unidades, String pregunta,
                                   List<String> recomendaciones, List<String> fortalezas) {
        List<ArmaAnalizada> armas = recopilarArmas(unidades);
        if (armas.isEmpty()) return;

        List<ArmaAnalizada> altoDano = armas.stream()
                .filter(this::esArmaContraResistentes)
                .sorted(Comparator.comparingDouble(this::puntuacionContraResistentes).reversed())
                .limit(3)
                .toList();
        List<ArmaAnalizada> saturacion = armas.stream()
                .filter(this::esArmaDeSaturacion)
                .sorted(Comparator.comparingDouble(this::puntuacionSaturacion).reversed())
                .limit(3)
                .toList();

        String consulta = normalizarParaComparar(pregunta);
        boolean preguntaResistentes = contieneAlguno(consulta, "resistent", "tanque", "blindad", "vehicle",
                "monster", "elite", "muchas heridas", "alta dureza", "alta resistencia", "dano alto");
        boolean preguntaNumerosos = contieneAlguno(consulta, "horda", "numeros", "muchos modelos", "muchas unidades",
                "pocas heridas", "infanteria ligera", "muchos disparos", "poco dano", "saturacion");

        if (preguntaResistentes) recomendarContraResistentes(altoDano, recomendaciones);
        if (preguntaNumerosos) recomendarContraNumerosos(saturacion, recomendaciones);
        if (!preguntaResistentes && !preguntaNumerosos) {
            if (altoDano.isEmpty()) {
                recomendaciones.add("No detecto perfiles claros contra unidades resistentes. Valora armas de Daño 3 o superior, Fuerza alta y buena penetración.");
            } else {
                fortalezas.add("Tienes opciones de daño alto contra objetivos resistentes: " + describirArmas(altoDano, 2) + ".");
            }
            if (saturacion.isEmpty()) {
                recomendaciones.add("No detecto mucha saturación a distancia. Contra ejércitos numerosos, busca perfiles de muchos ataques y Daño 1 o 2.");
            } else {
                fortalezas.add("Tienes perfiles de saturación contra unidades numerosas: " + describirArmas(saturacion, 2) + ".");
            }
        }
    }

    private void recomendarContraResistentes(List<ArmaAnalizada> armas, List<String> recomendaciones) {
        if (armas.isEmpty()) {
            recomendaciones.add("Contra un ejército resistente te faltan perfiles antiblindaje claros: busca Daño 3 o superior, Fuerza alta y FP -2 o mejor.");
        } else {
            recomendaciones.add("Contra unidades resistentes, prioriza entre tus perfiles disponibles "
                    + describirArmas(armas, 3) + ". Su combinación de Fuerza, penetración y Daño es la más adecuada de tu lista.");
        }
    }

    private void recomendarContraNumerosos(List<ArmaAnalizada> armas, List<String> recomendaciones) {
        if (armas.isEmpty()) {
            recomendaciones.add("Contra ejércitos numerosos te falta saturación: incorpora armas a distancia con 4 o más ataques medios y Daño 1 o 2.");
        } else {
            recomendaciones.add("Contra muchas miniaturas de pocas heridas, prioriza " + describirArmas(armas, 3)
                    + ". Generan muchos ataques y evitan desperdiciar perfiles de Daño alto.");
        }
    }

    private List<ArmaAnalizada> recopilarArmas(List<JsonNode> unidades) {
        Map<String, ArmaAnalizada> resultado = new LinkedHashMap<>();
        for (JsonNode unidad : unidades) {
            String nombreUnidad = texto(unidad.path("nombre"));
            for (JsonNode perfil : unidad.path("perfilesArmas")) {
                String nombreArma = texto(perfil.path("nombre"));
                String tipo = texto(perfil.path("tipo"));
                String rango = texto(perfil.path("rango"));
                if (nombreArma.isBlank() || esCuerpoACuerpo(tipo, rango)) continue;
                ArmaAnalizada arma = new ArmaAnalizada(
                        nombreUnidad, nombreArma,
                        texto(perfil.path("ataques")), texto(perfil.path("fuerza")),
                        texto(perfil.path("penetracion")), texto(perfil.path("dano")),
                        valorMedio(texto(perfil.path("ataques"))),
                        primerEntero(texto(perfil.path("fuerza"))),
                        Math.abs(primerEntero(texto(perfil.path("penetracion")))),
                        valorMedio(texto(perfil.path("dano")))
                );
                resultado.putIfAbsent(normalizarParaComparar(nombreUnidad + "\u0000" + nombreArma), arma);
            }
        }
        return List.copyOf(resultado.values());
    }

    private boolean esCuerpoACuerpo(String tipo, String rango) {
        String valor = normalizarParaComparar(tipo + " " + rango);
        return valor.contains("melee") || valor.contains("cuerpo a cuerpo");
    }

    private boolean esArmaContraResistentes(ArmaAnalizada arma) {
        return arma.danoMedio() >= 3 || (arma.fuerza() >= 9 && arma.penetracion() >= 2);
    }

    private boolean esArmaDeSaturacion(ArmaAnalizada arma) {
        return arma.ataquesMedios() >= 4 && arma.danoMedio() > 0 && arma.danoMedio() <= 2;
    }

    private double puntuacionContraResistentes(ArmaAnalizada arma) {
        return arma.danoMedio() * 3 + arma.fuerza() * 0.5 + arma.penetracion() * 2 + arma.ataquesMedios() * 0.2;
    }

    private double puntuacionSaturacion(ArmaAnalizada arma) {
        return arma.ataquesMedios() * 2 + (arma.danoMedio() <= 1 ? 2 : 0) + arma.fuerza() * 0.1;
    }

    private String describirArmas(List<ArmaAnalizada> armas, int limite) {
        return armas.stream().limit(limite).map(arma -> arma.nombreArma() + " de " + arma.nombreUnidad()
                        + " (A " + valorVisible(arma.ataques()) + ", F " + valorVisible(arma.fuerzaTexto())
                        + ", FP " + valorVisible(arma.penetracionTexto()) + ", D " + valorVisible(arma.danoTexto()) + ")")
                .reduce((primera, siguiente) -> primera + "; " + siguiente)
                .orElse("ningún perfil");
    }

    private double valorMedio(String expresion) {
        String valor = expresion == null ? "" : expresion.toUpperCase(Locale.ROOT).replace(" ", "");
        Matcher dados = EXPRESION_DADOS.matcher(valor);
        if (dados.find()) {
            int cantidad = dados.group(1) == null ? 1 : Integer.parseInt(dados.group(1));
            int caras = Integer.parseInt(dados.group(2));
            double resultado = cantidad * (caras + 1) / 2.0;
            if (dados.group(4) != null) {
                int modificador = Integer.parseInt(dados.group(4));
                resultado += "-".equals(dados.group(3)) ? -modificador : modificador;
            }
            return Math.max(0, resultado);
        }
        return Math.max(0, primerEntero(valor));
    }

    private int primerEntero(String valor) {
        Matcher matcher = PRIMER_ENTERO.matcher(valor == null ? "" : valor);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    private String valorVisible(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private boolean contieneAlguno(String valor, String... terminos) {
        for (String termino : terminos) {
            if (valor.contains(termino)) return true;
        }
        return false;
    }

    private String normalizarParaComparar(String valor) {
        String normalizado = Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private String unirNombres(List<String> nombres) {
        if (nombres.size() <= 1) return nombres.isEmpty() ? "" : nombres.get(0);
        return String.join(", ", nombres.subList(0, nombres.size() - 1)) + " y " + nombres.get(nombres.size() - 1);
    }

    private Map<String, Integer> contarRepeticiones(List<JsonNode> unidades) {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (JsonNode unidad : unidades) {
            String nombre = texto(unidad.path("nombre"));
            if (!nombre.isBlank()) resultado.merge(nombre, 1, Integer::sum);
        }
        return resultado;
    }

    private int contarBooleano(List<JsonNode> unidades, String campo) {
        return (int) unidades.stream().filter(unidad -> unidad.path(campo).asBoolean(false)).count();
    }

    private int contarPorTexto(List<JsonNode> unidades, String... terminos) {
        return (int) unidades.stream().filter(unidad -> {
            String texto = (texto(unidad.path("roles")) + " " + texto(unidad.path("palabrasClave")))
                    .toLowerCase(Locale.ROOT);
            for (String termino : terminos) if (texto.contains(termino)) return true;
            return false;
        }).count();
    }

    private String adaptarResumen(String resumen, String pregunta) {
        String consulta = pregunta == null ? "" : pregunta.trim();
        if (consulta.isBlank()) return resumen;
        return resumen + " Sobre tu pregunta, estas son las mejoras que priorizaria.";
    }

    private String normalizarPregunta(String pregunta) {
        String consulta = pregunta == null ? "" : pregunta.trim();
        if (consulta.length() > 300) {
            throw new IllegalArgumentException("La pregunta no puede superar los 300 caracteres.");
        }
        return consulta;
    }

    private int entero(JsonNode nodo) {
        return nodo.isNumber() ? Math.max(0, nodo.asInt()) : 0;
    }

    private String texto(JsonNode nodo) {
        return nodo.isTextual() ? nodo.asText("").trim() : "";
    }

    public record AnalisisListaView(
            String resumen,
            List<String> fortalezas,
            List<String> recomendaciones,
            int puntosTotales,
            int limitePuntos,
            int totalUnidades
    ) {
    }

    private record CandidatoUnidad(String nombre, int puntuacion, int puntos) {
    }

    private record ArmaAnalizada(
            String nombreUnidad,
            String nombreArma,
            String ataques,
            String fuerzaTexto,
            String penetracionTexto,
            String danoTexto,
            double ataquesMedios,
            int fuerza,
            int penetracion,
            double danoMedio
    ) {
    }
}
