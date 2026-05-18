<%@ page import="java.util.List" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Ejercito40k" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Unidad40k" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Creador de listas 40k</title>
  </head>
  <body>
    <%
      Ejercito40k ejercitoData = (Ejercito40k) request.getAttribute("ejercitoData");
      List<Unidad40k> unidades = ejercitoData != null ? ejercitoData.unidades() : List.of();
    %>

    <h1>
      <%= request.getAttribute("nombreLista") %>
      -
      <span id="contadorPuntos">0</span> pts
    </h1>
    <p>Formato de juego: <span id="formatoJuegoTexto"><%= request.getAttribute("formatoJuego") %></span></p>
    <p>Ejercito: <%= request.getAttribute("ejercito") %></p>
    <p>Faccion: <%= request.getAttribute("faccion") %></p>
    <p><a href="/menu-principal">Volver</a></p>
    <p>
      <button type="button" id="botonGuardarLista" onclick="guardarLista()">Guardar lista</button>
      <span id="estadoGuardado"></span>
    </p>

    <table border="1" width="100%">
      <tr>
        <td width="33%" valign="top">
          <h2>Catalogo</h2>

          <h3>Personajes</h3>
          <div id="catalogo-personajes">
            <% for (Unidad40k unidad : unidades) {
                 String roles = unidad.roles() == null ? "" : unidad.roles().toLowerCase();
                 if (roles.contains("character") || roles.contains("epic hero")) { %>
              <button
                type="button"
                onclick="agregarUnidad('<%= unidad.nombre().replace("'", "\\'") %>', '<%= unidad.roles().replace("'", "\\'") %>', '<%= unidad.puntos().replace("'", "\\'") %>', '<%= unidad.armas().replace("'", "\\'") %>', '<%= unidad.habilidades().replace("'", "\\'") %>')">
                <%= unidad.nombre() %> (<%= unidad.puntos() %> pts)
              </button><br />
            <% }} %>
          </div>

          <h3>Linea</h3>
          <div id="catalogo-linea">
            <% for (Unidad40k unidad : unidades) {
                 String roles = unidad.roles() == null ? "" : unidad.roles().toLowerCase();
                 if (roles.contains("battleline")) { %>
              <button
                type="button"
                onclick="agregarUnidad('<%= unidad.nombre().replace("'", "\\'") %>', '<%= unidad.roles().replace("'", "\\'") %>', '<%= unidad.puntos().replace("'", "\\'") %>', '<%= unidad.armas().replace("'", "\\'") %>', '<%= unidad.habilidades().replace("'", "\\'") %>')">
                <%= unidad.nombre() %> (<%= unidad.puntos() %> pts)
              </button><br />
            <% }} %>
          </div>

          <h3>Transporte</h3>
          <div id="catalogo-transporte">
            <% for (Unidad40k unidad : unidades) {
                 String roles = unidad.roles() == null ? "" : unidad.roles().toLowerCase();
                 if (roles.contains("dedicated transport")) { %>
              <button
                type="button"
                onclick="agregarUnidad('<%= unidad.nombre().replace("'", "\\'") %>', '<%= unidad.roles().replace("'", "\\'") %>', '<%= unidad.puntos().replace("'", "\\'") %>', '<%= unidad.armas().replace("'", "\\'") %>', '<%= unidad.habilidades().replace("'", "\\'") %>')">
                <%= unidad.nombre() %> (<%= unidad.puntos() %> pts)
              </button><br />
            <% }} %>
          </div>

          <h3>Vehiculos y monstruos</h3>
          <div id="catalogo-pesado">
            <% for (Unidad40k unidad : unidades) {
                 String roles = unidad.roles() == null ? "" : unidad.roles().toLowerCase();
                 if (roles.contains("vehicle") || roles.contains("monster") || roles.contains("walker") || roles.contains("aircraft")) { %>
              <button
                type="button"
                onclick="agregarUnidad('<%= unidad.nombre().replace("'", "\\'") %>', '<%= unidad.roles().replace("'", "\\'") %>', '<%= unidad.puntos().replace("'", "\\'") %>', '<%= unidad.armas().replace("'", "\\'") %>', '<%= unidad.habilidades().replace("'", "\\'") %>')">
                <%= unidad.nombre() %> (<%= unidad.puntos() %> pts)
              </button><br />
            <% }} %>
          </div>

          <h3>Otras unidades</h3>
          <div id="catalogo-otros">
            <% for (Unidad40k unidad : unidades) {
                 String roles = unidad.roles() == null ? "" : unidad.roles().toLowerCase();
                 if (!roles.contains("character")
                         && !roles.contains("epic hero")
                         && !roles.contains("battleline")
                         && !roles.contains("dedicated transport")
                         && !roles.contains("vehicle")
                         && !roles.contains("monster")
                         && !roles.contains("walker")
                         && !roles.contains("aircraft")) { %>
              <button
                type="button"
                onclick="agregarUnidad('<%= unidad.nombre().replace("'", "\\'") %>', '<%= unidad.roles().replace("'", "\\'") %>', '<%= unidad.puntos().replace("'", "\\'") %>', '<%= unidad.armas().replace("'", "\\'") %>', '<%= unidad.habilidades().replace("'", "\\'") %>')">
                <%= unidad.nombre() %> (<%= unidad.puntos() %> pts)
              </button><br />
            <% }} %>
          </div>
        </td>

        <td width="34%" valign="top">
          <h2>Lista</h2>

          <h3>Personajes</h3>
          <div id="bloque-personajes">Vacio</div>

          <h3>Linea</h3>
          <div id="bloque-linea">Vacio</div>

          <h3>Transporte</h3>
          <div id="bloque-transporte">Vacio</div>

          <h3>Vehiculos y monstruos</h3>
          <div id="bloque-pesado">Vacio</div>

          <h3>Otras unidades</h3>
          <div id="bloque-otros">Vacio</div>
        </td>

        <td width="33%" valign="top">
          <h2>Unidad seleccionada</h2>
          <div id="panelVacio">Selecciona una unidad de la lista.</div>

          <div id="panelDetalle" style="display:none;">
            <p><strong id="detalleNombre"></strong></p>
            <p id="detalleMeta"></p>

            <p>Equipo principal</p>
            <select id="equipoPrincipal">
              <option value="">Sin elegir</option>
              <option value="equipo-base">Equipo base</option>
              <option value="equipo-alt">Opcion alternativa</option>
            </select>

            <p>Equipo secundario</p>
            <select id="equipoSecundario">
              <option value="">Sin elegir</option>
              <option value="sin-cambios">Sin cambios</option>
              <option value="mejora-1">Mejora 1</option>
              <option value="mejora-2">Mejora 2</option>
            </select>

            <p>Notas</p>
            <textarea id="notasUnidad" rows="6" cols="35"></textarea>

            <p>Habilidades</p>
            <textarea id="habilidadesUnidad" rows="8" cols="35" readonly></textarea>

            <p>Armas</p>
            <textarea id="armasUnidad" rows="8" cols="35" readonly></textarea>
          </div>
        </td>
      </tr>
    </table>

    <script>
      let contadorUnidad = 0;
      let unidadActiva = null;
      const formatoJuego = "<%= String.valueOf(request.getAttribute("formatoJuego")).replace("\"", "\\\"") %>";
      const nombreLista = "<%= String.valueOf(request.getAttribute("nombreLista")).replace("\"", "\\\"") %>";
      const faccion = "<%= String.valueOf(request.getAttribute("faccion")).replace("\"", "\\\"") %>";
      const ejercito = "<%= String.valueOf(request.getAttribute("ejercito")).replace("\"", "\\\"") %>";
      const limitePuntos = 2000;

      function obtenerBloquePorRol(roles) {
        const texto = (roles || "").toLowerCase();

        if (texto.includes("character") || texto.includes("epic hero")) {
          return "bloque-personajes";
        }
        if (texto.includes("battleline")) {
          return "bloque-linea";
        }
        if (texto.includes("dedicated transport")) {
          return "bloque-transporte";
        }
        if (texto.includes("vehicle") || texto.includes("monster") || texto.includes("walker") || texto.includes("aircraft")) {
          return "bloque-pesado";
        }
        return "bloque-otros";
      }

      function obtenerPuntosMinimos(textoPuntos) {
        if (!textoPuntos) {
          return 0;
        }

        const trozos = textoPuntos.split(",");
        let minimo = null;

        for (let i = 0; i < trozos.length; i++) {
          const numero = parseInt(trozos[i].trim(), 10);
          if (!isNaN(numero) && (minimo === null || numero < minimo)) {
            minimo = numero;
          }
        }

        return minimo === null ? 0 : minimo;
      }

      function actualizarContadorPuntos() {
        const unidades = document.querySelectorAll(".unidad-en-lista");
        let total = 0;

        for (let i = 0; i < unidades.length; i++) {
          total += parseInt(unidades[i].dataset.puntosBase || "0", 10);
        }

        document.getElementById("contadorPuntos").textContent = total;
      }

      function asegurarBloqueLimpio(idBloque) {
        const bloque = document.getElementById(idBloque);
        if (bloque.textContent.trim() === "Vacio") {
          bloque.textContent = "";
        }
      }

      function revisarBloqueVacio(idBloque) {
        const bloque = document.getElementById(idBloque);
        if (bloque.children.length === 0) {
          bloque.textContent = "Vacio";
        }
      }

      function agregarUnidad(nombre, roles, puntos, armas, habilidades) {
        const idBloque = obtenerBloquePorRol(roles);
        const bloque = document.getElementById(idBloque);
        asegurarBloqueLimpio(idBloque);

        contadorUnidad++;
        const puntosBase = obtenerPuntosMinimos(puntos);

        const contenedor = document.createElement("div");
        contenedor.className = "unidad-en-lista";
        contenedor.dataset.nombre = nombre;
        contenedor.dataset.roles = roles;
        contenedor.dataset.puntos = puntos;
        contenedor.dataset.puntosBase = puntosBase;
        contenedor.dataset.armas = armas;
        contenedor.dataset.habilidades = habilidades;
        contenedor.dataset.identificador = "unidad-" + contadorUnidad;
        contenedor.dataset.categoria = idBloque;
        contenedor.dataset.equipoPrincipal = "";
        contenedor.dataset.equipoSecundario = "";
        contenedor.dataset.notas = "";

        const botonSeleccion = document.createElement("button");
        botonSeleccion.type = "button";
        botonSeleccion.textContent = nombre + " (" + puntosBase + " pts)";
        botonSeleccion.onclick = function () {
          seleccionarUnidad(contenedor);
        };

        const botonDuplicar = document.createElement("button");
        botonDuplicar.type = "button";
        botonDuplicar.textContent = "Duplicar";
        botonDuplicar.onclick = function () {
          agregarUnidad(nombre, roles, puntos, armas, habilidades);
        };

        const botonEliminar = document.createElement("button");
        botonEliminar.type = "button";
        botonEliminar.textContent = "Eliminar";
        botonEliminar.onclick = function () {
          eliminarUnidad(contenedor, idBloque);
        };

        contenedor.appendChild(botonSeleccion);
        contenedor.appendChild(document.createTextNode(" "));
        contenedor.appendChild(botonDuplicar);
        contenedor.appendChild(document.createTextNode(" "));
        contenedor.appendChild(botonEliminar);
        contenedor.appendChild(document.createElement("br"));

        bloque.appendChild(contenedor);
        actualizarContadorPuntos();
      }

      function seleccionarUnidad(contenedor) {
        guardarCambiosUnidadActiva();
        unidadActiva = contenedor.dataset.identificador;
        document.getElementById("panelVacio").style.display = "none";
        document.getElementById("panelDetalle").style.display = "block";
        document.getElementById("detalleNombre").textContent = contenedor.dataset.nombre;
        document.getElementById("detalleMeta").textContent = "Rol: " + (contenedor.dataset.roles || "Sin rol") + " | Puntos: " + (contenedor.dataset.puntos || "Sin coste");
        document.getElementById("equipoPrincipal").value = contenedor.dataset.equipoPrincipal || "";
        document.getElementById("equipoSecundario").value = contenedor.dataset.equipoSecundario || "";
        document.getElementById("notasUnidad").value = contenedor.dataset.notas || "";
        document.getElementById("habilidadesUnidad").value = contenedor.dataset.habilidades || "Sin habilidades cargadas";
        document.getElementById("armasUnidad").value = contenedor.dataset.armas || "Sin armas cargadas";
      }

      function eliminarUnidad(contenedor, idBloque) {
        if (unidadActiva === contenedor.dataset.identificador) {
          unidadActiva = null;
          document.getElementById("panelVacio").style.display = "block";
          document.getElementById("panelDetalle").style.display = "none";
        }

        contenedor.remove();
        revisarBloqueVacio(idBloque);
        actualizarContadorPuntos();
      }

      function obtenerUnidadActiva() {
        if (!unidadActiva) {
          return null;
        }

        return document.querySelector('[data-identificador="' + unidadActiva + '"]');
      }

      function guardarCambiosUnidadActiva() {
        const contenedor = obtenerUnidadActiva();
        if (!contenedor) {
          return;
        }

        contenedor.dataset.equipoPrincipal = document.getElementById("equipoPrincipal").value || "";
        contenedor.dataset.equipoSecundario = document.getElementById("equipoSecundario").value || "";
        contenedor.dataset.notas = document.getElementById("notasUnidad").value || "";
      }

      function construirPayloadLista() {
        guardarCambiosUnidadActiva();

        const unidades = document.querySelectorAll(".unidad-en-lista");
        const listaUnidades = [];

        for (let i = 0; i < unidades.length; i++) {
          listaUnidades.push({
            nombre: unidades[i].dataset.nombre || "",
            roles: unidades[i].dataset.roles || "",
            puntos: unidades[i].dataset.puntos || "",
            puntosBase: parseInt(unidades[i].dataset.puntosBase || "0", 10),
            categoria: unidades[i].dataset.categoria || "",
            equipoPrincipal: unidades[i].dataset.equipoPrincipal || "",
            equipoSecundario: unidades[i].dataset.equipoSecundario || "",
            notas: unidades[i].dataset.notas || "",
            habilidades: unidades[i].dataset.habilidades || "",
            armas: unidades[i].dataset.armas || ""
          });
        }

        return {
          esquema: "1.0",
          formatoJuego: formatoJuego,
          nombreLista: nombreLista,
          faccion: faccion,
          ejercito: ejercito,
          limitePuntos: limitePuntos,
          puntosTotales: parseInt(document.getElementById("contadorPuntos").textContent || "0", 10),
          revisionCatalogo: "",
          unidades: listaUnidades
        };
      }

      async function guardarLista() {
        const estado = document.getElementById("estadoGuardado");
        const boton = document.getElementById("botonGuardarLista");
        const payload = construirPayloadLista();

        if (payload.unidades.length === 0) {
          estado.textContent = "Debes anadir al menos una unidad.";
          return;
        }

        estado.textContent = "Guardando...";
        boton.disabled = true;

        try {
          const datosFormulario = new URLSearchParams();
          datosFormulario.append("formatoJuego", formatoJuego);
          datosFormulario.append("nombreLista", nombreLista);
          datosFormulario.append("faccion", faccion);
          datosFormulario.append("ejercito", ejercito);
          datosFormulario.append("limitePuntos", String(limitePuntos));
          datosFormulario.append("puntosTotales", String(payload.puntosTotales));
          datosFormulario.append("revisionCatalogo", "");
          datosFormulario.append("datosListaJson", JSON.stringify(payload));

          const respuesta = await fetch("/creador-listas-40k/guardar", {
            method: "POST",
            headers: {
              "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
            },
            body: datosFormulario.toString()
          });

          const resultado = await respuesta.text();
          if (!respuesta.ok || !resultado.startsWith("OK|")) {
            if (resultado.startsWith("ERROR|")) {
              estado.textContent = resultado.substring(6);
            } else {
              estado.textContent = "No se ha podido guardar la lista.";
            }
            return;
          }

          estado.textContent = "Lista guardada. Version " + resultado.substring(3) + ".";
        } catch (error) {
          estado.textContent = "Error al guardar la lista.";
        } finally {
          boton.disabled = false;
        }
      }

      document.getElementById("equipoPrincipal").addEventListener("change", guardarCambiosUnidadActiva);
      document.getElementById("equipoSecundario").addEventListener("change", guardarCambiosUnidadActiva);
      document.getElementById("notasUnidad").addEventListener("input", guardarCambiosUnidadActiva);
    </script>
  </body>
</html>
