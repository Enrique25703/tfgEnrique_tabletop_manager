<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Menu principal</title>
    <style>
      body {
        margin: 20px;
        font-family: Arial, sans-serif;
        background: #efefef;
        color: #1a1a1a;
      }

      h1 {
        margin-top: 0;
        margin-bottom: 8px;
        font-size: 28px;
      }

      p {
        margin-top: 0;
        color: #555555;
      }

      a,
      button,
      select,
      input {
        display: inline-block;
        margin-right: 10px;
        margin-top: 10px;
        padding: 10px 14px;
        border: 1px solid #999999;
        background: #e6e6e6;
        color: #111111;
        text-decoration: none;
        font-size: 14px;
      }

      button {
        cursor: pointer;
      }

      button[disabled] {
        cursor: not-allowed;
        color: #777777;
      }

      .popup {
        display: none;
        position: fixed;
        top: 60px;
        left: 60px;
        padding: 14px;
        background: #ffffff;
        border: 1px solid #888888;
      }

      .popup.visible {
        display: block;
      }

      .linea {
        margin-top: 10px;
      }
    </style>
  </head>
  <body>
    <div id="menuPrincipalApp">
      <h1>Menu principal</h1>
      <p>Bienvenido, <c:out value="${menuPrincipal.nombreUsuario}" /></p>
      <p>Esta pantalla aun esta a medio hacer.</p>
      <c:if test="${not empty menuPrincipal.errorCatalogo}">
        <p><c:out value="${menuPrincipal.errorCatalogo}" /></p>
      </c:if>

      <a href="/catalogo40k">Ver catalogo de 40k</a>
      <button type="button" disabled>Age of Sigmar</button>
      <button type="button" disabled>Ver comunidades</button>
      <a href="/mis-listas-40k">Ver mis listas</a>
      <button type="button" id="abrirPopupCreador">Creador de ejercitos</button>

      <div id="popupCreador" class="popup">
        <p>Elige el juego</p>
        <button type="button" id="mostrarFormulario40k">Warhammer 40.000 10º edicion</button>
        <button type="button" id="mostrarErrorAos">Age of Sigmar 4º edicion</button>
        <button type="button" id="cerrarPopupCreador">Cerrar</button>

        <form id="form40k" class="linea" action="/creador-listas-40k" method="get" style="display:none;">
          <input type="hidden" name="formatoJuego" value="WH40K_10" />
          <div class="linea">
            <label for="faccion">Faccion</label><br />
            <select id="faccion" name="faccion">
              <option value="">Selecciona una faccion</option>
              <c:forEach var="faccion" items="${menuPrincipal.facciones}">
                <option value="<c:out value='${faccion.nombre}'/>"><c:out value="${faccion.nombre}" /></option>
              </c:forEach>
            </select>
          </div>

          <div class="linea">
            <label for="ejercito">Ejercito</label><br />
            <select id="ejercito" name="ejercito">
              <option value="">Selecciona un ejercito</option>
            </select>
          </div>

          <div class="linea">
            <label for="nombreLista">Nombre de la lista</label><br />
            <input id="nombreLista" name="nombreLista" type="text" />
          </div>

          <div class="linea">
            <button type="submit">Crear</button>
          </div>
        </form>

        <select id="ejercitosPlantilla" hidden>
          <c:forEach var="faccion" items="${menuPrincipal.facciones}">
            <c:forEach var="ejercito" items="${faccion.ejercitos}">
              <option
                data-faccion="<c:out value='${faccion.nombre}'/>"
                value="<c:out value='${ejercito}'/>"><c:out value="${ejercito}" /></option>
            </c:forEach>
          </c:forEach>
        </select>
      </div>
    </div>

    <script src="/js/menu-principal.js"></script>
  </body>
</html>
