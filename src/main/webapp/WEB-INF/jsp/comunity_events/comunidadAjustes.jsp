<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Ajustes · <c:out value="${comunidadActual.nombre}" /></title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/comunidad-visor.css?v=2" />
    <link rel="stylesheet" href="/css/comunidad-ajustes.css?v=1" />
  </head>
  <body>
    <c:set var="sidebarActive" value="comunidades" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />
      <div class="app-main">
        <header class="profile-bar"><jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" /><div class="profile-card"><p class="profile-title">Mi perfil</p><p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p></div></header>
        <main class="page-content community-view-page">
          <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/cabeceraComunidad.jsp" />
          <c:if test="${not empty mensajeOk}"><div class="note-box community-alert"><c:out value="${mensajeOk}" /></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="error-box community-alert"><c:out value="${mensajeError}" /></div></c:if>
          <section class="viewer-panel community-settings-panel">
            <header class="viewer-panel-header"><div><h2>Ajustes de la comunidad</h2><p>Configura quién puede acceder y solicitar unirse.</p></div></header>
            <form class="viewer-form community-privacy-form" method="post" action="/comunidades/${comunidadActual.id}/ajustes/privacidad">
              <fieldset class="privacy-options">
                <legend>Privacidad</legend>
                <label class="privacy-option"><input type="radio" name="privacidad" value="PUBLICA" ${comunidadActual.privacidad eq 'Pública' ? 'checked' : ''} required /><span><strong>Pública</strong><small>Cualquier usuario puede unirse directamente.</small></span></label>
                <label class="privacy-option"><input type="radio" name="privacidad" value="PRIVADA" ${comunidadActual.privacidad eq 'Privada' ? 'checked' : ''} required /><span><strong>Privada</strong><small>Los administradores deben aprobar cada solicitud.</small></span></label>
              </fieldset>
              <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/selectorImagenComunidad.jsp">
                <jsp:param name="selectorId" value="editar" />
                <jsp:param name="logoActual" value="${comunidadActual.logoUrl}" />
              </jsp:include>
              <div class="modal-actions"><button class="button-primary" type="submit">Guardar cambios</button></div>
            </form>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
