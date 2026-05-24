<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

      th,
      td {
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
    <h1>Mis listas de Warhammer 40.000</h1>
    <p>Usuario: <c:out value="${misListas.nombreUsuario}" /></p>
    <p><a href="/menu-principal">Volver al menu principal</a></p>

    <c:choose>
      <c:when test="${empty misListas.listas}">
        <p>No tienes listas guardadas.</p>
      </c:when>
      <c:otherwise>
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
            <c:forEach var="lista" items="${misListas.listas}">
              <c:url var="detalleListaUrl" value="/mi-lista-40k">
                <c:param name="listaId" value="${lista.listaId}" />
              </c:url>
              <tr>
                <td>
                  <a href="${detalleListaUrl}">
                    <c:out value="${lista.nombreLista}" />
                  </a>
                </td>
                <td><c:out value="${lista.faccion}" /></td>
                <td><c:out value="${lista.ejercito}" /></td>
                <td><c:out value="${lista.puntos}" /></td>
                <td><c:out value="${lista.numeroVersion}" /></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </body>
</html>
