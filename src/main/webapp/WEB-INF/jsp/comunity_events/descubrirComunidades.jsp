<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Descubrir comunidades</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/comunidades-actual.css?v=2" />
  </head>
  <body>
    <c:set var="sidebarActive" value="comunidades" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />
      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card"><p class="profile-title">Mi perfil</p><p class="profile-role"><c:out value="${descubrir.nombreUsuario}" /></p></div>
        </header>
        <main class="page-content communities-page">
          <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/cabeceraListadoComunidades.jsp" />
          <c:if test="${not empty mensajeOk}"><div class="note-box community-alert"><c:out value="${mensajeOk}" /></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="error-box community-alert"><c:out value="${mensajeError}" /></div></c:if>

          <section class="discover-hero">
            <div><h2>Descubre tu próxima comunidad</h2><p>Encuentra nuevos jugadores y comparte tu pasión por los wargames.</p></div>
            <form class="discover-search" action="/comunidades/descubrir" method="get">
              <label><span aria-hidden="true">⌕</span><input name="busqueda" value="<c:out value='${descubrir.busqueda}' />" placeholder="Buscar comunidades o palabras clave..." /></label>
              <button class="button-primary" type="submit">Buscar</button>
              <c:if test="${not empty descubrir.busqueda}"><a class="button-secondary" href="/comunidades/descubrir">Limpiar</a></c:if>
            </form>
          </section>

          <div class="discover-workspace">
            <section class="discover-results-panel">
              <header><strong><c:out value="${descubrir.totalComunidades}" /> comunidades encontradas</strong><span>Ordenadas por nombre</span></header>
              <c:choose>
                <c:when test="${empty descubrir.comunidades}">
                  <div class="events-empty"><h3>No se han encontrado comunidades</h3><p>Prueba otra búsqueda o crea una nueva comunidad.</p></div>
                </c:when>
                <c:otherwise>
                  <div class="discover-result-list">
                    <c:forEach var="comunidad" items="${descubrir.comunidades}">
                      <c:url var="urlComunidad" value="/comunidades/descubrir"><c:param name="comunidadId" value="${comunidad.id}" /><c:if test="${not empty descubrir.busqueda}"><c:param name="busqueda" value="${descubrir.busqueda}" /></c:if></c:url>
                      <article class="discover-result-card${descubrir.seleccionada.comunidad.id eq comunidad.id ? ' active' : ''}">
                        <a class="discover-result-main" href="${urlComunidad}">
                          <span class="discover-result-logo"><c:choose><c:when test="${not empty comunidad.logoUrl}"><img src="<c:out value='${comunidad.logoUrl}' />" alt="" /></c:when><c:otherwise>♜</c:otherwise></c:choose></span>
                          <span class="discover-result-copy"><strong><c:out value="${comunidad.nombre}" /></strong><span class="discover-result-badges"><c:forEach var="juego" items="${comunidad.juegos}"><small><c:out value="${juego}" /></small></c:forEach><small class="privacy-chip"><c:out value="${comunidad.privada ? 'Privada' : 'Pública'}" /></small></span><span><c:out value="${comunidad.descripcion}" /></span></span>
                        </a>
                        <span class="discover-result-stats">♙ <c:out value="${comunidad.totalMiembros}" /> miembros</span>
                        <form action="/comunidades/unirse" method="post"><input type="hidden" name="comunidadId" value="${comunidad.id}" /><input type="hidden" name="origen" value="descubrir" /><button class="button-primary" type="submit" ${comunidad.solicitudPendiente ? 'disabled' : ''}><c:out value="${comunidad.solicitudPendiente ? 'Pendiente' : (comunidad.privada ? 'Solicitar' : 'Unirme')}" /></button></form>
                      </article>
                    </c:forEach>
                  </div>
                </c:otherwise>
              </c:choose>
            </section>

            <aside class="discover-detail-panel">
              <c:choose>
                <c:when test="${descubrir.seleccionada == null}"><div class="event-detail-placeholder"><p>Selecciona una comunidad para consultar sus datos.</p></div></c:when>
                <c:otherwise>
                  <c:set var="comunidad" value="${descubrir.seleccionada.comunidad}" />
                  <div class="discover-detail-heading">
                    <span class="discover-detail-logo"><c:choose><c:when test="${not empty comunidad.logoUrl}"><img src="<c:out value='${comunidad.logoUrl}' />" alt="" /></c:when><c:otherwise>♜</c:otherwise></c:choose></span>
                    <div><h2><c:out value="${comunidad.nombre}" /></h2><div class="discover-result-badges"><c:forEach var="juego" items="${comunidad.juegos}"><small><c:out value="${juego}" /></small></c:forEach><small class="privacy-chip"><c:out value="${comunidad.privada ? 'Privada' : 'Pública'}" /></small></div><p>♙ <c:out value="${comunidad.totalMiembros}" /> miembros · ▣ <c:out value="${comunidad.totalEventos}" /> eventos</p></div>
                  </div>
                  <p class="discover-detail-description"><c:out value="${comunidad.descripcion}" /></p>
                  <section class="discover-upcoming-events">
                    <header><h3>Próximos eventos</h3></header>
                    <c:choose>
                      <c:when test="${empty descubrir.seleccionada.proximosEventos}"><p class="page-subtitle">Esta comunidad no tiene eventos próximos publicados.</p></c:when>
                      <c:otherwise><c:forEach var="evento" items="${descubrir.seleccionada.proximosEventos}"><article><span class="discover-date"><strong><c:out value="${evento.dia}" /></strong><small><c:out value="${evento.mes}" /></small></span><div><strong><c:out value="${evento.titulo}" /></strong><small><c:out value="${evento.fecha}" /> · <c:out value="${evento.formato}" /></small><small>⌖ <c:out value="${evento.ubicacion}" /></small></div><c:if test="${evento.plazasDisponibles != null}"><span class="places-chip">+<c:out value="${evento.plazasDisponibles}" /> plazas</span></c:if></article></c:forEach></c:otherwise>
                    </c:choose>
                  </section>
                  <div class="discover-privacy-note">ⓘ <c:out value="${comunidad.privada ? 'La comunidad requiere aprobación para unirse.' : 'Puedes unirte directamente a esta comunidad.'}" /></div>
                  <form class="discover-primary-action" action="/comunidades/unirse" method="post"><input type="hidden" name="comunidadId" value="${comunidad.id}" /><input type="hidden" name="origen" value="descubrir" /><button class="button-primary" type="submit" ${comunidad.solicitudPendiente ? 'disabled' : ''}><c:out value="${comunidad.solicitudPendiente ? 'Solicitud pendiente' : (comunidad.privada ? 'Solicitar ingreso' : 'Unirme a la comunidad')}" /></button></form>
                </c:otherwise>
              </c:choose>
            </aside>
          </div>
        </main>
      </div>
    </div>
    <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/modalCrearComunidad.jsp" />
    <script src="/js/comunidades-actual.js?v=2"></script>
  </body>
</html>
