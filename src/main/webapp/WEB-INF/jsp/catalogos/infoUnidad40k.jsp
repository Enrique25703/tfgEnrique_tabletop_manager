<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Info unidad 40k</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .two-column {
        display: grid;
        grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
        gap: 18px;
      }

      .sub-panel {
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-muted);
      }

      .sub-panel h3 {
        margin-top: 0;
      }

      .text-block {
        white-space: pre-line;
        color: var(--muted);
      }

      @media (max-width: 980px) {
        .two-column {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body>
    <c:url var="volverCatalogoUrl" value="/catalogo40k">
      <c:param name="faccion" value="${infoUnidad.faccionSeleccionada}" />
      <c:param name="ejercito" value="${infoUnidad.ejercitoSeleccionado}" />
    </c:url>

    <c:set var="sidebarActive" value="catalogos" />
    <c:set var="sidebarComunidadesEnabled" value="false" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title"><c:out value="${infoUnidad.nombreUnidad}" /></h2>
              <p class="page-subtitle">
                <a class="link-inline" href="${volverCatalogoUrl}">Volver al catalogo</a>
                ·
                <c:out value="${infoUnidad.faccionSeleccionada}" />
                -
                <c:out value="${infoUnidad.ejercitoSeleccionado}" />
              </p>
            </div>

            <div class="two-column">
              <div class="sub-panel">
                <h3>Perfil de la unidad</h3>
                <table class="data-table">
                  <tbody>
                    <c:forEach var="estadistica" items="${infoUnidad.estadisticas}">
                      <tr>
                        <th><c:out value="${estadistica.nombre}" /></th>
                        <td><c:out value="${estadistica.valor}" /></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>

              <div class="sub-panel">
                <h3>Equipo y armas</h3>
                <p class="card-kicker">Equipamiento</p>
                <p class="text-block"><c:out value="${infoUnidad.perfiles}" /></p>
                <p class="card-kicker" style="margin-top:18px;">Armas</p>
                <p class="text-block"><c:out value="${infoUnidad.armas}" /></p>
              </div>
            </div>

            <div class="sub-panel" style="margin-top:18px;">
              <h3>Habilidades</h3>
              <table class="data-table">
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
            </div>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
