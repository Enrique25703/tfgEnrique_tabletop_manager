<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

      th,
      td {
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
    <c:url var="volverCatalogoUrl" value="/catalogo40k">
      <c:param name="faccion" value="${infoUnidad.faccionSeleccionada}" />
      <c:param name="ejercito" value="${infoUnidad.ejercitoSeleccionado}" />
    </c:url>

    <a class="link" href="${volverCatalogoUrl}">Volver al catalogo</a>

    <c:if test="${not empty infoUnidad}">
      <h1><c:out value="${infoUnidad.nombreUnidad}" /></h1>
      <p class="muted">
        <c:out value="${infoUnidad.faccionSeleccionada}" />
        -
        <c:out value="${infoUnidad.ejercitoSeleccionado}" />
      </p>

      <h2>Perfil de la unidad</h2>
      <table class="tabla-perfil">
        <tbody>
          <c:forEach var="estadistica" items="${infoUnidad.estadisticas}">
            <tr>
              <th><c:out value="${estadistica.nombre}" /></th>
              <td><c:out value="${estadistica.valor}" /></td>
            </tr>
          </c:forEach>
          <tr>
            <th>Equipamiento</th>
            <td class="texto-bloque"><c:out value="${infoUnidad.perfiles}" /></td>
          </tr>
          <tr>
            <th>Armas</th>
            <td class="texto-bloque"><c:out value="${infoUnidad.armas}" /></td>
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
          <c:forEach var="habilidad" items="${infoUnidad.habilidades}">
            <tr>
              <td><c:out value="${habilidad.nombre}" /></td>
              <td><c:out value="${habilidad.descripcion}" /></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if>
  </body>
</html>
