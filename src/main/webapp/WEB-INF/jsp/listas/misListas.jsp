<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Mis listas</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .filters-bar {
        display: flex;
        justify-content: space-between;
        gap: 16px;
        align-items: end;
        margin-bottom: 18px;
        flex-wrap: wrap;
      }

      .filter-form {
        display: flex;
        gap: 12px;
        align-items: end;
        flex-wrap: wrap;
      }

      .summary-chip {
        padding: 8px 12px;
        border: 1px solid var(--line);
        border-radius: 999px;
        color: var(--muted);
        background: rgba(14, 22, 34, 0.62);
      }

      .export-button {
        padding: 8px 12px;
        white-space: nowrap;
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="listas" />
    <c:set var="sidebarComunidadesEnabled" value="false" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${misListas.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Mis listas</h2>
              <p class="page-subtitle">Consulta las listas guardadas y filtralas por juego.</p>
            </div>

            <div class="filters-bar">
              <form class="filter-form" method="get" action="/mis-listas">
                <label>
                  Juego
                  <select name="formatoJuego">
                    <c:forEach var="formato" items="${misListas.formatosDisponibles}">
                      <c:choose>
                        <c:when test="${misListas.formatoJuegoSeleccionado eq formato.codigo}">
                          <option value="${formato.codigo}" selected><c:out value="${formato.nombre}" /></option>
                        </c:when>
                        <c:otherwise>
                          <option value="${formato.codigo}"><c:out value="${formato.nombre}" /></option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </select>
                </label>
                <button class="button-primary" type="submit">Filtrar</button>
                <button type="button" class="button-secondary" id="abrirPopupCreador">Crear lista</button>
              </form>

              <span class="summary-chip"><c:out value="${misListas.listas.size()}" /> listas</span>
            </div>

            <c:choose>
              <c:when test="${empty misListas.listas}">
                <div class="note-box">No tienes listas guardadas para el filtro seleccionado.</div>
              </c:when>
              <c:otherwise>
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th>Juego</th>
                      <th>Faccion</th>
                      <th>Ejercito</th>
                      <th>Puntos</th>
                      <th>Version</th>
                      <th>Exportar</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="lista" items="${misListas.listas}">
                      <c:url var="detalleListaUrl" value="/mi-lista-40k">
                        <c:param name="listaId" value="${lista.listaId}" />
                      </c:url>
                      <c:url var="exportarListaUrl" value="/mis-listas/exportar">
                        <c:param name="listaId" value="${lista.listaId}" />
                      </c:url>
                      <tr>
                        <td>
                          <a class="link-inline" href="${detalleListaUrl}">
                            <c:out value="${lista.nombreLista}" />
                          </a>
                        </td>
                        <td><c:out value="${lista.formatoJuego}" /></td>
                        <td><c:out value="${lista.faccion}" /></td>
                        <td><c:out value="${lista.ejercito}" /></td>
                        <td><c:out value="${lista.puntos}" /></td>
                        <td><c:out value="${lista.numeroVersion}" /></td>
                        <td>
                          <c:choose>
                            <c:when test="${lista.codigoFormatoJuego eq 'WH40K_11'}">
                              <a class="button-secondary export-button" href="${exportarListaUrl}">Exportar</a>
                            </c:when>
                            <c:otherwise>
                              <span class="summary-chip">No disponible</span>
                            </c:otherwise>
                          </c:choose>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
