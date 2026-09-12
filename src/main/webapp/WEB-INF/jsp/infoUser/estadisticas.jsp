<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Estadisticas</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .stats-screen .page-panel {
        display: flex;
        flex-direction: column;
        overflow: auto;
      }

      .stats-layout {
        display: grid;
        flex: 0 0 auto;
        min-height: 0;
        gap: 12px;
      }

      .stats-grid {
        display: grid;
        min-height: 0;
        grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
        gap: 12px;
        align-items: stretch;
      }

      .stats-card,
      .summary-card,
      .legend-card {
        padding: 14px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
      }

      .stats-card,
      .legend-card {
        min-height: 0;
        overflow: hidden;
      }

      .summary-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 10px;
      }

      .summary-value {
        margin: 0 0 4px;
        font-size: 1.45rem;
        font-weight: 700;
      }

      .summary-label {
        margin: 0;
        color: var(--muted);
      }

      .chart-shell {
        margin-top: 10px;
        padding: 12px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: rgba(14, 22, 34, 0.55);
      }

      .results-pie {
        width: min(220px, 100%);
        aspect-ratio: 1;
        border-radius: 50%;
        margin: 0 auto;
      }

      .results-legend {
        display: grid;
        gap: 8px;
        margin: 16px 0 0;
        padding: 0;
        list-style: none;
      }

      .distribution-shell {
        display: grid;
        gap: 12px;
        justify-items: center;
      }

      .donut {
        width: min(240px, 60vw);
        aspect-ratio: 1;
        border-radius: 50%;
        background: #233246;
        display: grid;
        place-items: center;
        position: relative;
      }

      .donut::after {
        content: "";
        position: absolute;
        inset: 17%;
        border-radius: 50%;
        background: rgba(10, 16, 25, 0.96);
        border: 1px solid rgba(144, 166, 196, 0.14);
      }

      .donut-center {
        position: relative;
        z-index: 1;
        text-align: center;
      }

      .donut-total {
        margin: 0;
        font-size: 2rem;
        font-weight: 700;
      }

      .donut-label {
        margin: 6px 0 0;
        color: var(--muted);
      }

      .selector-row {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        justify-content: center;
      }

      .selector-button {
        min-width: 140px;
        padding: 9px 12px;
        font-size: 0.88rem;
      }

      .selector-button.passive {
        opacity: 0.78;
      }

      .legend-list {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 8px;
        margin-top: 10px;
      }

      .legend-item {
        display: grid;
        grid-template-columns: auto 1fr auto;
        gap: 8px;
        align-items: center;
        padding: 8px 10px;
        border: 1px solid var(--line);
        border-radius: 14px;
        background: rgba(14, 22, 34, 0.55);
      }

      .legend-color {
        width: 14px;
        height: 14px;
        border-radius: 999px;
      }

      .legend-copy {
        display: grid;
        gap: 2px;
      }

      .legend-label {
        font-weight: 600;
      }

      .legend-subcopy {
        color: var(--muted);
        font-size: 0.84rem;
      }

      .empty-box {
        padding: 18px;
        border: 1px dashed var(--line);
        border-radius: 16px;
        color: var(--muted);
        background: rgba(12, 18, 28, 0.6);
      }

      @media (max-width: 1080px) {
        .stats-grid,
        .summary-grid {
          grid-template-columns: 1fr;
        }

        .legend-list {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body class="stats-screen">
    <c:set var="sidebarActive" value="estadisticas" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${estadisticas.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Estadisticas</h2>
              <p class="page-subtitle">Resumen de victorias y distribucion de partidas por juego y faccion.</p>
            </div>

            <div class="stats-layout">
              <div class="summary-grid">
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.resultados.porcentajeVictorias}" /></p>
                  <p class="summary-label">Porcentaje de victorias</p>
                </article>
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.resultados.victorias}" /></p>
                  <p class="summary-label">Victorias</p>
                </article>
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.resultados.derrotas}" /></p>
                  <p class="summary-label">Derrotas</p>
                </article>
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.resultados.empates}" /></p>
                  <p class="summary-label">Empates</p>
                </article>
              </div>

              <div class="stats-grid">
                <article class="stats-card">
                  <h3>Resultados de partidas</h3>
                  <div class="chart-shell">
                    <c:choose>
                      <c:when test="${estadisticas.resultados.tieneDatos}">
                        <div class="results-pie" style="background: ${estadisticas.resultados.graficaCss};" role="img" aria-label="Porcentaje de victorias, empates y derrotas" aria-describedby="leyendaResultados"></div>
                      </c:when>
                      <c:otherwise>
                        <div class="empty-box">Todavía no hay partidas finalizadas.</div>
                      </c:otherwise>
                    </c:choose>
                    <ul class="results-legend" id="leyendaResultados">
                      <c:forEach var="resultado" items="${estadisticas.resultados.segmentos}">
                        <li class="legend-item">
                          <span class="legend-color" style="background: ${resultado.color};" aria-hidden="true"></span>
                          <span class="legend-label"><c:out value="${resultado.etiqueta}" /></span>
                          <strong><c:out value="${resultado.porcentaje}" /></strong>
                        </li>
                      </c:forEach>
                    </ul>
                  </div>
                </article>

                <article class="stats-card">
                  <h3>Distribucion por juego</h3>
                  <p class="page-subtitle"><c:out value="${estadisticas.distribucionSeleccionada.descripcion}" /></p>

                  <div class="distribution-shell">
                    <div class="donut" style="background: ${estadisticas.distribucionSeleccionada.graficaCss};">
                      <div class="donut-center">
                        <p class="donut-total"><c:out value="${estadisticas.distribucionSeleccionada.totalPartidas}" /></p>
                        <p class="donut-label">partidas</p>
                      </div>
                    </div>

                    <div class="selector-row">
                      <c:forEach var="juego" items="${estadisticas.juegos}">
                        <c:choose>
                          <c:when test="${juego.seleccionado}">
                            <a class="button-primary selector-button" href="/estadisticas?juego=${juego.codigo}">
                              <c:out value="${juego.nombre}" /> (<c:out value="${juego.totalPartidas}" />)
                            </a>
                          </c:when>
                          <c:otherwise>
                            <a class="button-secondary selector-button passive" href="/estadisticas?juego=${juego.codigo}">
                              <c:out value="${juego.nombre}" /> (<c:out value="${juego.totalPartidas}" />)
                            </a>
                          </c:otherwise>
                        </c:choose>
                      </c:forEach>
                    </div>
                  </div>
                </article>
              </div>

              <article class="legend-card">
                <h3>Detalle de la distribucion</h3>
                <c:choose>
                  <c:when test="${empty estadisticas.distribucionSeleccionada.segmentos}">
                    <div class="empty-box">No hay partidas registradas para este juego todavia.</div>
                  </c:when>
                  <c:otherwise>
                    <div class="legend-list">
                      <c:forEach var="segmento" items="${estadisticas.distribucionSeleccionada.segmentos}">
                        <div class="legend-item">
                          <span class="legend-color" style="background:${segmento.color};"></span>
                          <div class="legend-copy">
                            <span class="legend-label"><c:out value="${segmento.etiqueta}" /></span>
                            <span class="legend-subcopy"><c:out value="${segmento.totalPartidas}" /> partidas</span>
                          </div>
                          <strong><c:out value="${segmento.porcentaje}" /></strong>
                        </div>
                      </c:forEach>
                    </div>
                  </c:otherwise>
                </c:choose>
              </article>
            </div>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
