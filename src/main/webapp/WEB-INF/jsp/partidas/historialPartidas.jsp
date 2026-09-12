<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Historial de partidas</title>
    <link rel="stylesheet" href="<c:url value='/css/app-shell.css' />" />
    <link rel="stylesheet" href="<c:url value='/css/historial-partidas.css' />" />
    <script src="<c:url value='/js/historial-partidas.js' />" defer></script>
  </head>
  <body>
    <c:set var="sidebarActive" value="partidas" scope="request" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />
      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${historial.nombreUsuario}" /></p>
          </div>
        </header>
        <main class="page-content">
          <section class="page-panel">
            <div class="history-heading">
              <div class="page-header">
                <h2 class="page-title">Partidas</h2>
                <p class="page-subtitle">Consulta tus partidas finalizadas, sus jugadores y puntuaciones.</p>
              </div>
              <a class="button-primary" href="<c:url value='/partidas/nueva' />">Nueva partida</a>
            </div>
            <c:if test="${not empty mensajeOk}">
              <div class="note-box" role="status"><c:out value="${mensajeOk}" /></div>
            </c:if>
            <c:if test="${not empty mensajeError}">
              <div class="error-box" role="alert"><c:out value="${mensajeError}" /></div>
            </c:if>
            <form class="history-filters" method="get" action="<c:url value='/partidas' />">
              <label>Formato de juego
                <select name="formatoJuego">
                  <option value="">Todos los formatos</option>
                  <c:forEach items="${historial.formatos}" var="formato">
                    <option value="<c:out value='${formato.codigo}' />" ${formatoJuego eq formato.codigo ? 'selected' : ''}><c:out value="${formato.nombre}" /></option>
                  </c:forEach>
                </select>
              </label>
              <label>Desde
                <input type="date" name="desde" value="<c:out value='${desde}' />" />
              </label>
              <label>Hasta
                <input type="date" name="hasta" value="<c:out value='${hasta}' />" />
              </label>
              <label>Resultado
                <select name="resultado">
                  <option value="">Todos los resultados</option>
                  <option value="VICTORIA" ${resultado eq 'VICTORIA' ? 'selected' : ''}>Victorias</option>
                  <option value="DERROTA" ${resultado eq 'DERROTA' ? 'selected' : ''}>Derrotas</option>
                  <option value="EMPATE" ${resultado eq 'EMPATE' ? 'selected' : ''}>Empates</option>
                </select>
              </label>
              <button class="button-primary" type="submit">Filtrar</button>
              <a class="button-secondary" href="<c:url value='/partidas' />">Limpiar filtros</a>
            </form>
            <p class="history-help">Fechas de finalización, ambos días incluidos. Los resultados se muestran desde tu perspectiva.</p>
            <p class="history-count"><c:out value="${historial.partidas.size()}" /> partidas encontradas</p>
            <c:choose>
              <c:when test="${empty historial.partidas}">
                <div class="note-box">No hay partidas finalizadas para estos filtros. Puedes limpiar los filtros o empezar una nueva partida.</div>
              </c:when>
              <c:otherwise>
                <div class="history-table-scroll" role="region" aria-label="Historial de partidas" tabindex="0">
                  <table class="data-table history-table">
                    <caption class="history-help">Solo el creador puede eliminar una partida del registro.</caption>
                    <thead>
                      <tr>
                        <th scope="col">Fecha</th>
                        <th scope="col">Sistema de juego</th>
                        <th scope="col">Jugador 1</th>
                        <th scope="col">Puntuación</th>
                        <th scope="col">Jugador 2</th>
                        <th scope="col">Resultado</th>
                        <th scope="col">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach items="${historial.partidas}" var="partida">
                        <tr>
                          <td class="history-date"><c:out value="${partida.fecha}" /></td>
                          <td><c:out value="${partida.sistemaJuego}" /></td>
                          <td><c:out value="${partida.jugador1}" /></td>
                          <td class="history-score"><c:out value="${partida.puntos1}" /> – <c:out value="${partida.puntos2}" /></td>
                          <td><c:out value="${partida.jugador2}" /></td>
                          <td>
                            <span class="history-result history-result-${partida.resultado}">
                              <c:choose>
                                <c:when test="${partida.resultado eq 'VICTORIA'}">Victoria</c:when>
                                <c:when test="${partida.resultado eq 'DERROTA'}">Derrota</c:when>
                                <c:when test="${partida.resultado eq 'EMPATE'}">Empate</c:when>
                                <c:otherwise>No participaste</c:otherwise>
                              </c:choose>
                            </span>
                          </td>
                          <td>
                            <c:choose>
                              <c:when test="${partida.puedeEliminar}">
                                <form method="post" action="<c:url value='/partidas/${partida.id}/eliminar' />" data-eliminar-partida>
                                  <button class="button-secondary history-delete" type="submit"
                                          aria-label="Eliminar partida de <c:out value='${partida.jugador1}' /> contra <c:out value='${partida.jugador2}' />">Eliminar</button>
                                </form>
                              </c:when>
                              <c:otherwise><span class="history-help">Solo el creador</span></c:otherwise>
                            </c:choose>
                          </td>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>
                </div>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
