<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Info unidad AoS</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .two-column {
        display: grid;
        grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
        gap: 18px;
      }

      .sub-panel {
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-muted);
      }

      .sub-panel h3 {
        margin-top: 0;
      }

      .text-block {
        white-space: pre-line;
        color: var(--muted);
      }

      @media (max-width: 980px) {
        .two-column {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body>
    <c:url var="volverCatalogoUrl" value="/catalogo-aos">
      <c:param name="faccion" value="${infoUnidadAos.faccionSeleccionada}" />
      <c:param name="ejercito" value="${infoUnidadAos.ejercitoSeleccionado}" />
    </c:url>

    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <h1 class="brand-title">TFG</h1>
          <p class="brand-subtitle">Wargame Commander</p>
        </div>
        <nav class="sidebar-nav">
          <a class="sidebar-link" href="/menu-principal">Menu</a>
          <a class="sidebar-link active" href="/catalogos">Catalogos</a>
          <a class="sidebar-link" href="/mis-listas-40k">Listas</a>
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
            <div class="page-header">
              <h2 class="page-title"><c:out value="${infoUnidadAos.nombreUnidad}" /></h2>
              <p class="page-subtitle">
                <a class="link-inline" href="${volverCatalogoUrl}">Volver al catalogo</a>
                ·
                <c:out value="${infoUnidadAos.faccionSeleccionada}" />
                -
                <c:out value="${infoUnidadAos.ejercitoSeleccionado}" />
              </p>
            </div>

            <div class="two-column">
              <div class="sub-panel">
                <h3>Perfil de la unidad</h3>
                <table class="data-table">
                  <tbody>
                    <c:forEach var="estadistica" items="${infoUnidadAos.estadisticas}">
                      <tr>
                        <th><c:out value="${estadistica.nombre}" /></th>
                        <td><c:out value="${estadistica.valor}" /></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>

              <div class="sub-panel">
                <h3>Equipo y armas</h3>
                <p class="card-kicker">Equipamiento</p>
                <p class="text-block"><c:out value="${infoUnidadAos.perfiles}" /></p>
                <p class="card-kicker" style="margin-top:18px;">Armas</p>
                <p class="text-block"><c:out value="${infoUnidadAos.armas}" /></p>
              </div>
            </div>

            <div class="sub-panel" style="margin-top:18px;">
              <h3>Habilidades</h3>
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Habilidad</th>
                    <th>Explicacion</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="habilidad" items="${infoUnidadAos.habilidades}">
                    <tr>
                      <td><c:out value="${habilidad.nombre}" /></td>
                      <td><c:out value="${habilidad.descripcion}" /></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
