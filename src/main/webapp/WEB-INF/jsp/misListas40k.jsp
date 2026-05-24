<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Mis listas 40k</title>
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
            <p class="profile-role"><c:out value="${misListas.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Mis listas</h2>
              <p class="page-subtitle">Consulta las listas guardadas y entra a su detalle.</p>
            </div>

            <c:choose>
              <c:when test="${empty misListas.listas}">
                <div class="note-box">No tienes listas guardadas.</div>
              </c:when>
              <c:otherwise>
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th>Faccion</th>
                      <th>Ejercito</th>
                      <th>Puntos</th>
                      <th>Version</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="lista" items="${misListas.listas}">
                      <c:url var="detalleListaUrl" value="/mi-lista-40k">
                        <c:param name="listaId" value="${lista.listaId}" />
                      </c:url>
                      <tr>
                        <td>
                          <a class="link-inline" href="${detalleListaUrl}">
                            <c:out value="${lista.nombreLista}" />
                          </a>
                        </td>
                        <td><c:out value="${lista.faccion}" /></td>
                        <td><c:out value="${lista.ejercito}" /></td>
                        <td><c:out value="${lista.puntos}" /></td>
                        <td><c:out value="${lista.numeroVersion}" /></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
