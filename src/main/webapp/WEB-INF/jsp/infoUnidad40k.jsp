<%@ page import="java.util.List" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Unidad40k" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Estadistica40k" %>
<%@ page import="org.example.tfgenrique.service.Catalogo40kService.Habilidad40k" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Info unidad 40k</title>
    
  </head>
  <body>
    <%
      Unidad40k unidad = (Unidad40k) request.getAttribute("unidad");
      String faccionSeleccionada = (String) request.getAttribute("faccionSeleccionada");
      String ejercitoSeleccionado = (String) request.getAttribute("ejercitoSeleccionado");
      List<Estadistica40k> estadisticas = unidad != null ? unidad.estadisticas() : List.of();
      List<Habilidad40k> habilidades = unidad != null ? unidad.habilidadesDetalle() : List.of();
    %>

    <a class="link" href="/catalogo40k?faccion=<%= faccionSeleccionada %>&ejercito=<%= ejercitoSeleccionado %>">Volver al catalogo</a>

    <% if (unidad != null) { %>
      <h1><%= unidad.nombre() %></h1>
      <h1>ESTO DEBE DE SER UN POPUP AL FINAL</h1>
      <p class="muted"><%= faccionSeleccionada %> - <%= ejercitoSeleccionado %></p>

      <div class="stats-grid">
        <% for (Estadistica40k estadistica : estadisticas) { %>
          <div class="stat-box">
            <strong><%= estadistica.nombre() %></strong>
            <span><%= estadistica.valor() %></span>
          </div>
        <% } %>
      </div>

      <h2 class="section-title">Habilidades</h2>
      <table>
        <thead>
          <tr>
            <th>Habilidad</th>
            <th>Explicacion</th>
          </tr>
        </thead>
        <tbody>
          <% for (Habilidad40k habilidad : habilidades) { %>
            <tr>
              <td><%= habilidad.nombre() %></td>
              <td><%= habilidad.descripcion() == null || habilidad.descripcion().isBlank() ? "Sin descripcion" : habilidad.descripcion() %></td>
            </tr>
          <% } %>
        </tbody>
      </table>
    <% } %>
  </body>
</html>
