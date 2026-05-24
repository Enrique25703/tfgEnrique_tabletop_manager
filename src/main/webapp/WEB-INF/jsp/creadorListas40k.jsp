<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Creador de listas 40k</title>
  </head>
  <body>
    <div
      id="creadorListaApp"
      data-formato-juego="<c:out value='${creadorLista.formatoJuego}'/>"
      data-nombre-lista="<c:out value='${creadorLista.nombreLista}'/>"
      data-faccion="<c:out value='${creadorLista.faccion}'/>"
      data-ejercito="<c:out value='${creadorLista.ejercito}'/>"
      data-limite-puntos="<c:out value='${creadorLista.limitePuntos}'/>">

      <h1>
        <c:out value="${creadorLista.nombreLista}" />
        -
        <span id="contadorPuntos">0</span> pts
      </h1>
      <p>Formato de juego: <span id="formatoJuegoTexto"><c:out value="${creadorLista.formatoJuego}" /></span></p>
      <p>Ejercito: <c:out value="${creadorLista.ejercito}" /></p>
      <p>Faccion: <c:out value="${creadorLista.faccion}" /></p>
      <p><a href="/menu-principal">Volver</a></p>
      <p>
        <button type="button" id="botonGuardarLista">Guardar lista</button>
        <span id="estadoGuardado"></span>
      </p>

      <table border="1" width="100%">
        <tr>
          <td width="33%" valign="top">
            <h2>Catalogo</h2>

            <c:forEach var="categoria" items="${creadorLista.categorias}">
              <h3><c:out value="${categoria.titulo}" /></h3>
              <div id="catalogo-${categoria.id}">
                <c:forEach var="unidad" items="${categoria.unidades}">
                  <button
                    type="button"
                    class="boton-catalogo-unidad"
                    data-nombre="<c:out value='${unidad.nombre}'/>"
                    data-roles="<c:out value='${unidad.roles}'/>"
                    data-puntos="<c:out value='${unidad.puntos}'/>"
                    data-puntos-base="<c:out value='${unidad.puntosBase}'/>"
                    data-categoria="<c:out value='${unidad.categoria}'/>"
                    data-configuracion-json="<c:out value='${unidad.configuracionJson}'/>">
                    <c:out value="${unidad.nombre}" /> (<c:out value="${unidad.puntos}" /> pts)
                  </button><br />
                </c:forEach>
              </div>
            </c:forEach>
          </td>

          <td width="34%" valign="top">
            <h2>Lista</h2>

            <c:forEach var="categoria" items="${creadorLista.categorias}">
              <h3><c:out value="${categoria.titulo}" /></h3>
              <div id="bloque-${categoria.id}">Vacio</div>
            </c:forEach>
          </td>

          <td width="33%" valign="top">
            <h2>Unidad seleccionada</h2>
            <div id="panelVacio">Selecciona una unidad de la lista.</div>

            <div id="panelDetalle" style="display:none;">
              <p><strong id="detalleNombre"></strong></p>
              <p id="detalleMeta"></p>

              <div id="configuracionUnidad"></div>

              <p>Notas</p>
              <textarea id="notasUnidad" rows="6" cols="35"></textarea>
            </div>
          </td>
        </tr>
      </table>
    </div>

    <script src="/js/creador-listas-40k.js"></script>
  </body>
</html>
