<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Miembros · <c:out value="${comunidadActual.nombre}" /></title>
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
          <div class="profile-card"><p class="profile-title">Mi perfil</p><p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p></div>
        </header>
        <main class="page-content community-view-page">
          <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/cabeceraComunidad.jsp" />
          <c:if test="${not empty mensajeOk}"><div class="note-box community-alert"><c:out value="${mensajeOk}" /></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="error-box community-alert"><c:out value="${mensajeError}" /></div></c:if>
          <section class="viewer-panel">
            <header class="viewer-panel-header"><div><h2>Miembros de la comunidad</h2><p><c:out value="${visor.miembros.size()}" /> miembros activos</p></div></header>
            <div class="form-grid" style="margin-bottom:14px">
              <label>Buscar miembro<input id="buscarMiembro" type="search" placeholder="Nombre de usuario..." /></label>
              <label>Filtrar por rol<select id="filtrarRol"><option value="">Todos los roles</option><option value="administrador">Administradores</option><option value="afiliado">Afiliados</option></select></label>
            </div>
            <div class="viewer-table-wrap">
              <table class="viewer-table">
                <thead><tr><th>Miembro</th><th>Rol</th><th>Miembro desde</th><th>Acciones</th></tr></thead>
                <tbody id="tablaMiembros">
                  <c:forEach var="miembro" items="${visor.miembros}">
                    <tr data-member-row data-member-name="<c:out value='${miembro.nombreUsuario}' />" data-member-role="<c:out value='${miembro.rol}' />">
                      <td><div class="member-cell"><span class="member-avatar"><c:choose><c:when test="${not empty miembro.fotoUrl}"><img src="<c:out value='${miembro.fotoUrl}' />" alt="" /></c:when><c:otherwise>♙</c:otherwise></c:choose></span><span class="member-copy"><strong><c:out value="${miembro.nombreUsuario}" /></strong><c:if test="${miembro.usuarioActual}"><small>Tú</small></c:if></span></div></td>
                      <td><span class="role-badge"><c:out value="${miembro.rol}" /></span></td>
                      <td><c:out value="${miembro.miembroDesde}" /></td>
                      <td><div class="member-actions">
                        <c:if test="${visor.puedeGestionar}"><button class="icon-action manage-member-button" type="button" aria-label="Gestionar miembro" data-user-id="${miembro.usuarioId}" data-user-name="<c:out value='${miembro.nombreUsuario}' />" data-user-photo="<c:out value='${miembro.fotoUrl}' />" data-user-role="<c:out value='${miembro.rol}' />" data-can-promote="${miembro.puedePromover}" data-can-expel="${miembro.puedeExpulsar}">⚙</button></c:if>
                        <c:if test="${not miembro.usuarioActual}"><button class="icon-action duel-member-button" type="button" title="Desafiar a una partida" aria-label="Desafiar a <c:out value='${miembro.nombreUsuario}' />" data-user-id="${miembro.usuarioId}" data-user-name="<c:out value='${miembro.nombreUsuario}' />">⚔</button></c:if>
                      </div></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </section>
        </main>
      </div>
    </div>

    <div class="viewer-modal" id="modalGestionMiembro" hidden aria-hidden="true">
      <div class="viewer-modal-backdrop" data-close-modal></div>
      <section class="viewer-dialog" role="dialog" aria-modal="true" aria-labelledby="tituloGestionMiembro">
        <header><div><h2 id="tituloGestionMiembro">Gestionar miembro</h2><p>Las acciones se aplicarán inmediatamente.</p></div><button class="modal-close" type="button" data-close-modal aria-label="Cerrar">×</button></header>
        <div class="member-modal-body"><div class="member-modal-profile"><span class="member-avatar" id="avatarGestionMiembro">♙</span><div><strong id="nombreGestionMiembro"></strong><p id="rolGestionMiembro" class="page-subtitle"></p></div></div><form id="formPromoverMiembro" method="post"><button class="button-primary" type="submit">Promover a administrador</button></form><form id="formExpulsarMiembro" method="post" onsubmit="return confirm('¿Seguro que quieres expulsar a este miembro?');"><button class="button-secondary danger-action" type="submit">Expulsar de la comunidad</button></form><p id="sinAccionesMiembro" class="page-subtitle" hidden>No hay acciones disponibles para este miembro.</p></div>
      </section>
    </div>

    <div class="viewer-modal" id="modalDuelo" hidden aria-hidden="true">
      <div class="viewer-modal-backdrop" data-close-duel-modal></div>
      <section class="viewer-dialog wide" role="dialog" aria-modal="true" aria-labelledby="tituloDuelo">
        <header><div><h2 id="tituloDuelo">Proponer duelo</h2><p>El oponente recibirá una invitación en sus notificaciones.</p></div><button class="modal-close" type="button" data-close-duel-modal aria-label="Cerrar">×</button></header>
        <form class="viewer-form" id="formDuelo" method="post">
          <input id="latitudDuelo" name="latitud" type="hidden" />
          <input id="longitudDuelo" name="longitud" type="hidden" />
          <p class="page-subtitle">Oponente: <strong id="oponenteDuelo"></strong></p>
          <div class="form-grid"><label>Fecha y hora<input name="fecha" type="datetime-local" required /></label><label>Formato de juego<select name="formatoJuego" required><option value="WH40K_11">Warhammer 40.000</option><option value="AOS_4">Age of Sigmar</option></select></label></div>
          <label>Buscar ubicación<div class="map-search-row"><input id="buscarUbicacionDuelo" type="search" placeholder="Dirección o lugar" /><button class="button-secondary" id="botonBuscarUbicacionDuelo" type="button">Buscar</button></div></label>
          <label>Ubicación seleccionada<input id="lugarDuelo" name="lugar" maxlength="150" readonly required /></label>
          <div id="mapaDuelo" class="event-map-picker" aria-label="Mapa para seleccionar la ubicación del duelo"></div>
          <p id="estadoMapaDuelo" class="page-subtitle">Busca una dirección o pulsa sobre el mapa.</p>
          <label>Mensaje opcional<textarea name="mensaje" rows="3" maxlength="255"></textarea></label>
          <div class="modal-actions"><button class="button-primary" type="submit">Enviar desafío</button><button class="button-secondary" type="button" data-close-duel-modal>Cancelar</button></div>
        </form>
      </section>
    </div>

    <script>window.COMUNIDAD_ID=<c:out value="${comunidadActual.id}" />;</script>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
    <script src="/js/comunidad-miembros.js?v=3"></script>
  </body>
</html>
