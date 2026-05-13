<%@ page import="java.util.Map" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Ejercito40k" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Menu principal</title>
    <style>
      body {
        margin: 20px;
        font-family: Arial, sans-serif;
        background: #efefef;
        color: #1a1a1a;
      }

      h1 {
        margin-top: 0;
        margin-bottom: 8px;
        font-size: 28px;
      }

      p {
        margin-top: 0;
        color: #555555;
      }

      a,
      button,
      select,
      input {
        display: inline-block;
        margin-right: 10px;
        margin-top: 10px;
        padding: 10px 14px;
        border: 1px solid #999999;
        background: #e6e6e6;
        color: #111111;
        text-decoration: none;
        font-size: 14px;
      }

      button {
        cursor: pointer;
      }

      button[disabled] {
        cursor: not-allowed;
        color: #777777;
      }

      .popup {
        display: none;
        position: fixed;
        top: 60px;
        left: 60px;
        padding: 14px;
        background: #ffffff;
        border: 1px solid #888888;
      }

      .popup.visible {
        display: block;
      }

      .linea {
        margin-top: 10px;
      }
    </style>
  </head>
  <body>
    <%
      Catalogo40kData catalogo40k = (Catalogo40kData) request.getAttribute("catalogo40k");
      Map<String, Map<String, Ejercito40k>> facciones = catalogo40k != null ? catalogo40k.facciones() : Map.of();
      String errorCatalogo = (String) request.getAttribute("errorCatalogo");
    %>

    <h1>Menu principal</h1>
    <p>Bienvenido, <%= request.getAttribute("nombreUsuario") %></p>
    <p>Esta pantalla aun esta a medio hacer.</p>
    <% if (errorCatalogo != null) { %>
      <p><%= errorCatalogo %></p>
    <% } %>

    <a href="/catalogo40k">Ver catalogo de 40k</a>
    <button type="button" disabled>Age of Sigmar</button>
    <button type="button" disabled>Ver comunidades</button>
    <button type="button">Ver mis listas</button>
    <button type="button" onclick="abrirPopup()">Creador de ejercitos</button>

    <%-- Este popup lo he dejado simple a proposito para enseñar el flujo sin liar mas pantallas. --%>
    <div id="popupCreador" class="popup">
      <p>Elige el juego</p>
      <button type="button" onclick="mostrarFormulario40k()">Warhammer 40.000 10º edicion</button>
      <button type="button" onclick="errorAos()">Age of Sigmar 4º edicion</button>
      <button type="button" onclick="cerrarPopup()">Cerrar</button>

      <%-- Si entra en 40k aqui le pido los datos basicos para crear la lista de prueba. --%>
      <form id="form40k" class="linea" action="/creador-listas-40k" method="get" style="display:none;">
        <div class="linea">
          <label for="faccion">Faccion</label><br />
          <select id="faccion" name="faccion" onchange="actualizarEjercitos()">
            <option value="">Selecciona una faccion</option>
            <% for (String faccion : facciones.keySet()) { %>
              <option value="<%= faccion %>"><%= faccion %></option>
            <% } %>
          </select>
        </div>

        <div class="linea">
          <label for="ejercito">Ejercito</label><br />
          <select id="ejercito" name="ejercito">
            <option value="">Selecciona un ejercito</option>
          </select>
        </div>

        <div class="linea">
          <label for="nombreLista">Nombre de la lista</label><br />
          <input id="nombreLista" name="nombreLista" type="text" />
        </div>

        <div class="linea">
          <button type="submit">Crear</button>
        </div>
      </form>
    </div>

    <script>
      // Dejo esto ya cargado en js para no hacer otra petcion solo para rellenar el select.
      const catalogo40k = {
        <% for (Map.Entry<String, Map<String, Ejercito40k>> entry : facciones.entrySet()) { %>
        "<%= entry.getKey().replace("\"", "\\\"") %>": [
          <% for (String nombreEjercito : entry.getValue().keySet()) { %>
          "<%= nombreEjercito.replace("\"", "\\\"") %>",
          <% } %>
        ],
        <% } %>
      };

      function abrirPopup() {
        document.getElementById("popupCreador").classList.add("visible");
      }

      function cerrarPopup() {
        document.getElementById("popupCreador").classList.remove("visible");
      }

      function mostrarFormulario40k() {
        document.getElementById("form40k").style.display = "block";
      }

      function errorAos() {
        // Esto falla a proposito porque AoS no lo he montado aun.
        alert("Error al cargar Age of Sigmar 4º edicion.");
      }

      function actualizarEjercitos() {
        const faccion = document.getElementById("faccion").value;
        const selectEjercito = document.getElementById("ejercito");
        const ejercitos = catalogo40k[faccion] || [];

        // Cada vez que cambia la faccion rehago los ejercitos para que salgan los suyos.
        selectEjercito.innerHTML = '<option value="">Selecciona un ejercito</option>';

        for (let i = 0; i < ejercitos.length; i++) {
          const option = document.createElement("option");
          option.value = ejercitos[i];
          option.textContent = ejercitos[i];
          selectEjercito.appendChild(option);
        }
      }
    </script>
  </body>
</html>
