<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Catalogo Warhammer 40k</title>
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

      .topbar {
        display: flex;
        gap: 12px;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
      }

      .link {
        color: #2563eb;
        font-weight: 700;
        text-decoration: none;
      }

      .panel {
        background: #ffffff;
        border: 1px solid #d1d5db;
        margin-bottom: 20px;
        padding: 16px;
      }

      form {
        display: grid;
        gap: 12px;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        align-items: end;
      }

      label {
        display: block;
        font-weight: 700;
        margin-bottom: 6px;
      }

      select,
      button {
        width: 100%;
        box-sizing: border-box;
        padding: 10px;
        font: inherit;
      }

      button {
        border: 0;
        background: #111827;
        color: #ffffff;
        cursor: pointer;
        font-weight: 700;
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
        position: sticky;
        top: 0;
      }

      tr:nth-child(even) {
        background: #f9fafb;
      }

      .muted {
        color: #6b7280;
      }

      .error {
        background: #fee2e2;
        border: 1px solid #fca5a5;
        color: #7f1d1d;
        margin-bottom: 20px;
        padding: 12px;
      }
    </style>
  </head>
  <body>
    <div class="topbar">
      <div>
        <h1>Catalogo Warhammer 40k</h1>
        <p class="muted">Datos actualizados desde BSData wh40k-10e al abrir la pagina.</p>
      </div>
      <a class="link" href="/menu-principal">Volver al menu</a>
    </div>

    <c:if test="${not empty paginaCatalogo.errorCatalogo}">
      <div class="error">
        <c:out value="${paginaCatalogo.errorCatalogo}" />. Se muestran los ultimos datos cargados si existen.
      </div>
    </c:if>

    <div class="panel">
      <form id="filtroCatalogoForm" method="get" action="/catalogo40k">
        <div>
          <label for="faccion">Faccion</label>
          <select id="faccion" name="faccion">
            <option value="">Selecciona una faccion</option>
            <c:forEach var="faccion" items="${paginaCatalogo.facciones}">
              <option value="<c:out value='${faccion.nombre}'/>" <c:if test="${faccion.nombre eq paginaCatalogo.faccionSeleccionada}">selected</c:if>>
                <c:out value="${faccion.nombre}" />
              </option>
            </c:forEach>
          </select>
        </div>

        <div>
          <label for="ejercito">Ejercito</label>
          <select
            id="ejercito"
            name="ejercito"
            <c:if test="${empty paginaCatalogo.ejercitosDisponibles}">disabled</c:if>>
            <option value="">Selecciona un ejercito</option>
            <c:forEach var="ejercito" items="${paginaCatalogo.ejercitosDisponibles}">
              <option value="<c:out value='${ejercito.nombre}'/>" <c:if test="${ejercito.nombre eq paginaCatalogo.ejercitoSeleccionado}">selected</c:if>>
                <c:out value="${ejercito.nombre}" />
              </option>
            </c:forEach>
          </select>
        </div>

        <button type="submit">Ver unidades</button>
      </form>
    </div>

    <c:choose>
      <c:when test="${not empty paginaCatalogo.ejercito}">
        <h2>
          <c:out value="${paginaCatalogo.ejercito.faccion}" />
          -
          <c:out value="${paginaCatalogo.ejercito.nombre}" />
        </h2>
        <p class="muted"><c:out value="${paginaCatalogo.ejercito.totalUnidades}" /> unidades encontradas.</p>

        <table>
          <thead>
            <tr>
              <th>Nombre</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="unidad" items="${paginaCatalogo.ejercito.unidades}">
              <c:url var="detalleUnidadUrl" value="/infoUnidad40k">
                <c:param name="faccion" value="${paginaCatalogo.ejercito.faccion}" />
                <c:param name="ejercito" value="${paginaCatalogo.ejercito.nombre}" />
                <c:param name="unidad" value="${unidad.nombre}" />
              </c:url>
              <tr>
                <td>
                  <a class="link" href="${detalleUnidadUrl}">
                    <c:out value="${unidad.nombre}" />
                  </a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <p class="muted">Selecciona una faccion y un ejercito para ver sus unidades.</p>
      </c:otherwise>
    </c:choose>

    <script src="/js/catalogo-40k.js"></script>
  </body>
</html>
