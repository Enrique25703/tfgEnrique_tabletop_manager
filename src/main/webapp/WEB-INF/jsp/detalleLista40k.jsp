<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
    <p><a href="/mis-listas-40k">Volver a mis listas</a></p>

    <c:if test="${not empty detalleLista}">
      <h1><c:out value="${detalleLista.nombreLista}" /></h1>
      <p>
        Faccion: <c:out value="${detalleLista.faccion}" />
        |
        Ejercito: <c:out value="${detalleLista.ejercito}" />
        |
        Puntos: <c:out value="${detalleLista.puntos}" />
        |
        Version: <c:out value="${detalleLista.numeroVersion}" />
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
          <c:forEach var="unidad" items="${detalleLista.unidades}">
            <c:url var="detalleUnidadUrl" value="/infoUnidad40k">
              <c:param name="faccion" value="${detalleLista.faccion}" />
              <c:param name="ejercito" value="${detalleLista.ejercito}" />
              <c:param name="unidad" value="${unidad.nombreUnidad}" />
            </c:url>
            <tr>
              <td>
                <a href="${detalleUnidadUrl}">
                  <c:out value="${unidad.nombreUnidad}" />
                </a>
              </td>
              <td><c:out value="${unidad.roles}" /></td>
              <td><c:out value="${unidad.puntosBase}" /></td>
              <td><c:out value="${unidad.categoria}" /></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if>
  </body>
</html>
