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
    <style>
      body {
        margin: 20px;
        font-family: Arial, sans-serif;
      }

      table {
        border-collapse: collapse;
        width: 100%;
        margin-top: 12px;
      }

      th, td {
        border: 1px solid #888888;
        padding: 8px 10px;
        text-align: left;
        vertical-align: top;
      }

      th {
        background: #efefef;
      }

      .tabla-perfil th {
        width: 180px;
      }

      .texto-bloque {
        white-space: pre-line;
      }
    </style>
  </head>
  <body>
    <%
      Unidad40k unidad = (Unidad40k) request.getAttribute("unidad");
      String faccionSeleccionada = (String) request.getAttribute("faccionSeleccionada");
      String ejercitoSeleccionado = (String) request.getAttribute("ejercitoSeleccionado");
      List<Estadistica40k> estadisticas = unidad != null ? unidad.estadisticas() : List.of();
      List<Habilidad40k> habilidades = unidad != null ? unidad.habilidadesDetalle() : List.of();
      String perfiles = unidad != null && unidad.perfiles() != null && !unidad.perfiles().isBlank() ? unidad.perfiles() : "Sin equipamiento registrado";
      String armas = unidad != null && unidad.armas() != null && !unidad.armas().isBlank() ? unidad.armas() : "Sin armas registradas";
    %>

    <a class="link" href="/catalogo40k?faccion=<%= faccionSeleccionada %>&ejercito=<%= ejercitoSeleccionado %>">Volver al catalogo</a>

    <% if (unidad != null) { %>
      <h1><%= unidad.nombre() %></h1>
      <p class="muted"><%= faccionSeleccionada %> - <%= ejercitoSeleccionado %></p>

      <h2>Perfil de la unidad</h2>
      <table class="tabla-perfil">
        <tbody>
          <% for (Estadistica40k estadistica : estadisticas) { %>
            <tr>
              <th><%= estadistica.nombre() %></th>
              <td><%= estadistica.valor() %></td>
            </tr>
          <% } %>
          <tr>
            <th>Equipamiento</th>
            <td class="texto-bloque"><%= perfiles %></td>
          </tr>
          <tr>
            <th>Armas</th>
            <td class="texto-bloque"><%= armas %></td>
          </tr>
        </tbody>
      </table>

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
