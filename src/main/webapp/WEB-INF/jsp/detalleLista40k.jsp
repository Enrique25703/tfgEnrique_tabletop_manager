<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.example.tfgenrique.service.CreacionListasService.ListaGuardadaView" %>
<%@ page import="org.example.tfgenrique.service.CreacionListasService.UnidadGuardadaView" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Detalle de lista 40k</title>
    <style>
      body {
        margin: 20px;
        font-family: Arial, sans-serif;
      }

      table {
        border-collapse: collapse;
        width: 100%;
      }

      th, td {
        border: 1px solid #888888;
        padding: 8px 10px;
        text-align: left;
      }

      th {
        background: #efefef;
      }
    </style>
  </head>
  <body>
    <%
      ListaGuardadaView lista = (ListaGuardadaView) request.getAttribute("listaGuardada");
    %>

    <p><a href="/mis-listas-40k">Volver a mis listas</a></p>

    <% if (lista != null) { %>
      <h1><%= lista.nombreLista() %></h1>
      <p>
        Faccion: <%= lista.faccion() %>
        |
        Ejercito: <%= lista.ejercito() %>
        |
        Puntos: <%= lista.puntosActuales() %> / <%= lista.limitePuntos() %>
        |
        Version: <%= lista.numeroVersion() %>
      </p>

      <table>
        <thead>
          <tr>
            <th>Unidad</th>
            <th>Rol</th>
            <th>Puntos</th>
            <th>Categoria</th>
          </tr>
        </thead>
        <tbody>
          <% for (UnidadGuardadaView unidad : lista.unidades()) { %>
            <tr>
              <td>
                <a href="/infoUnidad40k?faccion=<%= URLEncoder.encode(lista.faccion(), "UTF-8") %>&ejercito=<%= URLEncoder.encode(lista.ejercito(), "UTF-8") %>&unidad=<%= URLEncoder.encode(unidad.nombreUnidad(), "UTF-8") %>">
                  <%= unidad.nombreUnidad() %>
                </a>
              </td>
              <td><%= unidad.roles().isBlank() ? "Sin rol" : unidad.roles() %></td>
              <td><%= unidad.puntosBase() %></td>
              <td><%= unidad.categoria().isBlank() ? "Sin categoria" : unidad.categoria() %></td>
            </tr>
          <% } %>
        </tbody>
      </table>
    <% } %>
  </body>
</html>
