<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Eventos cercanos · Comunidades</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/comunidades-actual.css?v=2" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin="" />
  </head>
  <body>
    <c:set var="sidebarActive" value="comunidades" />
    <c:set var="sidebarComunidadesEnabled" value="true" />

    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />
      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${comunidades.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content communities-page">
          <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/cabeceraListadoComunidades.jsp" />

          <c:if test="${not empty mensajeOk}"><div class="note-box community-alert"><c:out value="${mensajeOk}" /></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="error-box community-alert"><c:out value="${mensajeError}" /></div></c:if>

          <section class="community-summary" aria-labelledby="tituloResumen">
            <div>
              <h2 id="tituloResumen">Resumen rápido</h2>
              <p>Tu actividad en comunidades</p>
            </div>
            <div class="summary-grid">
              <article class="summary-card summary-blue">
                <span class="summary-icon" aria-hidden="true">♙</span>
                <div><strong><c:out value="${comunidades.totalComunidades}" /></strong><span>Comunidades unidas</span><small>Sigue formando parte de ellas</small></div>
              </article>
              <article class="summary-card summary-green">
                <span class="summary-icon" aria-hidden="true">▣</span>
                <div><strong><c:out value="${comunidades.totalEventosProximos}" /></strong><span>Eventos cercanos</span><small>Próximos o actualmente en curso</small></div>
              </article>
              <article class="summary-card summary-purple">
                <span class="summary-icon" aria-hidden="true">☆</span>
                <div><strong><c:out value="${comunidades.totalSugerencias}" /></strong><span>Sugerencias para ti</span><small>Comunidades que podrían interesarte</small></div>
              </article>
            </div>
          </section>

          <section class="events-workspace" aria-labelledby="tituloEventos">
            <div class="events-list-panel">
              <div class="section-heading">
                <div><h2 id="tituloEventos">Eventos cercanos en los que estás inscrito</h2><span class="count-badge"><c:out value="${comunidades.totalEventosProximos}" /></span></div>
                <span class="sort-label">Próximos primero</span>
              </div>

              <c:choose>
                <c:when test="${empty comunidades.eventosProximos}">
                  <div class="events-empty">
                    <span aria-hidden="true">▣</span>
                    <h3>No tienes eventos cercanos</h3>
                    <p>Se mostrarán los eventos futuros y los que todavía estén en curso.</p>
                  </div>
                </c:when>
                <c:otherwise>
                  <div class="event-cards">
                    <c:forEach var="evento" items="${comunidades.eventosProximos}" varStatus="estado">
                      <button class="event-card${estado.first ? ' active' : ''}" type="button" data-event-target="evento-${evento.id}" aria-expanded="${estado.first}">
                        <span class="event-emblem game-${evento.codigoFormato}"><span aria-hidden="true">⚔</span></span>
                        <span class="event-card-copy">
                          <span class="event-card-title"><c:out value="${evento.titulo}" /><span class="status-badge">Inscrito</span></span>
                          <span class="event-card-meta"><span>▣ <c:out value="${evento.fecha}" /> · <c:out value="${evento.hora}" /></span><span class="game-badge"><c:out value="${evento.formato}" /></span></span>
                          <span class="event-card-location">⌖ <c:out value="${evento.ubicacion}" /></span>
                        </span>
                        <span class="event-arrow" aria-hidden="true">›</span>
                      </button>
                    </c:forEach>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="event-details-panel" aria-live="polite">
              <c:choose>
                <c:when test="${empty comunidades.eventosProximos}">
                  <div class="event-detail-placeholder"><p>Cuando tengas una inscripción vigente podrás consultar aquí todos sus datos.</p></div>
                </c:when>
                <c:otherwise>
                  <c:forEach var="evento" items="${comunidades.eventosProximos}" varStatus="estado">
                    <article id="evento-${evento.id}" class="event-detail${estado.first ? ' active' : ''}" ${estado.first ? '' : 'hidden'}>
                      <header class="event-detail-header"><h2><c:out value="${evento.titulo}" /></h2><span class="status-badge">Inscrito</span></header>
                      <div class="event-facts">
                        <div class="event-fact"><span class="fact-icon">▣</span><div><small>Fecha y hora</small><strong><c:out value="${evento.fecha}" /> · <c:out value="${evento.hora}" /></strong></div></div>
                        <div class="event-fact"><span class="fact-icon">♙</span><div><small>Organiza</small><strong><c:out value="${evento.comunidad}" /></strong><span><c:out value="${evento.organizador}" /></span></div></div>
                        <div class="event-fact"><span class="fact-icon">⚔</span><div><small>Sistema de juego</small><strong><c:out value="${evento.formato}" /></strong></div></div>
                        <div class="event-fact"><span class="fact-icon">#</span><div><small>Rondas</small><strong><c:out value="${evento.rondas}" /> rondas</strong></div></div>
                      </div>
                      <section class="event-description"><h3>Descripción</h3><p><c:out value="${evento.descripcion}" /></p></section>
                      <section class="event-location-box">
                        <span class="fact-icon">⌖</span>
                        <div><small>Ubicación</small>
                          <c:choose>
                            <c:when test="${evento.tieneCoordenadas}">
                              <button type="button" class="location-map-button" data-map-title="<c:out value='${evento.titulo}' />" data-map-address="<c:out value='${evento.ubicacion}' />" data-map-lat="${evento.latitud}" data-map-lon="${evento.longitud}"><c:out value="${evento.ubicacion}" /></button>
                            </c:when>
                            <c:otherwise><strong><c:out value="${evento.ubicacion}" /></strong></c:otherwise>
                          </c:choose>
                        </div>
                      </section>
                    </article>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
            </div>
          </section>
        </main>
      </div>
    </div>

    <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/modalCrearComunidad.jsp" />

    <div id="modalUbicacionEvento" class="map-modal" hidden aria-hidden="true">
      <div class="map-modal-backdrop" data-close-map></div>
      <section class="map-dialog" role="dialog" aria-modal="true" aria-labelledby="tituloMapaEvento">
        <header><div><p class="map-kicker">Ubicación del evento</p><h2 id="tituloMapaEvento">Mapa</h2><p id="direccionMapaEvento"></p></div><button type="button" class="map-close" data-close-map aria-label="Cerrar mapa">×</button></header>
        <div id="mapaUbicacionEvento" class="event-map" aria-label="Mapa OpenStreetMap con la ubicación del evento"></div>
      </section>
    </div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
    <script src="/js/comunidades-actual.js?v=2"></script>
  </body>
</html>
