<%@ page import="java.util.Map" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Catalogo40kData" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Ejercito40k" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Unidad40k" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Catalogo Warhammer 40k</title>
    <style>
      body {
        font-family: Arial, sans-serif;
        margin: 32px;
        background: #f5f7fa;
        color: #1f2937;
      }

      h1 {
        margin-bottom: 8px;
      }

      .topbar {
        display: flex;
        gap: 12px;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
      }

      .link {
        color: #2563eb;
        font-weight: 700;
        text-decoration: none;
      }

      .panel {
        background: #ffffff;
        border: 1px solid #d1d5db;
        margin-bottom: 20px;
        padding: 16px;
      }

      form {
        display: grid;
        gap: 12px;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        align-items: end;
      }

      label {
        display: block;
        font-weight: 700;
        margin-bottom: 6px;
      }

      select,
      button {
        width: 100%;
        box-sizing: border-box;
        padding: 10px;
        font: inherit;
      }

      button {
        border: 0;
        background: #111827;
        color: #ffffff;
        cursor: pointer;
        font-weight: 700;
      }

      table {
        width: 100%;
        border-collapse: collapse;
        background: #ffffff;
      }

      th,
      td {
        border: 1px solid #d1d5db;
        padding: 10px;
        text-align: left;
        vertical-align: top;
      }

      th {
        background: #111827;
        color: #ffffff;
        position: sticky;
        top: 0;
      }

      tr:nth-child(even) {
        background: #f9fafb;
      }

      .muted {
        color: #6b7280;
      }

      .error {
        background: #fee2e2;
        border: 1px solid #fca5a5;
        color: #7f1d1d;
        margin-bottom: 20px;
        padding: 12px;
      }
    </style>
  </head>
  <body>
    <%
      Catalogo40kData catalogo = (Catalogo40kData) request.getAttribute("catalogo");
      Ejercito40k ejercito = (Ejercito40k) request.getAttribute("ejercito");
      String faccionSeleccionada = (String) request.getAttribute("faccionSeleccionada");
      String ejercitoSeleccionado = (String) request.getAttribute("ejercitoSeleccionado");
      String errorCatalogo = (String) request.getAttribute("errorCatalogo");
      Map<String, Map<String, Ejercito40k>> facciones = catalogo != null ? catalogo.facciones() : Map.of();
      Map<String, Ejercito40k> ejercitos = faccionSeleccionada != null ? facciones.get(faccionSeleccionada) : null;
    %>

    <div class="topbar">
      <div>
        <h1>Catalogo Warhammer 40k</h1>
        <p class="muted">Datos actualizados desde BSData wh40k-10e al abrir la pagina.</p>
      </div>
      <a class="link" href="/">Volver al menu</a>
    </div>

    <% if (errorCatalogo != null) { %>
      <div class="error"><%= errorCatalogo %>. Se muestran los ultimos datos cargados si existen.</div>
    <% } %>

    <div class="panel">
      <form method="get" action="/catalogo40k">
        <div>
          <label for="faccion">Faccion</label>
          <select id="faccion" name="faccion" onchange="this.form.submit()">
            <option value="">Selecciona una faccion</option>
            <% for (String faccion : facciones.keySet()) { %>
              <option value="<%= faccion %>" <%= faccion.equals(faccionSeleccionada) ? "selected" : "" %>><%= faccion %></option>
            <% } %>
          </select>
        </div>

        <div>
          <label for="ejercito">Ejercito</label>
          <select id="ejercito" name="ejercito" <%= ejercitos == null ? "disabled" : "" %>>
            <option value="">Selecciona un ejercito</option>
            <% if (ejercitos != null) {
                 for (String nombreEjercito : ejercitos.keySet()) { %>
              <option value="<%= nombreEjercito %>" <%= nombreEjercito.equals(ejercitoSeleccionado) ? "selected" : "" %>><%= nombreEjercito %></option>
            <%   }
               } %>
          </select>
        </div>

        <button type="submit">Ver unidades</button>
      </form>
    </div>

    <% if (ejercito != null) { %>
      <h2><%= ejercito.faccion() %> - <%= ejercito.nombre() %></h2>
      <p class="muted"><%= ejercito.unidades().size() %> unidades encontradas.</p>

      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Puntos</th>
            <th>Roles</th>
            <th>Faction keywords</th>
            <th>Keywords</th>
            <th>Perfil</th>
            <th>Habilidades</th>
            <th>Armas</th>
          </tr>
        </thead>
        <tbody>
          <% for (Unidad40k unidad : ejercito.unidades()) { %>
          <tr>
            <td><%= unidad.nombre() %></td>
            <td><%= unidad.puntos() %></td>
            <td><%= unidad.roles() %></td>
            <td><%= unidad.faccionKeywords() %></td>
            <td><%= unidad.keywords() %></td>
            <td><%= unidad.perfil() %></td>
            <td><%= unidad.habilidades() %></td>
            <td><%= unidad.armas() %></td>
          </tr>
          <% } %>
        </tbody>
      </table>
    <% } else { %>
      <p class="muted">Selecciona una faccion y un ejercito para ver sus unidades.</p>
    <% } %>
  </body>
</html>
