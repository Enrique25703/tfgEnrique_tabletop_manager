<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Catalogo Warhammer 40k</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .filter-panel {
        margin-bottom: 20px;
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-muted);
      }

      .filter-form {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 14px;
        align-items: end;
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="catalogos" />
    <c:set var="sidebarComunidadesEnabled" value="false" />
    <div class="app-shell">
      <jsp:include page="header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Catalogos</h2>
              <p class="page-subtitle">Consulta el contenido del repositorio de 40k actualizado desde BSData.</p>
            </div>

            <c:if test="${not empty paginaCatalogo.errorCatalogo}">
              <div class="error-box">
                <c:out value="${paginaCatalogo.errorCatalogo}" />. Se muestran los ultimos datos cargados si existen.
              </div>
            </c:if>

            <div class="filter-panel">
              <form id="filtroCatalogoForm" class="filter-form" method="get" action="/catalogo40k">
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
                  <select id="ejercito" name="ejercito" <c:if test="${empty paginaCatalogo.ejercitosDisponibles}">disabled</c:if>>
                    <option value="">Selecciona un ejercito</option>
                    <c:forEach var="ejercito" items="${paginaCatalogo.ejercitosDisponibles}">
                      <option value="<c:out value='${ejercito.nombre}'/>" <c:if test="${ejercito.nombre eq paginaCatalogo.ejercitoSeleccionado}">selected</c:if>>
                        <c:out value="${ejercito.nombre}" />
                      </option>
                    </c:forEach>
                  </select>
                </div>

                <button class="button-primary" type="submit">Ver unidades</button>
              </form>
            </div>

            <c:choose>
              <c:when test="${not empty paginaCatalogo.ejercito}">
                <div class="chip-row">
                  <span class="chip"><c:out value="${paginaCatalogo.ejercito.faccion}" /></span>
                  <span class="chip"><c:out value="${paginaCatalogo.ejercito.nombre}" /></span>
                  <span class="chip"><c:out value="${paginaCatalogo.ejercito.totalUnidades}" /> unidades</span>
                </div>

                <table class="data-table" style="margin-top:18px;">
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
                          <a class="link-inline" href="${detalleUnidadUrl}">
                            <c:out value="${unidad.nombre}" />
                          </a>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </c:when>
              <c:otherwise>
                <div class="note-box">Selecciona una faccion y un ejercito para ver sus unidades.</div>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>

    <script src="/js/catalogo-40k.js"></script>
  </body>
</html>
