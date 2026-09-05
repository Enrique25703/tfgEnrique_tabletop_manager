<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Mis comunidades</title>
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
          <div class="profile-card"><p class="profile-title">Mi perfil</p><p class="profile-role"><c:out value="${misComunidades.nombreUsuario}" /></p></div>
        </header>
        <main class="page-content communities-page">
          <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/cabeceraListadoComunidades.jsp" />
          <c:if test="${not empty mensajeOk}"><div class="note-box community-alert"><c:out value="${mensajeOk}" /></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="error-box community-alert"><c:out value="${mensajeError}" /></div></c:if>

          <section class="owned-communities-panel">
            <header class="owned-communities-heading">
              <div><h2>Mis comunidades</h2><p>Comunidades de las que formas parte actualmente.</p></div>
              <span class="count-badge"><c:out value="${misComunidades.totalComunidades}" /></span>
            </header>
            <label class="community-search-field" for="buscarMisComunidades"><span aria-hidden="true">⌕</span><input id="buscarMisComunidades" data-community-filter type="search" placeholder="Buscar entre mis comunidades..." /></label>

            <c:choose>
              <c:when test="${empty misComunidades.comunidades}">
                <div class="events-empty compact-owned-empty"><h3>Todavía no formas parte de ninguna comunidad</h3><p>Visita Descubrir comunidades para encontrar una.</p><a class="button-primary" href="/comunidades/descubrir">Descubrir comunidades</a></div>
              </c:when>
              <c:otherwise>
                <div class="owned-community-list">
                  <c:forEach var="comunidad" items="${misComunidades.comunidades}">
                    <a class="owned-community-card" data-community-row data-community-name="<c:out value='${comunidad.nombre}' /> <c:out value='${comunidad.descripcion}' />" href="/comunidades/${comunidad.id}">
                      <span class="owned-community-logo">
                        <c:choose><c:when test="${not empty comunidad.logoUrl}"><img src="<c:out value='${comunidad.logoUrl}' />" alt="" /></c:when><c:otherwise>♜</c:otherwise></c:choose>
                      </span>
                      <span class="owned-community-copy">
                        <span class="owned-community-title"><strong><c:out value="${comunidad.nombre}" /></strong><span class="role-badge-small"><c:out value="${comunidad.rolUsuario}" /></span></span>
                        <span class="owned-community-description"><c:out value="${comunidad.descripcion}" /></span>
                        <span class="owned-community-meta"><span>♙ <c:out value="${comunidad.totalMiembros}" /> miembros</span><span>▣ <c:out value="${comunidad.totalEventos}" /> eventos</span><span>◎ <c:out value="${comunidad.privada ? 'Privada' : 'Pública'}" /></span></span>
                      </span>
                      <span class="owned-community-enter">Abrir comunidad <span aria-hidden="true">→</span></span>
                    </a>
                  </c:forEach>
                </div>
                <p class="filter-empty-message" data-community-filter-empty hidden>No hay comunidades que coincidan con la búsqueda.</p>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>
    <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/modalCrearComunidad.jsp" />
    <script src="/js/comunidades-actual.js?v=2"></script>
    <script src="/js/comunidades-listado.js?v=1"></script>
  </body>
</html>
