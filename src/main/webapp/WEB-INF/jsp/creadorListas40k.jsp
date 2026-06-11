<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Creador de listas 40k</title>
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
        gap: 14px;
      }

      #notasUnidad {
        min-height: 160px;
      }

      .config-card {
        padding: 14px;
        border: 1px solid rgba(130, 173, 255, 0.18);
        border-radius: 14px;
        background: rgba(12, 18, 28, 0.72);
      }

      .config-card-title,
      .choice-title,
      .instance-title,
      .gear-title,
      .model-name {
        margin: 0;
        font-weight: 700;
      }

      .config-group.nivel-1,
      .config-group.nivel-2,
      .config-group.nivel-3 {
        margin-left: 10px;
      }

      .group-header,
      .model-header,
      .choice-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
      }

      .group-count,
      .choice-count,
      .count-value {
        padding: 7px 12px;
        border: 1px solid var(--line);
        border-radius: 12px;
        background: rgba(20, 29, 44, 0.94);
        color: var(--text);
        font-weight: 700;
      }

      .model-card,
      .instance-card,
      .choice-card,
      .gear-list {
        margin-top: 12px;
        padding: 12px;
        border: 1px solid rgba(130, 173, 255, 0.14);
        border-radius: 12px;
        background: rgba(15, 23, 35, 0.78);
      }

      .model-info,
      .model-detail {
        display: grid;
        gap: 8px;
      }

      .model-range {
        margin: 4px 0 0;
        color: var(--muted);
        font-size: 0.9rem;
      }

      .count-controls {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .count-button {
        min-width: 38px;
        min-height: 38px;
        padding: 0;
      }

      .choice-row,
      .gear-item {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        margin-top: 10px;
      }

      .gear-items {
        display: grid;
        gap: 8px;
      }

      @media (max-width: 1200px) {
        .builder-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="listas" />
    <c:set var="sidebarComunidadesEnabled" value="false" />
    <div class="app-shell">
      <jsp:include page="header.jsp" />

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
      data-formato-juego="<c:out value='${creadorLista.formatoJuego}'/>"
      data-nombre-lista="<c:out value='${creadorLista.nombreLista}'/>"
      data-faccion="<c:out value='${creadorLista.faccion}'/>"
      data-ejercito="<c:out value='${creadorLista.ejercito}'/>"
      data-limite-puntos="<c:out value='${creadorLista.limitePuntos}'/>"
      data-url-guardado="/creador-listas-40k/guardar">

            <section class="page-panel">
              <div class="page-header">
                <h2 class="page-title">
                  <c:out value="${creadorLista.nombreLista}" />
                  ·
                  <span id="contadorPuntos">0</span> pts
                </h2>
                <p class="page-subtitle">
                  Formato: <span id="formatoJuegoTexto"><c:out value="${creadorLista.formatoJuego}" /></span>
                  · Faccion: <c:out value="${creadorLista.faccion}" />
                  · Ejercito: <c:out value="${creadorLista.ejercito}" />
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
                  <c:forEach var="categoria" items="${creadorLista.categorias}">
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
                          data-armas="<c:out value='${unidad.armas}'/>"
                          data-configuracion-json="<c:out value='${unidad.configuracionJson}'/>">
                          <c:out value="${unidad.nombre}" /> (<c:out value="${unidad.puntos}" /> pts)
                        </button>
                      </c:forEach>
                    </div>
                  </c:forEach>
                </section>

                <section class="builder-column">
                  <h3>Lista</h3>
                  <c:forEach var="categoria" items="${creadorLista.categorias}">
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
