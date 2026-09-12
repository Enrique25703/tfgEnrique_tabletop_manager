<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section class="community-hero">
  <div class="community-identity">
    <div class="community-logo-shell">
      <c:choose>
        <c:when test="${not empty comunidadActual.logoUrl}"><img src="<c:out value='${comunidadActual.logoUrl}'/>" alt="Logo de <c:out value='${comunidadActual.nombre}'/>" /></c:when>
        <c:otherwise><span aria-hidden="true">♜</span></c:otherwise>
      </c:choose>
    </div>
    <div class="community-identity-copy">
      <div class="community-name-row"><h1><c:out value="${comunidadActual.nombre}" /></h1><c:if test="${comunidadActual.administrador}"><span class="admin-badge">♛ Administrador</span></c:if></div>
      <div class="community-meta">
        <span>♙ <c:out value="${comunidadActual.totalMiembros}" /> miembros</span>
        <span>▣ Creada el <c:out value="${comunidadActual.creadaEn}" /></span>
        <span>◎ <c:out value="${comunidadActual.privacidad}" /></span>
      </div>
      <p class="community-description"><c:out value="${comunidadActual.descripcion}" /></p>
    </div>
    <div class="community-header-actions">
      <a class="button-secondary back-communities" href="/comunidades">← Volver a comunidades</a>
      <form method="post" action="/comunidades/${comunidadActual.id}/abandonar" onsubmit="return confirm('¿Seguro que quieres abandonar esta comunidad?');">
        <button class="leave-community-button" type="submit">Abandonar comunidad</button>
      </form>
    </div>
  </div>
  <nav class="viewer-tabs" aria-label="Secciones de la comunidad">
    <a class="viewer-tab${pestanaActiva eq 'miembros' ? ' active' : ''}" href="/comunidades/${comunidadActual.id}/miembros">♙ Miembros</a>
    <c:if test="${comunidadActual.administrador}">
      <a class="viewer-tab${pestanaActiva eq 'solicitudes' ? ' active' : ''}" href="/comunidades/${comunidadActual.id}/solicitudes">♙ Solicitudes<c:if test="${comunidadActual.totalSolicitudesPendientes gt 0}"><span><c:out value="${comunidadActual.totalSolicitudesPendientes}" /></span></c:if></a>
    </c:if>
    <a class="viewer-tab${pestanaActiva eq 'eventos' ? ' active' : ''}" href="/comunidades/${comunidadActual.id}/eventos">▣ Eventos</a>
    <c:if test="${comunidadActual.administrador}">
      <a class="viewer-tab${pestanaActiva eq 'ajustes' ? ' active' : ''}" href="/comunidades/${comunidadActual.id}/ajustes">⚙ Ajustes</a>
    </c:if>
  </nav>
</section>
