<%@ page import="java.util.List" %>
<%@ page import="org.example.tfgenrique.service.CreacionListasService.ListaGuardadaView" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Mis listas 40k</title>
    <style>
      body {
        margin: 20px;
        font-family: Arial, sans-serif;
      }

      table {
        border-collapse: collapse;
        width: 100%;
        margin-bottom: 28px;
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
      List<ListaGuardadaView> listasGuardadas = (List<ListaGuardadaView>) request.getAttribute("listasGuardadas");
      if (listasGuardadas == null) {
        listasGuardadas = List.of();
      }
    %>

    <h1>Mis listas de Warhammer 40.000</h1>
    <p>Usuario: <%= request.getAttribute("nombreUsuario") %></p>
    <p><a href="/menu-principal">Volver al menu principal</a></p>

    <% if (listasGuardadas.isEmpty()) { %>
      <p>No tienes listas guardadas.</p>
    <% } else { %>
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Faccion</th>
            <th>Ejercito</th>
            <th>Puntos</th>
            <th>Version</th>
          </tr>
        </thead>
        <tbody>
          <% for (ListaGuardadaView lista : listasGuardadas) { %>
            <tr>
              <td>
                <a href="/mi-lista-40k?listaId=<%= lista.listaId() %>">
                  <%= lista.nombreLista() %>
                </a>
              </td>
              <td><%= lista.faccion().isBlank() ? "Sin faccion" : lista.faccion() %></td>
              <td><%= lista.ejercito().isBlank() ? "Sin ejercito" : lista.ejercito() %></td>
              <td><%= lista.puntosActuales() %> / <%= lista.limitePuntos() %></td>
              <td><%= lista.numeroVersion() %></td>
            </tr>
          <% } %>
        </tbody>
      </table>
    <% } %>
  </body>
</html>
