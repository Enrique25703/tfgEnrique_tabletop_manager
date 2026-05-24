<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Detalle de lista 40k</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
  </head>
  <body>
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <h1 class="brand-title">TFG</h1>
          <p class="brand-subtitle">Wargame Commander</p>
        </div>
        <nav class="sidebar-nav">
          <a class="sidebar-link" href="/menu-principal">Menu</a>
          <a class="sidebar-link" href="/catalogo40k">Catalogos</a>
          <a class="sidebar-link active" href="/mis-listas-40k">Listas</a>
          <span class="sidebar-link disabled">Partidas</span>
          <span class="sidebar-link disabled">Comunidades</span>
          <span class="sidebar-link disabled">Estadisticas</span>
          <span class="sidebar-link disabled">Ajustes</span>
        </nav>
        <div class="sidebar-footer">TFG Enrique<br />Build academica v1</div>
      </aside>

      <div class="app-main">
        <header class="profile-bar">
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <c:if test="${not empty detalleLista}">
              <div class="page-header">
                <h2 class="page-title"><c:out value="${detalleLista.nombreLista}" /></h2>
                <p class="page-subtitle">
                  <a class="link-inline" href="/mis-listas-40k">Volver a mis listas</a>
                </p>
              </div>

              <div class="chip-row">
                <span class="chip">Faccion: <c:out value="${detalleLista.faccion}" /></span>
                <span class="chip">Ejercito: <c:out value="${detalleLista.ejercito}" /></span>
                <span class="chip">Puntos: <c:out value="${detalleLista.puntos}" /></span>
                <span class="chip">Version: <c:out value="${detalleLista.numeroVersion}" /></span>
              </div>

              <table class="data-table" style="margin-top:18px;">
                <thead>
                  <tr>
                    <th>Unidad</th>
                    <th>Rol</th>
                    <th>Puntos</th>
                    <th>Categoria</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="unidad" items="${detalleLista.unidades}">
                    <c:url var="detalleUnidadUrl" value="/infoUnidad40k">
                      <c:param name="faccion" value="${detalleLista.faccion}" />
                      <c:param name="ejercito" value="${detalleLista.ejercito}" />
                      <c:param name="unidad" value="${unidad.nombreUnidad}" />
                    </c:url>
                    <tr>
                      <td>
                        <a class="link-inline" href="${detalleUnidadUrl}">
                          <c:out value="${unidad.nombreUnidad}" />
                        </a>
                      </td>
                      <td><c:out value="${unidad.roles}" /></td>
                      <td><c:out value="${unidad.puntosBase}" /></td>
                      <td><c:out value="${unidad.categoria}" /></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </c:if>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
