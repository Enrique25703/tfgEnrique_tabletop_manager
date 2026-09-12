<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Info unidad 40k</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/catalogos.css" />
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

      .weapons-panel { margin-top: 18px; }
      .weapons-table-scroll { overflow-x: auto; }
      .weapons-table { min-width: 660px; }
      .weapons-table th:not(:first-child), .weapons-table td { text-align: center; }

      @media (max-width: 980px) {
        .two-column {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body class="catalog-screen">
    <c:url var="volverCatalogoUrl" value="/catalogo40k">
      <c:param name="faccion" value="${infoUnidad.faccionSeleccionada}" />
      <c:param name="ejercito" value="${infoUnidad.ejercitoSeleccionado}" />
      <c:param name="buscarUnidad" value="${param.buscarUnidad}" />
      <c:param name="ocultarLegends" value="${param.ocultarLegends}" />
      <c:param name="ocultarAliados" value="${param.ocultarAliados}" />
      <c:param name="ocultarEstructuras" value="${param.ocultarEstructuras}" />
    </c:url>

    <c:set var="sidebarActive" value="catalogos" scope="request" />
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
                <a class="link-inline" href="<c:out value='${volverCatalogoUrl}' />">Volver al catálogo</a>
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

            <c:forEach var="tipoArma" items="${['distancia', 'cuerpoACuerpo']}">
              <c:set var="perfilesArmas" value="${tipoArma eq 'distancia' ? infoUnidad.armasDistancia : infoUnidad.armasCuerpoACuerpo}" />
              <section class="sub-panel weapons-panel">
                <h3>${tipoArma eq 'distancia' ? 'Armas a distancia' : 'Armas cuerpo a cuerpo'}</h3>
                <c:choose>
                  <c:when test="${empty perfilesArmas}">
                    <p class="text-block">El catálogo no incluye perfiles de este tipo para esta unidad.</p>
                  </c:when>
                  <c:otherwise>
                    <div class="weapons-table-scroll" role="region" aria-label="Perfiles de armas ${tipoArma eq 'distancia' ? 'a distancia' : 'cuerpo a cuerpo'}" tabindex="0">
                      <table class="data-table weapons-table">
                        <thead><tr>
                          <th scope="col">Nombre</th><th scope="col">Rango</th>
                          <th scope="col">Ataques</th><th scope="col">Impacta</th>
                          <th scope="col">Fuerza</th><th scope="col">Penetración</th><th scope="col">Daño</th>
                        </tr></thead>
                        <tbody>
                          <c:forEach var="arma" items="${perfilesArmas}">
                            <tr>
                              <th scope="row"><c:out value="${arma.nombre}" /></th>
                              <td><c:out value="${arma.rango}" /></td>
                              <td><c:out value="${arma.ataques}" /></td>
                              <td><c:out value="${arma.impacta}" /></td>
                              <td><c:out value="${arma.fuerza}" /></td>
                              <td><c:out value="${arma.penetracion}" /></td>
                              <td><c:out value="${arma.dano}" /></td>
                            </tr>
                          </c:forEach>
                        </tbody>
                      </table>
                    </div>
                  </c:otherwise>
                </c:choose>
              </section>
            </c:forEach>

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
