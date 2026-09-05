<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Eventos · <c:out value="${comunidadActual.nombre}" /></title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/comunidad-visor.css?v=1" />
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
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content community-view-page">
          <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/cabeceraComunidad.jsp" />
          <c:if test="${not empty mensajeOk}"><div class="note-box community-alert"><c:out value="${mensajeOk}" /></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="error-box community-alert"><c:out value="${mensajeError}" /></div></c:if>

          <section class="viewer-panel">
            <header class="viewer-panel-header">
              <div>
                <h2>Eventos de la comunidad</h2>
                <p>Consulta los próximos encuentros y administra los que organizas.</p>
              </div>
              <c:if test="${visor.puedeCrear}">
                <button class="button-primary" id="abrirCrearEvento" type="button">+ Crear evento</button>
              </c:if>
            </header>

            <c:choose>
              <c:when test="${empty visor.eventos}">
                <div class="empty-state"><strong>Todavía no hay eventos</strong><p>Crea el primer evento de esta comunidad.</p></div>
              </c:when>
              <c:otherwise>
                <div class="event-management-grid">
                  <c:forEach var="evento" items="${visor.eventos}">
                    <article class="managed-event-card">
                      <span class="event-date-tile"><span>▣</span><strong><c:out value="${evento.rondas}" /></strong><small>rondas</small></span>
                      <div class="managed-event-copy">
                        <h3><c:out value="${evento.titulo}" /></h3>
                        <p><c:out value="${evento.descripcion}" /></p>
                        <div class="managed-event-meta">
                          <span>▣ <c:out value="${evento.fecha}" /></span>
                          <span>⚔ <c:out value="${evento.formato}" /></span>
                          <span>⌖ <c:out value="${evento.ubicacion}" /></span>
                          <span>♙ Organiza <c:out value="${evento.organizador}" /></span>
                          <span><c:out value="${evento.participantes}" /><c:if test="${evento.maxParticipantes != null}">/<c:out value="${evento.maxParticipantes}" /></c:if> participantes</span>
                        </div>
                      </div>
                      <div class="event-actions">
                        <c:choose>
                          <c:when test="${evento.inscrito}">
                            <span class="state-badge">Inscrito</span>
                            <form action="/comunidades/eventos/desinscribirse" method="post" onsubmit="return confirm('¿Quieres desinscribirte de este evento?');">
                              <input type="hidden" name="eventoId" value="${evento.id}" />
                              <input type="hidden" name="comunidadId" value="${comunidadActual.id}" />
                              <button class="button-secondary" type="submit">Desinscribirme</button>
                            </form>
                          </c:when>
                          <c:when test="${evento.puedeInscribirse}">
                            <form action="/comunidades/eventos/unirse" method="post">
                              <input type="hidden" name="eventoId" value="${evento.id}" />
                              <input type="hidden" name="comunidadId" value="${comunidadActual.id}" />
                              <button class="button-secondary" type="submit">Inscribirme</button>
                            </form>
                          </c:when>
                        </c:choose>
                        <c:if test="${evento.puedeGestionar}">
                          <button
                            type="button"
                            class="button-secondary edit-event-button"
                            data-id="${evento.id}"
                            data-title="<c:out value='${evento.titulo}' />"
                            data-description="<c:out value='${evento.descripcion}' />"
                            data-date="${evento.fechaFormulario}"
                            data-format="${evento.codigoFormato}"
                            data-rounds="${evento.rondas}"
                            data-max="<c:out value='${evento.maxParticipantes}' />"
                            data-location="<c:out value='${evento.ubicacion}' />"
                            data-lat="${evento.latitud}"
                            data-lon="${evento.longitud}"
                          >Editar</button>
                          <form action="/comunidades/${comunidadActual.id}/eventos/${evento.id}/eliminar" method="post" onsubmit="return confirm('¿Seguro que quieres eliminar este evento?');">
                            <button class="button-secondary danger-action" type="submit">Eliminar</button>
                          </form>
                        </c:if>
                      </div>
                    </article>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </section>

          <section class="viewer-panel invitations-panel">
            <header class="viewer-panel-header">
              <div><h2>Invitaciones a partidas</h2><p>Propuestas informales publicadas por los miembros.</p></div>
              <c:if test="${visor.puedeCrearInvitacion}"><button class="button-secondary" id="abrirCrearInvitacion" type="button">+ Proponer partida</button></c:if>
            </header>
            <c:choose>
              <c:when test="${empty visor.invitaciones}"><div class="empty-state compact-state"><strong>No hay invitaciones publicadas</strong><p>Las nuevas propuestas aparecerán aquí.</p></div></c:when>
              <c:otherwise>
                <div class="invitation-grid">
                  <c:forEach var="invitacion" items="${visor.invitaciones}">
                    <article class="invitation-card">
                      <div><strong>Invitación de <c:out value="${invitacion.creador}" /></strong><p><c:out value="${invitacion.mensaje}" /></p></div>
                      <div class="managed-event-meta"><span>▣ <c:out value="${invitacion.fecha}" /></span><span>⚔ <c:out value="${invitacion.formato}" /></span><span>⌖ <c:out value="${invitacion.lugar}" /></span></div>
                    </article>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>

    <div id="modalEvento" class="viewer-modal" hidden aria-hidden="true">
      <div class="viewer-modal-backdrop" data-close-event-modal></div>
      <section class="viewer-dialog wide" role="dialog" aria-modal="true" aria-labelledby="tituloModalEvento">
        <header><div><h2 id="tituloModalEvento">Crear evento</h2><p>Selecciona la localización con OpenStreetMap.</p></div><button class="modal-close" type="button" data-close-event-modal aria-label="Cerrar">×</button></header>
        <form class="viewer-form" id="formEvento" action="/comunidades/${comunidadActual.id}/eventos/guardar" method="post">
          <input type="hidden" id="eventoId" name="eventoId" />
          <input type="hidden" id="latitudEvento" name="latitud" />
          <input type="hidden" id="longitudEvento" name="longitud" />
          <div class="form-grid">
            <label>Nombre<input id="tituloEvento" name="titulo" maxlength="150" required /></label>
            <label>Fecha y hora<input id="fechaEvento" name="fecha" type="datetime-local" required /></label>
            <label>Sistema de juego<select id="formatoEvento" name="formatoJuego" required><option value="WH40K_11">Warhammer 40.000</option><option value="AOS_4">Age of Sigmar</option></select></label>
            <label>Número de rondas<input id="rondasEvento" name="numeroRondas" type="number" min="1" max="20" required /></label>
            <label>Máximo de participantes<input id="maxEvento" name="maxParticipantes" type="number" min="1" placeholder="Sin límite" /></label>
          </div>
          <label>Descripción<textarea id="descripcionEvento" name="descripcion" rows="4" maxlength="5000"></textarea></label>
          <label>Buscar ubicación<div class="map-search-row"><input id="buscarUbicacionEvento" type="search" placeholder="Dirección o lugar" /><button class="button-secondary" id="botonBuscarUbicacion" type="button">Buscar</button></div></label>
          <label>Ubicación seleccionada<input id="lugarEvento" name="lugar" readonly required /></label>
          <div id="mapaEditorEvento" class="event-map-picker" aria-label="Mapa para seleccionar la ubicación del evento"></div>
          <p id="estadoMapaEvento" class="page-subtitle">Busca una dirección o pulsa sobre el mapa.</p>
          <div class="modal-actions"><button class="button-primary" id="guardarEventoTexto" type="submit">Crear evento</button><button class="button-secondary" type="button" data-close-event-modal>Cancelar</button></div>
        </form>
      </section>
    </div>

    <c:if test="${visor.puedeCrearInvitacion}">
      <div id="modalInvitacion" class="viewer-modal" hidden aria-hidden="true">
        <div class="viewer-modal-backdrop" data-close-invitation-modal></div>
        <section class="viewer-dialog" role="dialog" aria-modal="true" aria-labelledby="tituloModalInvitacion">
          <header><div><h2 id="tituloModalInvitacion">Proponer partida</h2><p>Publica una invitación para los demás miembros.</p></div><button class="modal-close" type="button" data-close-invitation-modal aria-label="Cerrar">×</button></header>
          <form class="viewer-form" action="/comunidades/invitaciones/crear" method="post">
            <input type="hidden" name="comunidadId" value="${comunidadActual.id}" />
            <label>Fecha y hora<input name="fecha" type="datetime-local" required /></label>
            <label>Lugar<input name="lugar" maxlength="150" required /></label>
            <label>Sistema de juego<select name="formatoJuego" required><option value="WH40K_11">Warhammer 40.000</option><option value="AOS_4">Age of Sigmar</option></select></label>
            <label>Mensaje<textarea name="mensaje" rows="4" maxlength="2000"></textarea></label>
            <div class="modal-actions"><button class="button-primary" type="submit">Publicar invitación</button><button class="button-secondary" type="button" data-close-invitation-modal>Cancelar</button></div>
          </form>
        </section>
      </div>
    </c:if>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
    <script src="/js/comunidad-eventos.js?v=1"></script>
  </body>
</html>
