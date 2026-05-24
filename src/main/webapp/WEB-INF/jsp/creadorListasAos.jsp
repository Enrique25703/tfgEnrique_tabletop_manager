<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Creador de listas AoS</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .builder-grid {
        display: grid;
        grid-template-columns: 0.95fr 1fr 0.95fr;
        gap: 18px;
      }

      .builder-column {
        min-width: 0;
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-muted);
      }

      .builder-column h3,
      .builder-column h4 {
        margin-top: 0;
      }

      .catalog-button {
        display: block;
        width: 100%;
        margin-bottom: 10px;
        padding: 12px;
        text-align: left;
      }

      #configuracionUnidad {
        display: grid;
        gap: 12px;
      }

      #notasUnidad {
        min-height: 160px;
      }

      @media (max-width: 1200px) {
        .builder-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
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
          <a class="sidebar-link" href="/catalogo-aos">Catalogos</a>
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
          <div
            id="creadorListaApp"
            data-formato-juego="<c:out value='${creadorListaAos.formatoJuego}'/>"
            data-nombre-lista="<c:out value='${creadorListaAos.nombreLista}'/>"
            data-faccion="<c:out value='${creadorListaAos.faccion}'/>"
            data-ejercito="<c:out value='${creadorListaAos.ejercito}'/>"
            data-limite-puntos="<c:out value='${creadorListaAos.limitePuntos}'/>"
            data-url-guardado="/creador-listas-aos/guardar">

            <section class="page-panel">
              <div class="page-header">
                <h2 class="page-title">
                  <c:out value="${creadorListaAos.nombreLista}" />
                  ·
                  <span id="contadorPuntos">0</span> pts
                </h2>
                <p class="page-subtitle">
                  Formato: <span id="formatoJuegoTexto"><c:out value="${creadorListaAos.formatoJuego}" /></span>
                  · Faccion: <c:out value="${creadorListaAos.faccion}" />
                  · Ejercito: <c:out value="${creadorListaAos.ejercito}" />
                </p>
              </div>

              <div class="chip-row">
                <a class="button-link" href="/menu-principal">Volver al menu</a>
                <button type="button" class="button-primary" id="botonGuardarLista">Guardar lista</button>
                <span id="estadoGuardado" class="chip"></span>
              </div>

              <div class="builder-grid" style="margin-top:22px;">
                <section class="builder-column">
                  <h3>Catalogo</h3>
                  <c:forEach var="categoria" items="${creadorListaAos.categorias}">
                    <h4><c:out value="${categoria.titulo}" /></h4>
                    <div id="catalogo-${categoria.id}">
                      <c:forEach var="unidad" items="${categoria.unidades}">
                        <button
                          type="button"
                          class="boton-catalogo-unidad catalog-button"
                          data-nombre="<c:out value='${unidad.nombre}'/>"
                          data-roles="<c:out value='${unidad.roles}'/>"
                          data-puntos="<c:out value='${unidad.puntos}'/>"
                          data-puntos-base="<c:out value='${unidad.puntosBase}'/>"
                          data-categoria="<c:out value='${unidad.categoria}'/>"
                          data-configuracion-json="<c:out value='${unidad.configuracionJson}'/>">
                          <c:out value="${unidad.nombre}" /> (<c:out value="${unidad.puntos}" /> pts)
                        </button>
                      </c:forEach>
                    </div>
                  </c:forEach>
                </section>

                <section class="builder-column">
                  <h3>Lista</h3>
                  <c:forEach var="categoria" items="${creadorListaAos.categorias}">
                    <h4><c:out value="${categoria.titulo}" /></h4>
                    <div id="bloque-${categoria.id}">Vacio</div>
                  </c:forEach>
                </section>

                <section class="builder-column">
                  <h3>Unidad seleccionada</h3>
                  <div id="panelVacio">Selecciona una unidad de la lista.</div>

                  <div id="panelDetalle" style="display:none;">
                    <p><strong id="detalleNombre"></strong></p>
                    <p id="detalleMeta" class="page-subtitle"></p>

                    <div id="configuracionUnidad"></div>

                    <p style="margin-top:18px;">Notas</p>
                    <textarea id="notasUnidad"></textarea>
                  </div>
                </section>
              </div>
            </section>
          </div>
        </main>
      </div>
    </div>

    <script src="/js/creador-listas-40k.js"></script>
  </body>
</html>
