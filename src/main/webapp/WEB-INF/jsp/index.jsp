<%@ page import="java.util.List" %>
<%@ page import="org.example.tfgenrique.entity.Usuario" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Menu principal</title>
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

      .subtitle {
        margin-top: 0;
        margin-bottom: 20px;
        color: #4b5563;
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
      }

      tr:nth-child(even) {
        background: #f9fafb;
      }

      .empty {
        text-align: center;
        color: #6b7280;
      }
    </style>
  </head>
  <body>
    <%
      List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    %>

    <h1>Menu principal</h1>
    <p class="subtitle">Listado completo de usuarios registrados en la base de datos.</p>

    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Nombre de usuario</th>
          <th>Email</th>
          <th>Contrasena hash</th>
          <th>Rol</th>
          <th>Activo</th>
          <th>Creado en</th>
          <th>Actualizado en</th>
        </tr>
      </thead>
      <tbody>
        <%
          if (usuarios != null && !usuarios.isEmpty()) {
              for (Usuario usuario : usuarios) {
        %>
        <tr>
          <td><%= usuario.getId() %></td>
          <td><%= usuario.getNombreUsuario() %></td>
          <td><%= usuario.getEmail() %></td>
          <td><%= usuario.getContrasenaHash() %></td>
          <td><%= usuario.getRol() %></td>
          <td><%= usuario.getActivo() %></td>
          <td><%= usuario.getCreadoEn() %></td>
          <td><%= usuario.getActualizadoEn() %></td>
        </tr>
        <%
              }
          } else {
        %>
        <tr>
          <td class="empty" colspan="8">No hay usuarios disponibles.</td>
        </tr>
        <%
          }
        %>
      </tbody>
    </table>
  </body>
</html>
