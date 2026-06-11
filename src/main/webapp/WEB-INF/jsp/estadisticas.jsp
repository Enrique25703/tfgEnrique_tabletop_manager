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
      .stats-layout {
        display: grid;
        gap: 18px;
      }

      .stats-grid {
        display: grid;
        grid-template-columns: 1.2fr 0.9fr;
        gap: 18px;
        align-items: start;
      }

      .stats-card,
      .summary-card,
      .legend-card {
        padding: 20px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
      }

      .summary-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 14px;
      }

      .summary-value {
        margin: 0 0 6px;
        font-size: 1.8rem;
        font-weight: 700;
      }

      .summary-label {
        margin: 0;
        color: var(--muted);
      }

      .chart-shell {
        margin-top: 16px;
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: rgba(14, 22, 34, 0.55);
      }

      .timeline-meta {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        flex-wrap: wrap;
        margin-top: 12px;
        color: var(--muted);
        font-size: 0.92rem;
      }

      .timeline-svg {
        width: 100%;
        height: auto;
        display: block;
      }

      .axis-text {
        fill: #90a6c4;
        font-size: 4px;
      }

      .grid-line {
        stroke: rgba(144, 166, 196, 0.22);
        stroke-width: 0.35;
      }

      .timeline-line {
        fill: none;
        stroke: #5b8def;
        stroke-width: 1.2;
        stroke-linecap: round;
        stroke-linejoin: round;
      }

      .timeline-point {
        fill: #dce8ff;
        stroke: #5b8def;
        stroke-width: 0.65;
      }

      .distribution-shell {
        display: grid;
        gap: 18px;
        justify-items: center;
      }

      .donut {
        width: min(320px, 78vw);
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
        font-size: 2.6rem;
        font-weight: 700;
      }

      .donut-label {
        margin: 6px 0 0;
        color: var(--muted);
      }

      .selector-row {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        justify-content: center;
      }

      .selector-button {
        min-width: 180px;
      }

      .selector-button.passive {
        opacity: 0.78;
      }

      .legend-list {
        display: grid;
        gap: 10px;
        margin-top: 14px;
      }

      .legend-item {
        display: grid;
        grid-template-columns: auto 1fr auto;
        gap: 10px;
        align-items: center;
        padding: 10px 12px;
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
        font-size: 0.92rem;
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
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="estadisticas" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
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
                  <p class="summary-value"><c:out value="${estadisticas.evolucionVictorias.porcentajeVictorias}" /></p>
                  <p class="summary-label">Porcentaje de victorias</p>
                </article>
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.evolucionVictorias.victorias}" /></p>
                  <p class="summary-label">Victorias</p>
                </article>
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.evolucionVictorias.derrotas}" /></p>
                  <p class="summary-label">Derrotas</p>
                </article>
                <article class="summary-card">
                  <p class="summary-value"><c:out value="${estadisticas.evolucionVictorias.empates}" /></p>
                  <p class="summary-label">Empates</p>
                </article>
              </div>

              <div class="stats-grid">
                <article class="stats-card">
                  <h3>Evolucion de victorias</h3>
                  <p class="page-subtitle">Porcentaje acumulado de victorias desde la creacion de la cuenta hasta la ultima partida finalizada.</p>

                  <div class="chart-shell">
                    <c:choose>
                      <c:when test="${estadisticas.evolucionVictorias.tieneDatos}">
                        <svg class="timeline-svg" viewBox="0 0 100 100" aria-label="Grafica de evolucion de victorias">
                          <line class="grid-line" x1="0" y1="100" x2="100" y2="100"></line>
                          <line class="grid-line" x1="0" y1="50" x2="100" y2="50"></line>
                          <line class="grid-line" x1="0" y1="0" x2="100" y2="0"></line>
                          <text class="axis-text" x="0" y="97">0%</text>
                          <text class="axis-text" x="0" y="47">50%</text>
                          <text class="axis-text" x="0" y="6">100%</text>
                          <polyline class="timeline-line" points="${estadisticas.evolucionVictorias.polylinePoints}"></polyline>
                          <c:forEach var="punto" items="${estadisticas.evolucionVictorias.puntos}">
                            <circle class="timeline-point" cx="${punto.x}" cy="${punto.y}" r="1.5">
                              <title><c:out value="${punto.fecha}" /> - <c:out value="${punto.porcentaje}" /> (<c:out value="${punto.victorias}" />/<c:out value="${punto.partidas}" />)</title>
                            </circle>
                          </c:forEach>
                        </svg>
                        <div class="timeline-meta">
                          <span>Inicio: <c:out value="${estadisticas.evolucionVictorias.fechaInicio}" /></span>
                          <span>Ultima partida: <c:out value="${estadisticas.evolucionVictorias.fechaFin}" /></span>
                          <span>Partidas finalizadas: <c:out value="${estadisticas.evolucionVictorias.partidasFinalizadas}" /></span>
                        </div>
                      </c:when>
                      <c:otherwise>
                        <div class="empty-box">Todavia no hay partidas finalizadas para construir la evolucion de victorias.</div>
                      </c:otherwise>
                    </c:choose>
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
