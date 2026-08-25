<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Resultado final</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .result-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 16px;
        margin-bottom: 20px;
      }

      .result-card {
        padding: 20px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-soft);
      }

      .result-value {
        margin: 0;
        font-size: 2.3rem;
        font-weight: 800;
      }

      table {
        width: 100%;
        border-collapse: collapse;
        overflow: hidden;
        border-radius: 16px;
      }

      th,
      td {
        padding: 12px;
        border-bottom: 1px solid var(--line);
        text-align: left;
      }

      th {
        color: var(--muted);
        background: rgba(14, 22, 34, 0.86);
      }

      @media (max-width: 900px) {
        .result-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="partidas" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <c:set var="partida" value="${finalPartida.partida}" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Resultado final</h2>
              <p class="page-subtitle">Revisa la puntuacion y guarda el cierre de la partida.</p>
            </div>

            <c:if test="${not empty mensajeOk}">
              <div class="note-box"><c:out value="${mensajeOk}" /></div>
            </c:if>

            <div class="result-grid">
              <div class="result-card">
                <p><c:out value="${partida.jugador1NombreSnapshot}" /></p>
                <p class="result-value"><c:out value="${partida.jugador1PuntuacionTotal}" /> VP</p>
              </div>
              <div class="result-card">
                <p>Ganador</p>
                <p class="result-value"><c:out value="${finalPartida.ganadorTexto}" /></p>
              </div>
              <div class="result-card">
                <p><c:out value="${partida.jugador2NombreSnapshot}" /></p>
                <p class="result-value"><c:out value="${partida.jugador2PuntuacionTotal}" /> VP</p>
              </div>
            </div>

            <table>
              <thead>
                <tr>
                  <th>Ronda</th>
                  <th><c:out value="${partida.jugador1NombreSnapshot}" /></th>
                  <th><c:out value="${partida.jugador2NombreSnapshot}" /></th>
                  <th>Editar</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach items="${finalPartida.rondas}" var="ronda">
                  <tr>
                    <td>Ronda <c:out value="${ronda.numeroRonda}" /></td>
                    <td>
                      Primaria <c:out value="${ronda.primariaJugador1}" /> /
                      Secundaria <c:out value="${ronda.secundariaJugador1}" /> /
                      Total <c:out value="${ronda.totalAcumuladoJugador1}" />
                    </td>
                    <td>
                      Primaria <c:out value="${ronda.primariaJugador2}" /> /
                      Secundaria <c:out value="${ronda.secundariaJugador2}" /> /
                      Total <c:out value="${ronda.totalAcumuladoJugador2}" />
                    </td>
                    <td><a href="/partidas/${partida.id}/ronda/${ronda.numeroRonda}">Editar</a></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>

            <form method="post" action="/partidas/${partida.id}/finalizar">
              <div class="form-actions">
                <a class="button-secondary" href="/partidas/${partida.id}/ronda/5">Atras</a>
                <button class="button-primary" type="submit">Finalizar y guardar resultado</button>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
