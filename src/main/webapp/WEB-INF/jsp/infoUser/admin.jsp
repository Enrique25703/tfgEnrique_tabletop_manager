<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Administración · Tabletop Manager</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/admin.css" />
    <script src="/js/admin.js" defer></script>
  </head>
  <body class="admin-screen">
    <svg class="admin-symbols" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <symbol id="admin-users" viewBox="0 0 24 24"><circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6m2 3a5 5 0 0 1 3 5v2"/></symbol>
      <symbol id="admin-community" viewBox="0 0 24 24"><path d="m12 3 9 5v9l-9 5-9-5V8Zm0 10v9M3 8l9 5 9-5M7.5 5.5l9 5v5"/></symbol>
      <symbol id="admin-shield" viewBox="0 0 24 24"><path d="m12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6Zm-4 9 3 3 5-6"/></symbol>
      <symbol id="admin-search" viewBox="0 0 24 24"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/></symbol>
      <symbol id="admin-arrow" viewBox="0 0 24 24"><path d="M5 12h14m-6-6 6 6-6 6"/></symbol>
      <symbol id="admin-close" viewBox="0 0 24 24"><path d="m6 6 12 12M6 18 18 6"/></symbol>
    </svg>
    <c:set var="sidebarActive" value="admin" scope="request" />
    <c:set var="sidebarComunidadesEnabled" value="true" scope="request" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />
      <div class="app-main">
        <header class="profile-bar admin-topbar">
          <span class="admin-breadcrumb">Tabletop Manager <span>/</span> <strong>Administración</strong></span>
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="admin-account">
            <span class="avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(admin.nombreAdmin, 0, 1))}" /></span>
            <div><strong><c:out value="${admin.nombreAdmin}" /></strong><span>Administrador</span></div>
          </div>
        </header>
        <main class="admin-content" id="administracion">
          <div class="admin-heading">
            <div><p class="eyebrow">Gestión de la plataforma</p><h1>Todo bajo control.</h1><p class="admin-intro">Un espacio para cuidar de tus usuarios y comunidades.</p></div>
            <span class="admin-access"><svg class="admin-icon" aria-hidden="true"><use href="#admin-shield" /></svg> Acceso de administrador</span>
          </div>
          <c:if test="${not empty mensajeOk}"><div class="admin-alert alert-success" role="status"><svg class="admin-icon" aria-hidden="true"><use href="#admin-shield" /></svg><span><c:out value="${mensajeOk}" /></span></div></c:if>
          <c:if test="${not empty mensajeError}"><div class="admin-alert alert-error" role="alert"><strong>No se pudo completar la acción.</strong><span><c:out value="${mensajeError}" /></span></div></c:if>
          <nav class="admin-summary" aria-label="Resumen y accesos a la administración">
            <a class="summary-card" href="#usuarios"><span class="summary-top"><span>Usuarios activos</span><svg class="admin-icon" aria-hidden="true"><use href="#admin-users" /></svg></span><strong>${fn:length(admin.usuarios)}</strong><span class="summary-bottom"><span>${empty admin.buscarUsuario ? 'Cuentas de la plataforma' : 'Coinciden con tu búsqueda'}</span><svg class="admin-icon" aria-hidden="true"><use href="#admin-arrow" /></svg></span></a>
            <a class="summary-card summary-community" href="#comunidades"><span class="summary-top"><span>Comunidades activas</span><svg class="admin-icon" aria-hidden="true"><use href="#admin-community" /></svg></span><strong>${fn:length(admin.comunidades)}</strong><span class="summary-bottom"><span>${empty admin.buscarComunidad ? 'Grupos de la plataforma' : 'Coinciden con tu búsqueda'}</span><svg class="admin-icon" aria-hidden="true"><use href="#admin-arrow" /></svg></span></a>
            <a class="summary-card summary-members" href="#miembros"><span class="summary-top"><span>Miembros de la selección</span><svg class="admin-icon" aria-hidden="true"><use href="#admin-shield" /></svg></span><strong>${admin.comunidadSeleccionada == null ? '—' : fn:length(admin.comunidadSeleccionada.miembros)}</strong><span class="summary-bottom"><span><c:out value="${admin.comunidadSeleccionada == null ? 'Selecciona una comunidad' : admin.comunidadSeleccionada.nombre}" /></span><svg class="admin-icon" aria-hidden="true"><use href="#admin-arrow" /></svg></span></a>
          </nav>
          <%@ include file="admin/usuarios.jsp" %>
          <div class="admin-community-layout">
            <%@ include file="admin/comunidades.jsp" %>
            <%@ include file="admin/miembros.jsp" %>
          </div>
          <footer class="admin-footer"><span>Tabletop Manager</span><span>Administración de usuarios y comunidades</span></footer>
        </main>
      </div>
    </div>
    <%@ include file="admin/modales.jsp" %>
  </body>
</html>
